package com.pgssoft.testwarez.feature.archive;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.pwittchen.reactivenetwork.library.ConnectivityStatus;
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseNavigationDrawerActivity;
import com.pgssoft.testwarez.core.FilterActivity;
import com.pgssoft.testwarez.event.ArchiveVideoClickEvent;
import com.pgssoft.testwarez.event.OpenArchiveEventEvent;
import com.pgssoft.testwarez.event.OpenGalleryImageEvent;
import com.pgssoft.testwarez.event.SearchEvent;
import com.pgssoft.testwarez.event.SyncDataEvent;
import com.pgssoft.testwarez.feature.archive.event.ArchiveEventActivity;
import com.pgssoft.testwarez.feature.archive.gallery.GalleryActivity;
import com.pgssoft.testwarez.feature.archive.video.VideoPlayerActivity;
import com.pgssoft.testwarez.feature.archive.video.YoutubePlayerActivity;
import com.pgssoft.testwarez.service.SyncService;
import com.pgssoft.testwarez.util.Observables;
import com.pgssoft.testwarez.util.Utils;
import com.pgssoft.testwarez.util.helper.PageChangeHelper;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dpodolak on 08.03.16.
 */
public class ArchiveActivity extends BaseNavigationDrawerActivity {

    public static final String WITHOUT_NAVIGATION_DRAWER = "without_naviagation_drawer";

    public static final int EVENT_POSITION = 0;
    public static final int GALLERY_POSITION = 1;
    public static final int VIDEO_POSITION = 2;

    public static final int GALLERY_REQUEST = 101;
    public static final int FILTER_REQUEST = 102;

    @Bind(R.id.vpActivityArchive)
    ViewPager archivePager;
    @Bind(R.id.archive_progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.progress_bar_container)
    RelativeLayout progressBarContainer;
    @Bind(R.id.archive_progress_message)
    TextView archiveProgressMessage;

    private ArchiveFragmentAdapter archiveFragmentAdapter;
    private int VIDEO_REQUEST_CODE = 150;
    private ArchiveVideoClickEvent tempVideoEvent;
    boolean syncedFlag;
    boolean shouldMenuBeVisible = true;
    private Subscription subscription;

    @Override
    public void updateView() {
        if (getMenu() != null) {
            getMenu().setGroupVisible(0, true);
        }

        if (archiveFragmentAdapter != null) {
            setTabLayoutVisibility(View.VISIBLE);
            archiveFragmentAdapter.update();
            progressBarContainer.setVisibility(View.GONE);
        } else {
            //archiveFragmentAdapter is null when, sync will be finished
            setViewPager();
        }
    }

