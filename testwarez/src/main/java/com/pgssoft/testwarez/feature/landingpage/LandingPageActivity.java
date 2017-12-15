package com.pgssoft.testwarez.feature.landingpage;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pwittchen.reactivenetwork.library.ConnectivityStatus;
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.LandingPageResponseEvent;
import com.pgssoft.testwarez.event.SyncDataEvent;
import com.pgssoft.testwarez.feature.agenda.AgendaActivity;
import com.pgssoft.testwarez.feature.archive.ArchiveActivity;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.database.model.LandingPageDescription;
import com.pgssoft.testwarez.networking.NetworkSevice;
import com.pgssoft.testwarez.service.SyncService;
import com.pgssoft.testwarez.util.SimpleImageLoader;
import com.pgssoft.testwarez.util.Utils;
import com.squareup.otto.Subscribe;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by dpodolak on 22.03.16.
 */
public class LandingPageActivity extends RxAppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final String SNACK = "snack";
    CompositeSubscription compositeSubscription = new CompositeSubscription();
    public static final String LANDING_PAGE_SHARED_PREFERENCES = "landing_page_shared_preferences";
    public static final String LANDING_PAGE_ID = "landing_page_id";

    private static final int ANIM_DURATION = 500;


    @Bind(R.id.ivActivityLandingPageLogo)
    ImageView logoImageView;
    @Bind(R.id.ivActivityLandingPageBanner)
    ImageView bannerImageView;
    @Bind(R.id.clActivityLandingPageContainer)
    CoordinatorLayout containerCoordinatorLayout;
    @Bind(R.id.tvActivityLandingPageDescription)
    TextView descriptionTextView;
    @Bind(R.id.bActivityLandingPageArchive)
    Button archiveButton;
    @Bind(R.id.llActivityLandingPageLoaderLayout)
    LinearLayout loaderLayoutProgressBar;
    @Bind(R.id.ctlActivityLandingPage)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.nsvActivityLandingPage)
    NestedScrollView descriptionNestedScrollView;
    @Bind(R.id.aplActivityLangingPage)
    AppBarLayout appBarLayout;
    @Bind(R.id.pbActivityLandingPageLoader)
    ProgressBar progressBar;

    private SharedPreferences landingPageSharedPreferences;
    private boolean conferenceDownloadFlag = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Utils.setLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        ButterKnife.bind(this);

        landingPageSharedPreferences = getSharedPreferences(LANDING_PAGE_SHARED_PREFERENCES, MODE_PRIVATE);

        collapsingToolbarLayout.setTitle("TestWarez");
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);

        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.primary), PorterDuff.Mode.SRC_ATOP);

        compositeSubscription.add(new ReactiveNetwork()
                .observeConnectivity(this)
                .skip(1)
                .delay(4, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectivityStatus -> {
                    if (ConnectivityStatus.OFFLINE != connectivityStatus) {
                        Timber.i("startSyncService: ConnectivityStatusOnline");
                        startService(new Intent(this, SyncService.class));
                    }
                }, Throwable::printStackTrace));

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isVisible = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                float offset = Math.abs(verticalOffset / (float) scrollRange);

                if (offset <= 0.9f && isVisible) {
                    isVisible = false;
                    animateToolbarTitle(false);
                } else if (offset > 0.9f && !isVisible) {
                    isVisible = true;
                    animateToolbarTitle(true);
                }

                if (offset <= 0.7f) {
                    collapsingToolbarLayout.setTitleEnabled(false);
                }
            }
        });
    }

    private void animateToolbarTitle(final boolean show) {
        new CountDownTimer(300, 3) {
            @Override
            public void onTick(long l) {
                if (!show) {
                    collapsingToolbarLayout.setCollapsedTitleTextColor(
                            adjustAlpha(ContextCompat.getColor(getApplicationContext(), R.color.lp_toolbar_title),
                                    l / 300.0f));
                    if (l / 300.0f < 0.1f) {
                        collapsingToolbarLayout.setTitleEnabled(false);
                    }
                } else {
                    collapsingToolbarLayout.setTitleEnabled(true);
                    collapsingToolbarLayout.setCollapsedTitleTextColor(
                            adjustAlpha(ContextCompat.getColor(getApplicationContext(), R.color.lp_toolbar_title),
                                    1 - (l / 300.0f)));
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    private void makeLandingPageRequest() {
        if (Utils.isInternetConnection(this)) {
            Intent serviceIntent = new Intent(this, NetworkSevice.class);
            serviceIntent.setAction(NetworkSevice.REQUEST_LANDING_PAGE);
            startService(serviceIntent);
        } else {
            animateShowBar();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Timber.i("startSyncService: OnResume");
        Intent serviceIntent = new Intent(this, SyncService.class);
        startService(serviceIntent);

        ApplicationController.getBus().register(this);

        ViewTreeObserver bannerObserver = logoImageView.getViewTreeObserver();
        bannerObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                logoImageView.getViewTreeObserver().removeOnPreDrawListener(this);

                archiveButton.setEnabled(false);

                appBarLayout.setTranslationY(-appBarLayout.getHeight());
                descriptionNestedScrollView.setTranslationY(descriptionNestedScrollView.getHeight());
                archiveButton.setTranslationY(archiveButton.getHeight());

                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationController.getBus().unregister(this);
    }


    @OnClick(R.id.bActivityLandingPageArchive)
    void onArchiveClick() {
        Intent archiveIntent = new Intent(this, ArchiveActivity.class);
        archiveIntent.putExtra(ArchiveActivity.WITHOUT_NAVIGATION_DRAWER, true);
        startActivity(archiveIntent);
    }

    @Subscribe
    public void onSyncData(SyncDataEvent event) {
        switch (event.getSyncState()) {
            case SyncDataEvent.START_SYNCING:
                animateHideBar();
                break;
            case SyncDataEvent.IS_ACTIVE_CONFERENCE: {
                Timber.i("startAgendaActivity: SyncDataEvent - isActiveConference");
                startAgendaActivity();
            }
            break;

            case SyncDataEvent.START_SYNC_ACTIVE_CONFERENCE:
                break;
            case SyncDataEvent.DOWNLOAD_ACTIVE_CONFERENCE_CONTENT: {
                Timber.i("startAgendaActivity: SyncDataEvent - DownloadActiveConferenceContent");
                startAgendaActivity();
            }
            break;
            case SyncDataEvent.SYNC_ERROR: {
                compositeSubscription.add(Observable.defer(() -> Observable.just(ApplicationController.getActiveConference()))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(conference -> {
                            if (conference != null && conference.isSync()) {
                                Timber.i("startAgendaActivity: SyncDataEvent - SyncError");
                                startAgendaActivity();
                            } else {
                                makeLandingPageRequest();
                            }
                        }, Throwable::printStackTrace));
            }
            break;
            case SyncDataEvent.IS_LANDING_PAGE: {
                makeLandingPageRequest();
            }
            break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
    }

    private void startAgendaActivity() {
        Intent intent = new Intent(this, AgendaActivity.class);

        if (!Utils.isInternetConnection(this)) {
            intent.putExtra("init_snackbar", true);
        }

        startActivity(intent);
        finish();
    }

    @Subscribe
    public void onLandingPageResponse(LandingPageResponseEvent event) {
        SharedPreferences.Editor editor = landingPageSharedPreferences.edit();
        editor.putInt(LANDING_PAGE_ID, event.getResponse().getId());
        editor.apply();

        showLandingPageElement();
    }


    private void showLandingPageElement() {
        animateShowBar();
    }

    private void animateShowBar() {
        if (loaderLayoutProgressBar.getVisibility() == View.GONE) {
            return;
        }

        loaderLayoutProgressBar.setVisibility(View.GONE);

        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator appBarYAnim = ObjectAnimator.ofFloat(appBarLayout, View.TRANSLATION_Y, 0);
        ObjectAnimator appBarAlphaAnim = ObjectAnimator.ofFloat(appBarLayout, View.ALPHA, 0, 1f);
        ObjectAnimator nestedYAnim = ObjectAnimator.ofFloat(descriptionNestedScrollView, View.TRANSLATION_Y, 0);
        ObjectAnimator nestedAlphaAnim = ObjectAnimator.ofFloat(descriptionNestedScrollView, View.ALPHA, 0, 1f);
        ObjectAnimator bannerAlphaAnim = ObjectAnimator.ofFloat(logoImageView, View.ALPHA, 1, 0f);

        AnimatorSet.Builder animBuilder = animatorSet
                .play(appBarYAnim)
                .with(nestedYAnim)
                .with(nestedAlphaAnim)
                .with(appBarAlphaAnim)
                .with(bannerAlphaAnim);

        animatorSet.setDuration(ANIM_DURATION);

        compositeSubscription.add(Observable.defer(() -> Observable.just(Utils.getLandingPage(this)))
                .doOnError(Throwable::printStackTrace)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(lpd -> bindViews(lpd))
                .observeOn(Schedulers.newThread())
                .flatMap(lpd -> Observable.just(areArchiveConference()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(archiveExists -> {

                    if (archiveExists) {
                        ObjectAnimator archiveYAnim = ObjectAnimator.ofFloat(archiveButton, View.TRANSLATION_Y, 0);
                        ObjectAnimator archiveAlphaAnim = ObjectAnimator.ofFloat(archiveButton, View.ALPHA, 0, 1);
                        animBuilder.with(archiveAlphaAnim).with(archiveYAnim);

                        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) descriptionNestedScrollView.getLayoutParams();
                        lp.bottomMargin = archiveButton.getHeight();
                        descriptionNestedScrollView.setLayoutParams(lp);

                        archiveButton.setEnabled(true);
                        archiveButton.setVisibility(View.VISIBLE);
                    } else {
                        archiveButton.setVisibility(View.GONE);
                    }

                    animatorSet.start();
                }));
    }

    private void bindViews(LandingPageDescription lpd) {
        if (lpd != null) {
            collapsingToolbarLayout.setTitle(lpd.getTitle());
            descriptionTextView.setText(Html.fromHtml(lpd.getDescription()));
            descriptionTextView.setMovementMethod(LinkMovementMethod.getInstance());
            descriptionTextView.setLinksClickable(true);

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .preProcessor(bitmap -> {
                        float ratio = (float) bitmap.getHeight() / bitmap.getWidth();
                        int width = appBarLayout.getWidth();
                        int maxHeight = (int) (width * ratio);
                        return Bitmap.createScaledBitmap(bitmap, width, maxHeight, false);
                    })
                    .build();

            appBarLayout.setMinimumHeight((int) (getResources().getDisplayMetrics().density * 125));

            if (lpd.getBanner() != null) {
                ImageLoader.getInstance().loadImage(getString(R.string.endpoint) +
                                getResources().getString(R.string.images_url_default, lpd.getBanner().getFileId()),
                        options, new SimpleImageLoader() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                bannerImageView.setAlpha(0f);
                                bannerImageView.setImageBitmap(loadedImage);
                                bannerImageView.animate().alpha(1.0f).start();

                                appBarLayout.getLayoutParams().height = loadedImage.getHeight();
                            }
                        });
            }
        } else {
            descriptionTextView.setText(getString(R.string.internet_request));
        }
    }


    private boolean areArchiveConference() {
        boolean areArchiveConference = false;

        try {
            QueryBuilder<Conference, Integer> conferenceQB = ApplicationController.getDatabaseHelper().getConferenceDao().queryBuilder();
            Where where = conferenceQB.where();
            where.eq(Conference.STATUS_COLUMN, Conference.CONFERENCE_ARCHIVE);
            long count = conferenceQB.countOf();
            areArchiveConference = count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return areArchiveConference;
    }

    private void animateHideBar() {

        if (loaderLayoutProgressBar.getVisibility() == View.VISIBLE) {
            return;
        }

        archiveButton.setEnabled(false);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator appBarYAnim = ObjectAnimator.ofFloat(appBarLayout, View.TRANSLATION_Y, 0, -appBarLayout.getHeight());
        ObjectAnimator appBarAlphaAnim = ObjectAnimator.ofFloat(appBarLayout, View.ALPHA, 1, 0f);
        ObjectAnimator nestedYAnim = ObjectAnimator.ofFloat(descriptionNestedScrollView, View.TRANSLATION_Y, 0, descriptionNestedScrollView.getHeight());
        ObjectAnimator nestedAlphaAnim = ObjectAnimator.ofFloat(descriptionNestedScrollView, View.ALPHA, 1, 0f);
        ObjectAnimator logoAlphaAnim = ObjectAnimator.ofFloat(logoImageView, View.ALPHA, 0, 1f);
        ObjectAnimator archiveYAnim = ObjectAnimator.ofFloat(archiveButton, View.TRANSLATION_Y, 0, archiveButton.getHeight());
        ObjectAnimator archiveAlphaAnim = ObjectAnimator.ofFloat(archiveButton, View.ALPHA, 1, 0);

        AnimatorSet.Builder animBuider = animatorSet
                .play(appBarAlphaAnim)
                .with(appBarYAnim)
                .with(nestedAlphaAnim)
                .with(nestedYAnim)
                .with(logoAlphaAnim);

        if (archiveButton.getAlpha() == 1) {
            animBuider.with(archiveAlphaAnim).with(archiveYAnim);
        }

        animatorSet.setDuration(ANIM_DURATION);
        animatorSet.start();

        loaderLayoutProgressBar.setVisibility(View.VISIBLE);
    }
}
