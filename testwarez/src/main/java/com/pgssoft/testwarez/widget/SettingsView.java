package com.pgssoft.testwarez.widget;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.BuildConfig;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.event.ChangeLanguageEvent;
import com.pgssoft.testwarez.database.model.Language;
import com.pgssoft.testwarez.util.DatabaseUtils;
import com.pgssoft.testwarez.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by rtulaza on 2015-08-13.
 */
public class SettingsView extends RelativeLayout {

    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private ArrayAdapter<String> langAdapter;

    public enum REMINDER_TIMES {
        ON_START, FIVE, FIFTEEN, THIRTY, HOUR
    }

    @Bind(R.id.notifications_enable)
    SwitchCompat reminderSwitchCompat;

    @Bind(R.id.notifications_general_enable)
    SwitchCompat notificationGeneralSwitchCompat;

    @Bind(R.id.notifications_time_title)
    View title;

    @Bind(R.id.notifications_time_description)
    View description;

    @Bind(R.id.notifications_time_spinner)
    Spinner time;

    @Bind(R.id.scViewSettingsCalendarSynchronize)
    SwitchCompat calendarSynchronizeSwitchCompat;

    @Bind(R.id.version_text_view)
    TextView versionTextView;

    @Bind(R.id.sViewSettingLanguage)
    Spinner langSpinner;

    @Bind(R.id.llViewSettingLanguage)
    LinearLayout languageLayout;

    private List<Locale> langList = new ArrayList<>();

    public SettingsView(Context context) {
        super(context);
        init();
    }

    public SettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_settings, this, true);

        ButterKnife.bind(this);

        boolean notifEnabled = Utils.areRemindersEnabled();
        reminderSwitchCompat.setChecked(notifEnabled);
        title.setEnabled(notifEnabled);
        description.setEnabled(notifEnabled);
        time.setEnabled(notifEnabled);

        notificationGeneralSwitchCompat.setChecked(Utils.isNotificationGeneral());
        calendarSynchronizeSwitchCompat.setChecked(Utils.isCalendarSync());

        String version = getResources().getString(R.string.version, BuildConfig.VERSION_NAME);
        versionTextView.setText(version);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.notification_times, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time.setAdapter(adapter);

        time.setSelection(Utils.getNotificationsTime().ordinal());

        reminderSwitchCompat.setOnCheckedChangeListener(reminderCheckListener);

        time.setOnItemSelectedListener(timeSelectedListener);

        notificationGeneralSwitchCompat.setOnCheckedChangeListener(notificationGeneralCheckListener);

        calendarSynchronizeSwitchCompat.setOnCheckedChangeListener(calendarSynchronizeCheckListener);


        langAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item);
        langSpinner.setAdapter(langAdapter);

        compositeSubscription.add(Observable.defer(() -> Observable.just(ApplicationController.getActiveConference()))
                .flatMap(conference -> Observable.from(conference.getLanguages()))
                .toList()
                .flatMap(languages -> {
                    if (languages.size() > 1) {
                        return Observable.from(languages);
                    } else {
                        return Observable.empty();
                    }
                })
                .map(Language::getLanguage)
                .map(lang -> new Locale(lang))

                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(locale -> {
                    languageLayout.setVisibility(VISIBLE);
                    langAdapter.add(Utils.capitalizeFirstLetter(locale.getDisplayLanguage()));
                    langList.add(locale);
                }, Throwable::printStackTrace, () -> {


                    int position = langList.indexOf(Locale.getDefault());
                    if (position >= 0) {
                        langSpinner.setSelection(position, false);
                    } else {
                        langSpinner.setSelection(0, false);
                    }
                    langSpinner.setOnItemSelectedListener(langSelectedListener);
                    langAdapter.notifyDataSetChanged();
                }));

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        compositeSubscription.clear();
        compositeSubscription.unsubscribe();
    }

    private void changeTimeInAllAlarms() {

        compositeSubscription.add(
                Observable.fromCallable(() -> ApplicationController.getDatabaseHelper().getFavoriteDao().queryForAll())
                        .doOnError(Throwable::printStackTrace)
                        .subscribeOn(Schedulers.newThread())
                        .flatMap(favorites -> {
                            if (Utils.areRemindersEnabled()) {
                                return Observable.from(favorites)
                                        .doOnNext(favorite -> {
                                            Utils.cancelEventAlarm(getContext(), favorite.getEvent());
                                            Utils.setEventAlarm(getContext(), favorite);
                                        });
                            } else {
                                if (!((BaseActivity) getContext()).calendarPermissionGranted(false)) {
                                    return Observable.just(1)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnNext(integer -> ((BaseActivity) getContext()).calendarPermissionGranted(false));
                                } else {
                                    return Observable.from(favorites)
                                            .doOnNext(favorite -> {
                                                Utils.cancelCalendarEvent(getContext(), favorite.getEvent());
                                                Utils.setCalendarEvent(getContext(), favorite);
                                            });
                                }
                            }

                        }).subscribe());

    }

    AdapterView.OnItemSelectedListener langSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Locale lang = langList.get(position);
            ApplicationController.getBus().post(new ChangeLanguageEvent(lang));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    public void updateView() {
        boolean notifEnabled = Utils.areRemindersEnabled();
        reminderSwitchCompat.setChecked(notifEnabled);
        title.setEnabled(notifEnabled);
        description.setEnabled(notifEnabled);
        time.setEnabled(notifEnabled);

        time.setSelection(Utils.getNotificationsTime().ordinal());
    }

    CompoundButton.OnCheckedChangeListener reminderCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (calendarSynchronizeSwitchCompat.isChecked() && isChecked) {
                calendarSynchronizeSwitchCompat.setChecked(false);
            }

            Utils.setRemindersEnabled(isChecked);

            title.setEnabled(isChecked);
            description.setEnabled(isChecked);
            time.setEnabled(isChecked);

            compositeSubscription.add(
                    Observable.just(isChecked)
                            .doOnError(Throwable::printStackTrace)
                            .observeOn(Schedulers.newThread())
                            .subscribe(isChecked1 -> {
                                if (isChecked1) {
                                    DatabaseUtils.addAlarmsToAllFavoriteEvents();
                                } else {
                                    DatabaseUtils.removeAlarmsFromAllEvents();
                                }
                            }));

        }
    };

    AdapterView.OnItemSelectedListener timeSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Utils.setReminderTime(REMINDER_TIMES.values()[position]);

            changeTimeInAllAlarms();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    CompoundButton.OnCheckedChangeListener notificationGeneralCheckListener = (buttonView, isChecked) -> Utils.setNotificationGeneral(isChecked);

    CompoundButton.OnCheckedChangeListener calendarSynchronizeCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (reminderSwitchCompat.isChecked() && isChecked) {
                reminderSwitchCompat.setChecked(false);
            }

            if (!((BaseActivity) getContext()).calendarPermissionGranted(false)) {
                buttonView.setChecked(false);
                return;
            }

            Utils.setCalendarSyncEnabled(isChecked);

            compositeSubscription.add(Observable.just(isChecked)
                    .doOnError(Throwable::printStackTrace)
                    .observeOn(Schedulers.newThread())
                    .subscribe(isChecked1 -> {
                        if (isChecked1) {
                            DatabaseUtils.addAlarmsToCalendar();
                        } else {
                            DatabaseUtils.removeAlarmsFromCalendar();
                        }
                    }));
        }
    };

    public void onCalendarPermissionGranted(boolean isGranted) {
        calendarSynchronizeSwitchCompat.setChecked(isGranted);
    }

}