    @Override
    public SearchEvent.SEARCH_TYPE getSerachType() {
        if (archivePager.getCurrentItem() == EVENT_POSITION) {
            return SearchEvent.SEARCH_TYPE.ARCHIVE_EVENTS;
        } else if (archivePager.getCurrentItem() == VIDEO_POSITION) {
            return SearchEvent.SEARCH_TYPE.ARCHIVE_VIDEOS;
        }

        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);
        ButterKnife.bind(this);

        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.primary), PorterDuff.Mode.SRC_ATOP);

        setMenu(R.menu.filter_main_menu);

        if (savedInstanceState == null) {
            boolean withoutNavigationDrawer = getIntent().getBooleanExtra(WITHOUT_NAVIGATION_DRAWER, false);

            if (withoutNavigationDrawer) {
                disableNavigationDrawer();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncedFlag = !Utils.isUpdateInProgress() && Utils.isAllArchiveDataDownloaded();

        if (syncedFlag && archiveFragmentAdapter == null) {
            setViewPager();
        } else if (!Utils.isUpdateInProgress() && !Utils.isAllArchiveDataDownloaded()) {
            if (Utils.isInternetConnection(this)) {
                OnSyncEvent(new SyncDataEvent(SyncDataEvent.START_SYNCING));
                startService(new Intent(this, SyncService.class));
            } else {
                progressBar.setVisibility(View.INVISIBLE);
                setMessageLayoutParams(false);
                archiveProgressMessage.setText(getText(R.string.archive_loading_no_internet));
                subscription = new ReactiveNetwork()
                        .observeConnectivity(this)
                        .skip(1)
                        .delay(4, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(connectivityStatus -> {
                            if (ConnectivityStatus.OFFLINE != connectivityStatus) {
                                OnSyncEvent(new SyncDataEvent(SyncDataEvent.START_SYNCING));
                                startService(new Intent(this, SyncService.class));
                            }
                        }, Throwable::printStackTrace);
                compositeSubscription.add(subscription);
            }
        }

        setFilterIcon();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case FILTER_REQUEST:
                updateView();
                setFilterIcon();
                break;
            case GALLERY_REQUEST:
                archivePager.setCurrentItem(1, false);
                break;
        }
    }

    private void setViewPager() {
        Observables.getInstance().getConferenceObservables().getArchiveConferences().count()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer == 0) {
                        shouldMenuBeVisible = false;
                        setMessageLayoutParams(true);
                        archiveProgressMessage.setText(getText(R.string.no_archive_conferences));
                        progressBar.setVisibility(View.INVISIBLE);
                    } else {
                        shouldMenuBeVisible = true;
                        initViewPager();
                    }
                }, Throwable::printStackTrace);
    }

    private void initViewPager() {
        if (getMenu() != null) {
            getMenu().setGroupVisible(0, shouldMenuBeVisible);
        }

        archiveFragmentAdapter = new ArchiveFragmentAdapter(this, getSupportFragmentManager());
        archivePager.setAdapter(archiveFragmentAdapter);
        Utils.setIsAllArchiveDataDownloaded(true);

        setTabLayoutVisibility(View.VISIBLE);
        setTabLayoutPager(archivePager);
        setTabLayoutMode(TabLayout.MODE_FIXED);

        archivePager.addOnPageChangeListener(new PageChangeHelper() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (getMenu() != null) {
                    switch (position) {
                        case EVENT_POSITION:
                            getMenu().findItem(R.id.main_menu_filter).setVisible(true);
                            getMenu().findItem(R.id.main_menu_search).setVisible(true);
                            break;
                        case VIDEO_POSITION:
                            getMenu().findItem(R.id.main_menu_filter).setVisible(false);
                            getMenu().findItem(R.id.main_menu_search).setVisible(true);
                            break;
                        default:
                            getMenu().findItem(R.id.main_menu_filter).setVisible(false);
                            getMenu().findItem(R.id.main_menu_search).setVisible(false);
                            break;
                    }
                }
                disableSearchBar();
            }
        });

        progressBarContainer.setVisibility(View.GONE);
    }

    private void setMessageLayoutParams(boolean center) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (center) {
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
        } else {
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.addRule(RelativeLayout.BELOW, R.id.archive_progress_bar);
        }
        archiveProgressMessage.setLayoutParams(params);
    }

    @Subscribe
    public void OnSyncEvent(SyncDataEvent event) {
        switch (event.getSyncState()) {
            case SyncDataEvent.START_SYNCING:
                if (getMenu() != null) {
                    getMenu().setGroupVisible(0, false);
                }
                setTabLayoutVisibility(View.GONE);
                progressBarContainer.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                setMessageLayoutParams(false);
                archiveProgressMessage.setText(getString(R.string.archive_loading_message));
                break;
            case SyncDataEvent.SYNC_ERROR:
                if (getMenu() != null) {
                    getMenu().setGroupVisible(0, false);
                }
                setTabLayoutVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
                setMessageLayoutParams(true);
                archiveProgressMessage.setText(getString(R.string.archive_loading_no_internet));
                break;
            case SyncDataEvent.SYNC_ARCHIVE_COMPLETE:
                setViewPager();
                break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setFilterIcon();

        if (!syncedFlag || !shouldMenuBeVisible) {
            menu.setGroupVisible(0, false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void setFilterIcon() {
        Menu menu = getMenu();
        if (menu != null) {
            MenuItem filterItem = menu.findItem(R.id.main_menu_filter);
            if (filterItem != null) {
                filterItem.setIcon(!Utils.getConferenceFilter().isEmpty() || !Utils.getArchiveFileTypesFilter().isEmpty() ? R.drawable.filter_icon_active : R.drawable.filter_icon);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_menu_filter) {
            Intent filterIntent = new Intent(this, FilterActivity.class);
            filterIntent.putExtra(FilterActivity.REQUEST_CODE, FILTER_REQUEST);
            startActivityForResult(filterIntent, FILTER_REQUEST);
            overridePendingTransition(R.anim.top_translate_in, R.anim.top_translate_out);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onOpenGalleryEvent(OpenGalleryImageEvent event) {
        GalleryActivity.open(this, event);
    }

    @Subscribe
    public void onOpenArchiveEventEvent(OpenArchiveEventEvent event) {
        ArchiveEventActivity.open(this, event.getEvent(), event.getTitle());
    }

    @Subscribe
    public void onArchiveVideoClickEvent(ArchiveVideoClickEvent event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requiresPermission()) {
            tempVideoEvent = event;
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, VIDEO_REQUEST_CODE);
            return;
        }

        if (event.getVideo().isYoutubeVideo()) {
            Intent youtubeIntent = new Intent(this, YoutubePlayerActivity.class);
            youtubeIntent.putExtra(YoutubePlayerActivity.VIDEO_EXTRA, event.getVideo());
            startActivity(youtubeIntent);
        } else {
            Intent videoIntent = new Intent(this, VideoPlayerActivity.class);
            videoIntent.putParcelableArrayListExtra(VideoPlayerActivity.VIDEO_LIST, new ArrayList<>(event.getVideoList()));
            videoIntent.putExtra(VideoPlayerActivity.VIDEO_POSITION, event.getPosition());
            startActivity(videoIntent);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean requiresPermission() {
        return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == VIDEO_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent videoIntent = new Intent(this, VideoPlayerActivity.class);
                videoIntent.putParcelableArrayListExtra(VideoPlayerActivity.VIDEO_LIST, new ArrayList<>(tempVideoEvent.getVideoList()));
                videoIntent.putExtra(VideoPlayerActivity.VIDEO_POSITION, tempVideoEvent.getPosition());
                startActivity(videoIntent);
            }
        }
    }
}