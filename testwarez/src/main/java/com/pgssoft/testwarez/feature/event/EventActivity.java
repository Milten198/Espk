package com.pgssoft.testwarez.feature.event;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.feature.speaker.detail.PersonActivity;
import com.pgssoft.testwarez.event.OpenEventEvent;
import com.pgssoft.testwarez.event.OpenPersonEvent;
import com.pgssoft.testwarez.feature.agenda.AgendaActivity;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.database.model.Staff;
import com.pgssoft.testwarez.database.model.Track;
import com.pgssoft.testwarez.util.DatabaseUtils;
import com.pgssoft.testwarez.util.EventBarBehavior;
import com.pgssoft.testwarez.util.FinishWithoutAnim;
import com.pgssoft.testwarez.util.Observables;
import com.pgssoft.testwarez.util.Utils;
import com.pgssoft.testwarez.util.helper.AnimatorHelperListener;
import com.squareup.otto.Subscribe;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import org.joda.time.DateTime;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class EventActivity extends BaseActivity implements FinishWithoutAnim {

    public static final String EVENT_ID = "event_id";
    public static final int REFRESH_NEEDED = 1;
    public static final int OPEN_PERSON_REQUEST = 13;
    public static final int RATE_REQUEST = 15;

    public static final String ANIMATION_BUNDLE = "animation_bundle";
    public static final String TITLE_LEFT = "title_left";
    public static final String TITLE_TOP = "title_top";
    public static final String TITLE_WIDTH = "title_width";
    public static final String TITLE_HEIGHT = "title_height";
    public static final String TITLE_COLOR = "title_color";

    public static final String TRACK_TOP = "track_top";
    public static final String TRACK_HEIGHT = "track_height";
    private static final long ANIM_DURATION = 300;

    private EventDataAdapter adapter;

    private Event event;
    private int eventId = -1;

    private Point screenSize = new Point();

    private Bundle animBundle = new Bundle();

    @Bind(R.id.toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.nestedscrollview)
    protected RecyclerView recyclerView;
    @Bind(R.id.fab)
    protected FloatingActionButton fab;

    @Bind(R.id.vActivityEventTrackIndicator)
    protected LinearLayout trackIndicator;

    @Bind(R.id.tvActivityEventContinous)
    protected TextView continousTextView;

    @Bind(R.id.clActivityEvent)
    protected CoordinatorLayout containerCoordinatorLayout;

    @Bind(R.id.app_bar_layout)
    protected AppBarLayout appBarLayout;

    @Bind(R.id.rlActivityEventBarContent)
    protected RelativeLayout appBarContent;

    @Bind(R.id.collapsing_toolbar)
    protected CollapsingToolbarLayout collapsingToolbarLayout;

    private ColorDrawable mBackground;
    private float trackDeltaY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            eventId = getIntent().getExtras().getInt(EVENT_ID, -1);
        }
        if (eventId == -1) {
            finish();
        }

        getWindowManager().getDefaultDisplay().getSize(screenSize);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(view -> {
            ApplicationController.getParentManager().runParentActivity(this);
        });

        mBackground = new ColorDrawable(Color.WHITE);
        containerCoordinatorLayout.setBackground(mBackground);

        animBundle = getIntent().getBundleExtra(ANIMATION_BUNDLE);

        if (animBundle != null) {
            trackIndicator.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    trackIndicator.getViewTreeObserver().removeOnPreDrawListener(this);

                    setViewsBeginState();
                    enterAnimation();
                    return true;
                }
            });
        }

        loadEvent();
    }

    private void loadEvent() {
        compositeSubscription.add(
                Observable.defer(() -> Observables.getInstance().getEventObservables().getActiveEventsObservable())
                        .doOnError(Throwable::printStackTrace)
                        .filter(event -> event != null)
                        .filter(event1 -> event1.getId() == eventId)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(event -> {
                            Timber.i("LoadEvent: " + event.isRated());
                            if (animBundle != null) {
                                RelativeLayout.LayoutParams trackParams = (RelativeLayout.LayoutParams) trackIndicator.getLayoutParams();
                                trackParams.height = animBundle.getInt(TRACK_HEIGHT);
                                trackIndicator.setLayoutParams(trackParams);

                            }

                            this.event = event;
                            prepareEvent();
                        }));
    }

    private void prepareEvent() {
        DateTime currentTime = new DateTime();

        if (currentTime.isAfter(event.getStartAt()) && currentTime.isBefore(event.getEndAt())) {
            continousTextView.setVisibility(View.VISIBLE);
        }

        divideIndicatorByTrack(event);
        if (!event.isTechnical()) {
            ArrayList<Speaker> speakers = event.getAllSpeakers();
            adapter = new EventDataAdapter(this, event, speakers, null, screenSize);
        } else {
            ArrayList<Staff> staff = event.getAllStaff();
            adapter = new EventDataAdapter(this, event, null, staff, screenSize);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        collapsingToolbarLayout.setTitle(event.getTitle());
        compositeSubscription.add(Observable.defer(() -> Observable.just(DatabaseUtils.isFavorite(event)))
                .doOnError(Throwable::printStackTrace)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isFavorite -> {
                    if (isFavorite) {
                        fab.setImageDrawable(Utils.getDrawableFilter(this, R.drawable.calendar, R.color.primary));
                    } else {
                        fab.setImageDrawable(Utils.getDrawable(this, R.drawable.calendar_grey));
                    }
                }));

        fab.setOnClickListener(v -> changeFavorite());

        new Handler().post(() -> recyclerView.scrollToPosition(0));
    }


    private void enterAnimation() {
        ObjectAnimator trackTop = ObjectAnimator.ofFloat(trackIndicator, View.TRANSLATION_Y, 0f);
        ObjectAnimator appBarTop = ObjectAnimator.ofFloat(appBarLayout, View.ALPHA, 0f, 1f);
        ObjectAnimator fabXScale = ObjectAnimator.ofFloat(fab, View.SCALE_X, 1f);
        ObjectAnimator fabYScale = ObjectAnimator.ofFloat(fab, View.SCALE_Y, 1f);
        ObjectAnimator bfAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        ObjectAnimator continousTextViewAnim = ObjectAnimator.ofFloat(continousTextView, "alpha", 1f);
        ObjectAnimator recyclerAnim = ObjectAnimator.ofFloat(recyclerView, View.TRANSLATION_Y, recyclerView.getHeight(), 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(recyclerAnim)
                .with(trackTop)
                .with(continousTextViewAnim)
                .with(fabXScale)
                .with(fabYScale)
                .after(50)
                .after(bfAnim)
                .after(appBarTop);

        animatorSet.setDuration(ANIM_DURATION);


        animatorSet.addListener(new AnimatorHelperListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                containerCoordinatorLayout.setClipChildren(true);
                CoordinatorLayout.LayoutParams contentParams = (CoordinatorLayout.LayoutParams) appBarContent.getLayoutParams();
                contentParams.setBehavior(new EventBarBehavior(containerCoordinatorLayout.getContext()));
                appBarContent.setLayoutParams(contentParams);
            }
        });
        animatorSet.start();
    }

    private void setViewsBeginState() {
        trackIndicator.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams trackParams = (RelativeLayout.LayoutParams) trackIndicator.getLayoutParams();
        trackParams.height = animBundle.getInt(TRACK_HEIGHT);
        trackIndicator.setLayoutParams(trackParams);

        int[] trackLocation = new int[2];
        trackIndicator.getLocationOnScreen(trackLocation);
        trackDeltaY = animBundle.getInt(TRACK_TOP) - (trackLocation[1] * 2);
        containerCoordinatorLayout.setClipChildren(false);

        recyclerView.setTranslationY(recyclerView.getHeight());
        continousTextView.setAlpha(0f);

        fab.setScaleX(0f);
        fab.setScaleY(0f);
    }

    private void exitAnimation() {

        appBarLayout.setExpanded(true, false);

        containerCoordinatorLayout.setClipChildren(false);

        ObjectAnimator appBarContentAnim = ObjectAnimator.ofFloat(appBarContent, View.TRANSLATION_Y, 0f);
        ObjectAnimator fabXScale = ObjectAnimator.ofFloat(fab, View.SCALE_X, 0f);
        ObjectAnimator fabYScale = ObjectAnimator.ofFloat(fab, View.SCALE_Y, 0f);
        ObjectAnimator appBarTop = ObjectAnimator.ofFloat(appBarLayout, View.ALPHA, 0f);
        ObjectAnimator bfAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
        ObjectAnimator continousTextViewAnim = ObjectAnimator.ofFloat(continousTextView, "alpha", 0f);
        ObjectAnimator trackTop = ObjectAnimator.ofFloat(trackIndicator, View.TRANSLATION_Y, trackDeltaY);
        ObjectAnimator recyclerAnim = ObjectAnimator.ofFloat(recyclerView, View.TRANSLATION_Y, recyclerView.getHeight());

        AnimatorSet animatorSet = new AnimatorSet();


        AnimatorSet.Builder builder = animatorSet.play(recyclerAnim);
        builder
                .with(trackTop)
                .with(fabXScale)
                .with(fabYScale)
                .with(continousTextViewAnim);

        if (appBarContent.getTranslationY() != 0) {
            builder.with(appBarContentAnim);
        }

        builder.before(bfAnim)
                .before(appBarTop);

        animatorSet.setDuration(ANIM_DURATION);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.addListener(new AnimatorHelperListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                EventActivity.super.finish();
                EventActivity.this.overridePendingTransition(0, 0);
            }
        });
        animatorSet.start();
    }

    @Override
    public void finish() {
        exitAnimation();
    }

    @Override
    public void finishWithoutAnimation() {
        super.finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void divideIndicatorByTrack(Event event) {
        if (event.getTrackList() != null && !event.getTrackList().isEmpty()) {

            if (animBundle != null) {
                int[] trackLocation = new int[2];
                trackIndicator.getLocationOnScreen(trackLocation);
                trackDeltaY = animBundle.getInt(TRACK_TOP) - (trackLocation[1] * 2);
                trackIndicator.setTranslationY(trackDeltaY);
            }
            trackIndicator.setVisibility(View.VISIBLE);

            float weight = 1f / event.getTrackList().size();
            LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, weight);

            for (Track track : event.getTrackList()) {
                View trackView = new View(this);
                trackView.setBackgroundColor(Color.parseColor(track.getColor()));
                trackView.setLayoutParams(viewParams);
                trackIndicator.addView(trackView);
            }

        } else {
            trackIndicator.setVisibility(View.GONE);
        }
    }

    private void changeFavorite() {

        if (Utils.isCalendarSync() && !calendarPermissionGranted(true)) {
            return;
        }

        Utils.signInOutFavorite(EventActivity.this, event, fab, compositeSubscription, false);

        Intent eventIntent = new Intent();
        eventIntent.putExtra(EVENT_ID, event.getId());
        setResult(REFRESH_NEEDED, eventIntent);
    }

    @Subscribe
    public void onOpenSpeakerEvent(OpenPersonEvent event) {
        PersonActivity.open(this, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CALENDAR: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    changeFavorite();
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_PERSON_REQUEST && resultCode == PersonActivity.REFRESH_NEEDED) {
            compositeSubscription.add(Observable.defer(() -> Observable.just(DatabaseUtils.isFavorite(event)))
                    .doOnError(Throwable::printStackTrace)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isFavorite -> {
                        if (isFavorite) {
                            fab.setImageDrawable(Utils.getDrawableFilter(this, R.drawable.calendar, R.color.primary));
                        } else {
                            fab.setImageDrawable(Utils.getDrawable(this, R.drawable.calendar_grey));
                        }

                        Intent eventIntent = new Intent();
                        eventIntent.putExtra(EVENT_ID, event.getId());
                        setResult(REFRESH_NEEDED, eventIntent);
                    }));

        } else if (requestCode == RATE_REQUEST && resultCode == RESULT_OK) {
            adapter = null;
            loadEvent();
        }
    }


    public static void open(Activity activity, OpenEventEvent openEventEvent) {
        Intent intent = new Intent(activity, EventActivity.class);
        Bundle bundle = new Bundle();

        intent.putExtra(EventActivity.EVENT_ID, openEventEvent.event.getId());

        TextView title = openEventEvent.getTitle();

        int[] titleLocation = new int[2];
        title.getLocationOnScreen(titleLocation);

        bundle.putInt(TITLE_LEFT, titleLocation[0]);
        bundle.putInt(TITLE_TOP, titleLocation[1]);
        bundle.putInt(TITLE_WIDTH, title.getWidth());

        bundle.putInt(TITLE_HEIGHT, title.getHeight());
        bundle.putInt(TITLE_COLOR, title.getCurrentTextColor());

        int[] trackLocation = new int[2];
        openEventEvent.getTrack().getLocationOnScreen(trackLocation);
        Timber.d("EventActivity:open: trackY: " + trackLocation[1]);

        bundle.putInt(TRACK_HEIGHT, openEventEvent.getTrack().getHeight());
        bundle.putInt(TRACK_TOP, trackLocation[1]);

        intent.putExtra(ANIMATION_BUNDLE, bundle);

        activity.startActivityForResult(intent, AgendaActivity.OPEN_EVENT_REQUEST);
        activity.overridePendingTransition(0, 0);
    }
}
