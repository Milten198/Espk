package com.pgssoft.testwarez.feature.archive.event;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.feature.archive.video.VideoPlayerActivity;
import com.pgssoft.testwarez.feature.archive.video.YoutubePlayerActivity;
import com.pgssoft.testwarez.feature.event.EventActivity;
import com.pgssoft.testwarez.feature.speaker.detail.PersonActivity;
import com.pgssoft.testwarez.event.ClickOnFileEvent;
import com.pgssoft.testwarez.event.OpenPersonEvent;
import com.pgssoft.testwarez.feature.agenda.AgendaActivity;
import com.pgssoft.testwarez.database.model.Archive;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.database.model.Video;
import com.pgssoft.testwarez.service.FileDownloadService;
import com.pgssoft.testwarez.util.helper.AnimatorHelperListener;
import com.squareup.otto.Subscribe;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by brosol on 2016-04-07.
 */
public class ArchiveEventActivity extends BaseActivity {

    public static final String EVENT_ID = "event_id";
    public static final int REFRESH_NEEDED = 1;
    private static final int OPEN_PERSON_REQUEST = 13;

    public static final String ANIMATION_BUNDLE = "animation_bundle";
    public static final String TITLE_LEFT = "title_left";
    public static final String TITLE_TOP = "title_top";
    public static final String TITLE_WIDTH = "title_width";
    public static final String TITLE_HEIGHT = "title_height";
    public static final String TITLE_COLOR = "title_color";
    private static final int REQUEST_STORAGE_PERMISSION = 105;

    private static final long ANIM_DURATION = 300;

    private ArchiveEventDataAdapter adapter;

    private int eventId = -1;

    private Point screenSize = new Point();


