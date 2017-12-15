package com.pgssoft.testwarez.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.j256.ormlite.stmt.QueryBuilder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.event.FavoriteRefreshEvent;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Favorite;
import com.pgssoft.testwarez.database.model.LandingPageDescription;
import com.pgssoft.testwarez.database.model.Track;
import com.pgssoft.testwarez.receiver.EventNotificationReceiver;
import com.pgssoft.testwarez.service.EventNotificationService;
import com.pgssoft.testwarez.widget.SettingsView;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by rtulaza on 2015-08-10.
 */
public class Utils {
    private static final String SHARED_PREFERENCES_KEY = "shared_preferences";
    private static final String GCM_TOKENS_PREFERENCE = "gcm_tokens_preference";

    private static final String UPDATE_IN_PROGRESS_KEY = "update_in_progress_key";
    private static final String HEADERS_STATUS = "headers_status";
    private static final String SCROLL_POSITION = "scroll_position";
    private static final String GCM_TOKEN_SENT_KEY = "gcm_token_sent_key";
    private static final String SYNC_REQUIRED_KEY = "sync_required_key";
    private static final String SYNC_ON_STARTUP_KEY = "sync_on_startup_key";
    private static final String REMINDERS_ENABLED_KEY = "reminders_enabled_key";
    private static final String DOWNLOADED_FILE_ID = "downloaded_file_id";
    private static final String SNACKBAR_ELAPSED_TIME = "snackbar_elapsed_time";
    private static final String CALENDAR_SYNC_ENABLED_KEY = "calendar_enabled_key";
    private static final String REMINDER_TIME_KEY = "reminders_time_key";
    private static final String NOTIFICATION_GENERAL_KEY = "notification_general_key";
    private static final String IS_AGENDA_FILTER_STATE = "is_agenda_filter";
    private static final String SOCIAL_FILTER = "social_filter";
    private static final String AGENDA_FILTER_DAY = "agenda_filter_day";
    private static final String AGENDA_FILTER_TRACK = "agenda_filter_track";
    private static final String CONFERENCE_FILTER = "conference_filter";
    private static final String ARCHIVE_FILE_TYPE_FILTER = "archive_file_type_filter";
    private static final String LANDING_PAGE_KEY = "landing_page_shared_preferences";
    private static final String LANDING_PAGE_ID = "landing_page_id";
    private static final String IS_ALL_ARCHIVE_DATA_DOWNLOADED = "is_all_data_downloaded";

    private static final String GCM_TOKEN = "gcm_token";

    private static final String ACTIVE_CONFERENCE_ID = "active_conference_id";