    @Bind(R.id.toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.nestedscrollview)
    protected RecyclerView recyclerView;
    @Bind(R.id.collapsing_toolbar)
    protected CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.clActivityEvent)
    protected CoordinatorLayout containerCoordinatorLayout;
    @Bind(R.id.app_bar_layout)
    protected AppBarLayout appBarLayout;

    private ColorDrawable mBackground;


    private ClickOnFileEvent checkPermissionEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_event);
        ButterKnife.bind(this);

        if (getIntent().getExtras() != null) {
            eventId = getIntent().getExtras().getInt(EVENT_ID, -1);
        }
        if (eventId == -1) {
            finish();
            return;
        }

        getWindowManager().getDefaultDisplay().getSize(screenSize);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());


        mBackground = new ColorDrawable(Color.WHITE);
        containerCoordinatorLayout.setBackground(mBackground);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        appBarLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                appBarLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                enterAnimation();
                return true;
            }
        });

        getSupportActionBar().setTitle("");

        compositeSubscription.add(
                Observable.defer(() -> Observable.fromCallable(() -> {
                    try {
                        return ApplicationController.getDatabaseHelper().getEventDao().queryForId(eventId);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }))
                        .doOnError(Throwable::printStackTrace)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(event -> {
                            ArrayList<Speaker> speakers = event.getAllSpeakers();
                            adapter = new ArchiveEventDataAdapter(this, event, speakers, event.getAllArchives(), screenSize);
                            recyclerView.setAdapter(adapter);
                            collapsingToolbarLayout.setTitle(event.getTitle());
                            new Handler().post(() -> recyclerView.scrollToPosition(0));
                        }));
    }

    private void enterAnimation() {

        appBarLayout.setAlpha(0f);
        recyclerView.setTranslationY(recyclerView.getHeight());
        mBackground.setAlpha(0);


        containerCoordinatorLayout.setClipChildren(false);
        ObjectAnimator appBarTop = ObjectAnimator.ofFloat(appBarLayout, View.ALPHA, 0f, 1f);
        ObjectAnimator bfAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        ObjectAnimator recyclerAnim = ObjectAnimator.ofFloat(recyclerView, View.TRANSLATION_Y, recyclerView.getHeight(), 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(recyclerAnim)
                .after(50)
                .after(appBarTop)
                .after(bfAnim);
        animatorSet.setDuration(ANIM_DURATION);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.addListener(new AnimatorHelperListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                containerCoordinatorLayout.setClipChildren(true);
            }
        });
        animatorSet.start();
    }


    private void exitAnimation() {

        appBarLayout.setExpanded(true, false);

        containerCoordinatorLayout.setClipChildren(false);
        ObjectAnimator appBarTop = ObjectAnimator.ofFloat(appBarLayout, View.ALPHA, 0f);
        ObjectAnimator bfAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
        ObjectAnimator recyclerAnim = ObjectAnimator.ofFloat(recyclerView, View.TRANSLATION_Y, recyclerView.getHeight());

        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.play(recyclerAnim)
                .before(bfAnim)
                .before(appBarTop);
        animatorSet.setDuration(ANIM_DURATION);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.addListener(new AnimatorHelperListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ArchiveEventActivity.super.finish();
                ArchiveEventActivity.this.overridePendingTransition(0, 0);
            }
        });
        animatorSet.start();
    }

    @Override
    public void finish() {
        exitAnimation();
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

    @Subscribe
    public void onOpenSpeakerEvent(OpenPersonEvent event) {
        PersonActivity.open(this, event);
    }

    @Subscribe
    public void onOpenFileEvent(ClickOnFileEvent event) {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);

            checkPermissionEvent = event;
            return;
        }

        if (event.getArchive().getType().equals(Archive.TYPE_UNDEFINED)) {
            // TODO: handle undefined file type
            return;
        }

        if (event.getArchive().getType().equals(Archive.TYPE_VIDEO)) {
            initVideoPlayerActivity(event.getArchive());
            return;
        }

        if (event.getArchive().getType().equals(Archive.TYPE_LINK)) {
            initBrowser(event.getArchive().getUrl());
            return;
        }

        if (isFileDownloading()) {
            return;
        }

        final int id = event.getArchive().getId();
        final String url = event.getArchive().getFullUrl(this);
        final String fileName = event.getArchive().getFileNameWithExtension(this);
        final String mimeType = event.getArchive().getMimeType();

        if (!isFileOnExternalStorage(fileName)) {
            isDownloadingStarted = true;
            Intent intent = new Intent(this, FileDownloadService.class);
            intent.putExtra("url", url);
            intent.putExtra("fileName", fileName);
            intent.putExtra("mimeType", mimeType);
            startService(intent);
        } else {
            File documentFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
            openFile(Uri.fromFile(documentFile), mimeType);
            return;
        }

        Snackbar.make(
                ButterKnife.findById(this, R.id.clActivityEvent),
                getString(R.string.downloading_file) + " \"" + fileName + "\"",
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onOpenFileEvent(checkPermissionEvent);
                }
            }
            break;
        }
    }

    private void initVideoPlayerActivity(Archive archive) {
        Video video = new Video(archive);

        if (video.isYoutubeVideo()) {
            Intent youtubeIntent = new Intent(this, YoutubePlayerActivity.class);
            youtubeIntent.putExtra(YoutubePlayerActivity.VIDEO_EXTRA, video);
            startActivity(youtubeIntent);
        } else {
            ArrayList<Video> videoList = new ArrayList<>();
            videoList.add(video);
            Intent videoIntent = new Intent(this, VideoPlayerActivity.class);
            videoIntent.putParcelableArrayListExtra(VideoPlayerActivity.VIDEO_LIST, videoList);
            videoIntent.putExtra(VideoPlayerActivity.VIDEO_POSITION, 0);
            startActivity(videoIntent);
        }
    }

    private void initBrowser(String url) {
        if (url != null && !url.isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(url));
            startActivity(browserIntent);
        }
    }

    private boolean isFileOnExternalStorage(String fileName) {

        String downloadDirPath =
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        File dirPath = new File(downloadDirPath);

        if (!dirPath.exists()) {
            dirPath.mkdirs();
        }

        return new File(downloadDirPath, fileName).exists();
    }


    @Override
    protected void fileHasBeenDownloaded(long id) {
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        Uri uri;
        String mimeType;

        uri = downloadManager.getUriForDownloadedFile(id);
        mimeType = downloadManager.getMimeTypeForDownloadedFile(id);

        openFile(uri, mimeType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_PERSON_REQUEST && resultCode == PersonActivity.REFRESH_NEEDED) {
            setResult(REFRESH_NEEDED);
        }
    }

    public static void open(Activity activity, Event event, TextView title) {
        Intent intent = new Intent(activity, ArchiveEventActivity.class);
        Bundle bundle = new Bundle();

        intent.putExtra(EventActivity.EVENT_ID, event.getId());

        int[] titleLocation = new int[2];
        title.getLocationOnScreen(titleLocation);

        bundle.putInt(TITLE_LEFT, titleLocation[0]);
        bundle.putInt(TITLE_TOP, titleLocation[1]);
        bundle.putInt(TITLE_WIDTH, title.getWidth());
        bundle.putInt(TITLE_HEIGHT, title.getHeight());
        bundle.putInt(TITLE_COLOR, title.getCurrentTextColor());

        intent.putExtra(ANIMATION_BUNDLE, bundle);
        activity.startActivityForResult(intent, AgendaActivity.OPEN_EVENT_REQUEST);
        activity.overridePendingTransition(0, 0);
    }
}