    public static boolean isUpdateInProgress() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        return sp.getBoolean(UPDATE_IN_PROGRESS_KEY, false);
    }

    public static void setUpdateInProgress(boolean isUpdateInProgress) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putBoolean(UPDATE_IN_PROGRESS_KEY, isUpdateInProgress).apply();
    }

    public static void saveHeadersStatus(String status) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putString(HEADERS_STATUS, status).apply();
    }

    public static List<Boolean> getHeadersStatus() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        List<Boolean> headersStatus = new ArrayList<>();

        String result = sp.getString(HEADERS_STATUS, "");
        String[] array = result.split(",");

        for (String s : array) {
            if (s.equals("true")) {
                headersStatus.add(true);
            } else if (s.equals("false")) {
                headersStatus.add(false);
            }
        }

        return headersStatus;
    }

    public static void saveAgendaScrollPosition(int position) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putInt(SCROLL_POSITION, position).apply();
    }

    public static int getAgendaScrollPosition() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        return sp.getInt(SCROLL_POSITION, 0);
    }

    public static void clearAgendaScrollPosition() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().remove(SCROLL_POSITION).apply();
    }

    public static boolean isGcmTokenSent() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        return sp.getBoolean(GCM_TOKEN_SENT_KEY, false);
    }

    public static void setGcmTokenSent(boolean sent) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putBoolean(GCM_TOKEN_SENT_KEY, sent).apply();
    }

    public static boolean isSyncRequired() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        return sp.getBoolean(SYNC_REQUIRED_KEY, false);
    }

    public static void setSyncRequired(boolean required) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putBoolean(SYNC_REQUIRED_KEY, required).apply();
    }

    public static boolean areRemindersEnabled() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        return sp.getBoolean(REMINDERS_ENABLED_KEY, true);
    }

    public static void setRemindersEnabled(boolean enabled) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putBoolean(REMINDERS_ENABLED_KEY, enabled).apply();
    }

    public static void setDownloadedFileId(long id) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putLong(DOWNLOADED_FILE_ID, id).apply();
    }

    public static void clearDownloadedFileId() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putLong(DOWNLOADED_FILE_ID, -1).apply();
    }

    public static long getDownloadedFileId() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        return sp.getLong(DOWNLOADED_FILE_ID, -1);
    }

    public static void setSnackbarElapsedTime(long time) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putLong(SNACKBAR_ELAPSED_TIME, time).apply();
    }

    public static long getSnackbarElapsedTime() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        return sp.getLong(SNACKBAR_ELAPSED_TIME, -1);
    }

    public static SettingsView.REMINDER_TIMES getNotificationsTime() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        return SettingsView.REMINDER_TIMES.values()[sp.getInt(REMINDER_TIME_KEY, SettingsView.REMINDER_TIMES.FIFTEEN.ordinal())]; // 15 minutes
    }

    public static void setReminderTime(SettingsView.REMINDER_TIMES time) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putInt(REMINDER_TIME_KEY, time.ordinal()).apply();
    }

    public static void setCalendarSyncEnabled(boolean enabled) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putBoolean(CALENDAR_SYNC_ENABLED_KEY, enabled).apply();
    }

    public static boolean isCalendarSync() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sp.getBoolean(CALENDAR_SYNC_ENABLED_KEY, false);
    }

    public static void setNotificationGeneral(boolean enabled) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putBoolean(NOTIFICATION_GENERAL_KEY, enabled).apply();
    }

    public static boolean isNotificationGeneral() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sp.getBoolean(NOTIFICATION_GENERAL_KEY, true);
    }

    public static Drawable getDrawable(Context context, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(resId, context.getTheme()).mutate();
        } else {
            return context.getResources().getDrawable(resId).mutate();
        }
    }

    public static void saveActiveConferenceId(int id) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        sp.edit().putInt(ACTIVE_CONFERENCE_ID, id).commit();
    }

    public static int getActiveConferenceId() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sp.getInt(ACTIVE_CONFERENCE_ID, -1);
    }

    public static void showNetworkErrorSnackbar(Context context, View view) {
        Snackbar sb = Snackbar.make(view, R.string.no_network, Snackbar.LENGTH_LONG);
        sb.getView().setBackgroundColor(context.getResources().getColor(R.color.network_error_background));

        ViewGroup.LayoutParams params = sb.getView().getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        sb.getView().setLayoutParams(params);

        sb.show();
    }

    public static Drawable getDrawableFilter(Context context, int resId, int colorRes) {
        Drawable dr = getDrawable(context, resId);
        dr.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(colorRes), PorterDuff.Mode.MULTIPLY));

        return dr;
    }

    public static boolean isOverlaping(DateTime start1, DateTime end1, DateTime start2, DateTime end2) {
        if (
                ((start2.isAfter(start1) || start2.isEqual(start1)) && start2.isBefore(end1)) ||
                        (end2.isAfter(start1) && (end2.isBefore(end1) || end2.isEqual(end1))) ||
                        (start2.isBefore(start1) && end2.isAfter(end1))
                ) {
            return true;
        }

        return false;
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void listFiles(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);

        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listFiles(file.getAbsolutePath(), files);
            }
        }
    }

    public static void setEventAlarm(Context context, Favorite favorite) {

        Intent intent = new Intent(context, EventNotificationReceiver.class);
        intent.putExtra(EventNotificationService.EXTRA_EVENT_ID, favorite.getEvent().getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, favorite.getEvent().getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        int time = 0;
        SettingsView.REMINDER_TIMES times = Utils.getNotificationsTime();
        switch (times) {
            case ON_START:
                time = 0;
                break;
            case FIVE:
                time = 1000 * 60 * 5; // 5 minutes
                break;
            case FIFTEEN:
                time = 1000 * 60 * 15; // 15 minutes
                break;
            case THIRTY:
                time = 1000 * 60 * 30; // 30 minutes
                break;
            case HOUR:
                time = 1000 * 60 * 60; // 60 minutes
                break;
        }

        if (System.currentTimeMillis() >= favorite.getEvent().getStartAt().getMillis() - time) {
            return;
        }

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, favorite.getEvent().getStartAt().getMillis() - time, pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, favorite.getEvent().getStartAt().getMillis() - time, pendingIntent);
        }

    }

    public static void cancelEventAlarm(Context context, Event event) {
        Intent intent = new Intent(context, EventNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, event.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    public static void setCalendarEvent(Context context, Favorite favorite) throws SecurityException {

        Event event = favorite.getEvent();

        long calendarId = getCalendarId(context);
        ContentValues calendarValues = new ContentValues();
        calendarValues.put(CalendarContract.Events.DTSTART, event.getStartAt().getMillis());
        calendarValues.put(CalendarContract.Events.DTEND, event.getEndAt().getMillis());
        calendarValues.put(CalendarContract.Events.TITLE, event.getTitle());
        calendarValues.put(CalendarContract.Events.DESCRIPTION, event.getDescription());
        calendarValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        calendarValues.put(CalendarContract.Events.CALENDAR_ID, calendarId);

        Uri insertUri = context.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, calendarValues);
        Timber.d("Utils:setCalendarEvent: uri: " + insertUri);


        calendarValues.clear();
        long eventId = new Long(insertUri.getLastPathSegment());
        favorite.setCalendarId(eventId);


        calendarValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
        calendarValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        calendarValues.put(CalendarContract.Reminders.MINUTES, 0);
        Uri reminderUri = context.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, calendarValues);
        Timber.d("Utils:setCalendarEvent: reminder uri:" + reminderUri);

        long reminderId = new Long(reminderUri.getLastPathSegment());
        favorite.setReminderId(reminderId);

        try {
            ApplicationController.getDatabaseHelper().getFavoriteDao().createOrUpdate(favorite);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void cancelCalendarEvent(Context context, Event event) throws SecurityException {

        Favorite favorite = null;

        try {
            List<Favorite> favoriteList = ApplicationController.getDatabaseHelper().getFavoriteDao().queryForAll();

            for (Favorite f : favoriteList) {
                if (f.getEvent().getId() == event.getId()) {
                    favorite = f;
                }
            }

            if (favorite != null) {
                String[] projection = {
                        CalendarContract.Events.CALENDAR_ID,
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND,
                };

                String selection = CalendarContract.Events.CALENDAR_ID + " = " + getCalendarId(context);
                selection = null;

                Uri eventUri = Uri.parse(CalendarContract.Events.CONTENT_URI + "/" + favorite.getCalendarId());
                Uri reminderUri = Uri.parse(CalendarContract.Reminders.CONTENT_URI + "/" + favorite.getReminderId());
                Timber.d("Utils:cancelCalendarEvent: eventUri: " + eventUri);
                Cursor cursor = context.getContentResolver().query(eventUri, projection, selection
                        , null, null);

                Timber.d("Utils:cancelCalendarEvent: cursors cound: " + cursor.getCount());
                if (cursor.moveToFirst()) {
                    int deleteEventCount = context.getContentResolver().delete(eventUri, null, null);
                    int deleteReminderCount = context.getContentResolver().delete(reminderUri, null, null);

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static String capitalizeFirstLetter(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public static void saveAsDefaultLanguage(Context context, String lang) {
        SharedPreferences langSharedPreference = context.getSharedPreferences(BaseActivity.LANG_PREFS, Context.MODE_PRIVATE);
        langSharedPreference.edit().putString(BaseActivity.LANG_KEY, lang).apply();
    }

    public static void setLanguage(Context context) {
        SharedPreferences langSharedPreference = context.getSharedPreferences(BaseActivity.LANG_PREFS, Context.MODE_PRIVATE);
        String defaultLang = getLanguage();

        String lang = langSharedPreference.getString(BaseActivity.LANG_KEY, defaultLang);

        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();

        conf.locale = new Locale(lang);

        Locale.setDefault(conf.locale);
        res.updateConfiguration(conf, dm);
    }

    private static long getCalendarId(Context context) throws SecurityException {


        String[] projection = new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.NAME};
        String selection =
                CalendarContract.Calendars.ACCOUNT_NAME +
                        " = ? AND " +
                        CalendarContract.Calendars.ACCOUNT_TYPE +
                        " = ? ";
        // use the same values as above:
        Cursor cursor =
                context.getContentResolver().
                        query(
                                CalendarContract.Calendars.CONTENT_URI,
                                projection,
                                null,
                                null,
                                null);
        if (cursor.moveToFirst()) {

            while (!cursor.isAfterLast()) {
                Timber.d("Utils:getCalendarId: id:%3d title:%10s", cursor.getInt(0), cursor.getString(1));
                cursor.moveToNext();
            }

            cursor.moveToFirst();

            return cursor.getLong(0);
        }

        return -1;
    }

    public static void hideKeyboard(Activity context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (context.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static boolean validHexColor(String color) {

        String HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

        Pattern pattern = Pattern.compile(HEX_PATTERN);

        Matcher matcher = pattern.matcher(color);

        return matcher.matches();
    }

    public static void saveFilterState(boolean state) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        sp.edit().putBoolean(IS_AGENDA_FILTER_STATE, state).apply();
    }

    public static boolean isFilter() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        return sp.getBoolean(IS_AGENDA_FILTER_STATE, false);
    }

    /**
     * If user isn't sign in on this, event, will be sign in. If is sign on this event will be sign out
     * Thanks this, user can get remind notification from app or calendar
     *
     * @param context
     * @param event     event on which user wants sign in or sign out
     * @param indicator indicate state of user signing
     */
    public static void signInOutFavorite(Context context, Event event, ImageView indicator, CompositeSubscription compositeSubscription, boolean override) {

        compositeSubscription.add(Observable.defer(() -> Observable.just(DatabaseUtils.isFavorite(event)))
                .doOnError(Throwable::printStackTrace)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .flatMap(isFavorite -> {
                    if (isFavorite) {

                        Callable c = () -> {
                            DatabaseUtils.removeFavorite(event);
                            return null;
                        };

                        return Observable.fromCallable(c)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext(o -> {
                                    indicator.setImageDrawable(Utils.getDrawable(context, R.drawable.calendar_grey));
                                    Toast.makeText(context, R.string.remove_from_favorite_success, Toast.LENGTH_SHORT).show();
                                    ApplicationController.getBus().post(new FavoriteRefreshEvent(event));
                                });

                    } else {
                        return Observable.just(DatabaseUtils.addFavorite(event, override))
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext(addedEvent -> {
                                    if (addedEvent == event) {
                                        Toast.makeText(context, R.string.add_to_favorite_success, Toast.LENGTH_SHORT).show();
                                        indicator.setImageDrawable(Utils.getDrawableFilter(context, R.drawable.calendar, R.color.primary));
                                    } else {
                                        showChangeFavoriteDialog(context, addedEvent, event, indicator, compositeSubscription);
                                    }
                                    ApplicationController.getBus().post(new FavoriteRefreshEvent(event));
                                });
                    }
                })
                .subscribe()
        );
    }

    private static void showChangeFavoriteDialog(Context context, Event oldEvent, Event newEvent, ImageView indicator, CompositeSubscription compositeSubscription) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.switch_favorite_event_title))
                .setMessage(context.getString(R.string.switch_favorite_event_text, newEvent.getTitle(), oldEvent.getTitle()))
                .setPositiveButton(context.getString(R.string.switch_favorite_event_actual), (dialogInterface, i) ->
                        signInOutFavorite(context, newEvent, indicator, compositeSubscription, true))
                .setNegativeButton(context.getString(R.string.switch_favorite_event_previous), (dialogInterface, i) ->
                        dialogInterface.dismiss())
                .show();
    }

    public static String getLanguage() {
        // TODO In current version only polish language is supported, uncomment this line and
        // TODO remove the line below in case of supporting english language too
        //String lang = Locale.getDefault().getLanguage();
        String lang = "pl";

        if (lang.equals("pl") || lang.equals("en")) {
            return lang;
        } else {
            return "en";
        }
    }

    public static String getSaveLanguage(Context context) {
        SharedPreferences langSharedPreference = context.getSharedPreferences(BaseActivity.LANG_PREFS, Context.MODE_PRIVATE);
        return langSharedPreference.getString(BaseActivity.LANG_KEY, getLanguage());
    }


    public static LandingPageDescription getLandingPage(Context context) {
        SharedPreferences landingPageSharedPreferences = context.getSharedPreferences(LANDING_PAGE_KEY, Context.MODE_PRIVATE);
        int id = landingPageSharedPreferences.getInt(LANDING_PAGE_ID, -1);

        List<LandingPageDescription> descriptions = new ArrayList<>();
        try {
            QueryBuilder<LandingPageDescription, Integer> qb = ApplicationController.getDatabaseHelper().getLandingPageDescriptionDao().queryBuilder();
            qb.where().eq(LandingPageDescription.LANDING_PAGE_ID, id);

            descriptions.addAll(qb.query());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String lang = Utils.getLanguage();

        for (LandingPageDescription description : descriptions) {
            if (lang.equals(description.getLang())) {
                return description;
            }
        }

        if (descriptions.size() > 0) {
            return descriptions.get(0);
        }

        return null;
    }

    /**
     * set tracks color in the indicator
     *
     * @param event
     * @param trackIndicator
     */
    public static void divideIndicatorByTrack(Context context, Event event, LinearLayout trackIndicator) {
        if (event.getTrackList() != null && !event.getTrackList().isEmpty()) {
            trackIndicator.setVisibility(View.VISIBLE);

            float weight = 1f / event.getTrackList().size();
            LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, weight);


            for (Track track : event.getTrackList()) {
                if (track == null) {
                    continue;
                }

                View trackView = new View(context);
                trackView.setBackgroundColor(Color.parseColor(track.getColor()));
                trackView.setLayoutParams(viewParams);
                trackIndicator.addView(trackView);
            }
        } else {
            trackIndicator.setVisibility(View.GONE);
        }
    }

    public static void clearSocialFilter() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        sp.edit().remove(SOCIAL_FILTER).apply();
    }

    public static void saveAgendaFilter(ArrayList<Integer> dayCategoryIdList, ArrayList<Integer> trackList) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        try {
            editor.remove(AGENDA_FILTER_DAY);
            editor.remove(AGENDA_FILTER_TRACK);
            editor.putString(AGENDA_FILTER_DAY, ObjectSerializer.serialize(dayCategoryIdList));
            editor.putString(AGENDA_FILTER_TRACK, ObjectSerializer.serialize(trackList));

        } catch (IOException e) {
            e.printStackTrace();
        }

        editor.apply();
    }

    public static void clearAllFilters() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.remove(AGENDA_FILTER_DAY);
        editor.remove(AGENDA_FILTER_TRACK);
        editor.remove(SOCIAL_FILTER);
        editor.remove(CONFERENCE_FILTER);
        editor.remove(ARCHIVE_FILE_TYPE_FILTER);

        editor.apply();
    }

    public static List<Integer> getAgendaDayFilter() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        List<Integer> dayList = new ArrayList<>();
        try {
            if (sp.contains(AGENDA_FILTER_DAY)) {
                dayList = (List<Integer>) ObjectSerializer.deserialize(sp.getString(AGENDA_FILTER_DAY, ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dayList;
    }

    public static List<Integer> getAgendaTrackFilter() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        List<Integer> trackList = new ArrayList<>();
        try {
            if (sp.contains(AGENDA_FILTER_DAY)) {
                trackList = (List<Integer>) ObjectSerializer.deserialize(sp.getString(AGENDA_FILTER_TRACK, ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trackList;
    }

    public static void saveSocialFilter(ArrayList<Integer> socialFilter) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        try {
            editor.putString(SOCIAL_FILTER, ObjectSerializer.serialize(socialFilter)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> getSocialFilter() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        List<Integer> socialList = new ArrayList<>();

        try {
            if (sp.contains(SOCIAL_FILTER)) {
                socialList = (List<Integer>) ObjectSerializer.deserialize(sp.getString(SOCIAL_FILTER, ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Timber.d("Utils:getSocialFilter: " + String.valueOf(socialList));
        return socialList;

    }

    public static void clearConferencesFilter() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        sp.edit().remove(CONFERENCE_FILTER).apply();
    }

    public static void saveConferencesFilter(ArrayList<Integer> conferencesFilter) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        try {
            editor.putString(CONFERENCE_FILTER, ObjectSerializer.serialize(conferencesFilter)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> getConferenceFilter() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        List<Integer> conferenceList = new ArrayList<>();

        try {
            if (sp.contains(CONFERENCE_FILTER)) {
                conferenceList = (List<Integer>) ObjectSerializer.deserialize(sp.getString(CONFERENCE_FILTER, ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return conferenceList;
    }

    public static List<Integer> getArchiveFileTypesFilter() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        List<Integer> archiveFileTypeList = new ArrayList<>();

        try {
            if (sp.contains(ARCHIVE_FILE_TYPE_FILTER)) {
                archiveFileTypeList = (List<Integer>) ObjectSerializer.deserialize(sp.getString(ARCHIVE_FILE_TYPE_FILTER, ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return archiveFileTypeList;
    }

    public static void clearArchiveFileTypesFilter() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        sp.edit().remove(ARCHIVE_FILE_TYPE_FILTER).apply();
    }

    public static void saveArchiveFileTypesFilter(ArrayList<Integer> archiveFileTypesFilter) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        try {
            editor.putString(ARCHIVE_FILE_TYPE_FILTER, ObjectSerializer.serialize(archiveFileTypesFilter)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isInternetConnection(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void saveGCMToken(String token) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(GCM_TOKENS_PREFERENCE, Context.MODE_PRIVATE);
        sp.edit().putString(GCM_TOKEN, token).apply();
        Timber.d("Utils:saveGCMToken: %s", token);
    }

    public static String getGCMToken() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(GCM_TOKENS_PREFERENCE, Context.MODE_PRIVATE);
        String token = sp.getString(GCM_TOKEN, null);
        Timber.d("Utils:getGCMToken: %s", token);
        return token;
    }

    public static boolean isGCMTokenPerConference(int conferenceId) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(GCM_TOKENS_PREFERENCE, Context.MODE_PRIVATE);
        boolean isToken = sp.getBoolean(String.format("Conference_%d", conferenceId), false);
        Timber.d("Utils:isGCMTokenPerConference: Conference %d, isToken %s", conferenceId, isToken);
        return isToken;
    }

    public static List<Boolean> getGCMTokensFlags() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(GCM_TOKENS_PREFERENCE, Context.MODE_PRIVATE);

        List<Boolean> tokensFlag = new ArrayList<>();
        for (String key : sp.getAll().keySet()) {

            Object o = sp.getAll().get(key);
            if (o instanceof Boolean) {
                tokensFlag.add((Boolean) o);
            }
        }

        return tokensFlag;
    }

    public static void setGCMTokenPerConference(int conferenceId) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(GCM_TOKENS_PREFERENCE, Context.MODE_PRIVATE);
        sp.edit().putBoolean(String.format("Conference_%d", conferenceId), true).apply();
        Timber.d("Utils:setGCMTokenPerConference: Conference %d", conferenceId);
    }

    public static void setIsAllArchiveDataDownloaded(boolean isAllDataDownloaded) {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(IS_ALL_ARCHIVE_DATA_DOWNLOADED, Context.MODE_PRIVATE);
        sp.edit().putBoolean(IS_ALL_ARCHIVE_DATA_DOWNLOADED, isAllDataDownloaded).apply();
    }

    public static boolean isAllArchiveDataDownloaded() {
        SharedPreferences sp = ApplicationController.getInstance().getSharedPreferences(IS_ALL_ARCHIVE_DATA_DOWNLOADED, Context.MODE_PRIVATE);
        return sp.getBoolean(IS_ALL_ARCHIVE_DATA_DOWNLOADED, false);
    }

    public static boolean isTablet(Context context) {
        return context != null && context.getResources().getBoolean(R.bool.isTablet);
    }

    public static String convertMimeTypeToStoreExtension(String mimeType) {
        String result;

        switch (mimeType) {
            case "application/pdf":
                result = "pdf";
                break;
            case "application/msword":
                result = "msword";
                break;
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                result = "msword";
                break;
            case "application/vnd.ms-powerpoint":
                result = "powerpoint";
                break;
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                result = "powerpoint";
                break;
            default:
                result = null;
        }

        return result;
    }

    public static boolean isImageInCache(String imageUrl) {
        return ImageLoader.getInstance().getDiskCache().get(imageUrl).exists() ||
                ImageLoader.getInstance().getMemoryCache().get(imageUrl) != null;
    }
}
