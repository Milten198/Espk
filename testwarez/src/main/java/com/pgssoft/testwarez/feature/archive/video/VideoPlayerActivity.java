package com.pgssoft.testwarez.feature.archive.video;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.util.Util;
import com.jakewharton.rxbinding.view.RxView;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.database.model.Video;
import com.pgssoft.testwarez.util.exoplayer.DemoPlayer;
import com.pgssoft.testwarez.util.exoplayer.ExtractorRendererBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpodolak on 18.04.16.
 */
public class VideoPlayerActivity extends BaseActivity implements SurfaceHolder.Callback, DemoPlayer.Listener {

    public static final String VIDEO_LIST = "video_list";
    public static final String VIDEO_POSITION = "video_position";

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Bind(R.id.rlActivityVideoContainer)
    FrameLayout container;

    @Bind(R.id.pbActivityVideoLoader)
    ProgressBar loaderProgressBar;

    @Bind(R.id.tvActivityVideoError)
    TextView errorTextView;

    @Bind(R.id.surface)
    SurfaceView surface;

    @Bind(R.id.video_frame)
    AspectRatioFrameLayout videoFrame;

    private List<Video> videoList;
    private Video video;
    private int currentPosition;

    private Observable<Void> containerViewObservable;

    MediaController mediaController;

    private DemoPlayer player;
    private boolean playerNeedsPrepare;
    private String userAgent;
    private long playerPosition;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);
        ButterKnife.bind(this);

        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        containerViewObservable = RxView.clicks(container);

        loaderProgressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.primary), PorterDuff.Mode.SRC_ATOP);

        compositeSubscription.add(containerViewObservable
                .doOnError(Throwable::printStackTrace)
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    toggleControlsVisibility();
                }));

        videoList = getIntent().getParcelableArrayListExtra(VIDEO_LIST);
        currentPosition = getIntent().getIntExtra(VIDEO_POSITION, 0);

        video = videoList.get(currentPosition);

        surface.getHolder().addCallback(this);

        mediaController = new KeyCompatibleMediaController(this);
        mediaController.setAnchorView(container);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            onShown();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) {
            onShown();
        }
    }

    private void toggleControlsVisibility() {
        if (mediaController.isShowing()) {
            mediaController.hide();
        } else {
            mediaController.show();
        }
    }

    private void onShown() {
        if (player == null) {
            preparePlayer(true);
        } else {
            player.setBackgrounded(false);
        }
    }

    private void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            Observable.defer(() -> Observable.just(getRenderer()))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(rendererBuilder -> {
                        player = new DemoPlayer(rendererBuilder);
                        player.addListener(this);
                        player.seekTo(playerPosition);

                        playerNeedsPrepare = true;
                        mediaController.setMediaPlayer(player.getPlayerControl());
                        mediaController.setEnabled(true);
                        mediaController.setPrevNextListeners(next -> {
                            currentPosition = ++currentPosition % videoList.size();
                            video = videoList.get(currentPosition);
                            releasePlayer();
                            playerPosition = 0;
                            preparePlayer(true);
                        }, prev -> {
                            currentPosition = currentPosition == 0 ? videoList.size() - 1 : --currentPosition;
                            video = videoList.get(currentPosition);
                            releasePlayer();
                            playerPosition = 0;
                            preparePlayer(true);
                        });

                        prepare(playWhenReady);

                    }, throwable -> {
                        showErrorDialog();
                    });

        } else {
            prepare(playWhenReady);
        }
    }

    private void showErrorDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(com.pgssoft.testwarez.R.string.can_not_load_the_movie)
                .setPositiveButton(getString(R.string.close_drawer_action), (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                }).create();
        alertDialog.show();
    }

    private void prepare(boolean playWhenReady) {
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setSurface(surface.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    public DemoPlayer.RendererBuilder getRenderer() {
        if (video.getFile() == null) {
            return new ExtractorRendererBuilder(this, userAgent, Uri.parse(""));
        }

        Uri videoUri = Uri.parse(getString(R.string.endpoint) + "api/v1/streams/" + video.getFile().getFileId());
        return new ExtractorRendererBuilder(this, userAgent, videoUri);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            onHidden();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            onHidden();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
        compositeSubscription.unsubscribe();
        releasePlayer();
    }

    private void onHidden() {
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            playerPosition = player.getCurrentPosition();
            player = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState != ExoPlayer.STATE_IDLE) {
            errorTextView.setVisibility(View.GONE);
        }

        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                loaderProgressBar.setVisibility(View.VISIBLE);
                break;
            case ExoPlayer.STATE_ENDED:

                break;
            case ExoPlayer.STATE_IDLE:

                break;
            case ExoPlayer.STATE_PREPARING:
                loaderProgressBar.setVisibility(View.VISIBLE);
                break;
            case ExoPlayer.STATE_READY:
                loaderProgressBar.setVisibility(View.GONE);
                mediaController.show();
                surface.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onError(Exception e) {
        loaderProgressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        surface.setVisibility(View.GONE);
        mediaController.hide();
        e.printStackTrace();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        videoFrame.setAspectRatio(
                height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
    }

    private static final class KeyCompatibleMediaController extends MediaController {

        private static final int DISPLAY_DURATION = 6000;
        private MediaController.MediaPlayerControl playerControl;

        public KeyCompatibleMediaController(Context context) {
            super(context);


        }

        @Override
        public void hide() {
            animate().translationY(getHeight()).withEndAction(() -> super.hide());
        }

        @Override
        public void show() {
            super.show(DISPLAY_DURATION);
            animate().translationY(0).start();
        }


        @Override
        public void setMediaPlayer(MediaController.MediaPlayerControl playerControl) {
            super.setMediaPlayer(playerControl);
            this.playerControl = playerControl;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (playerControl.canSeekForward() && (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
                    || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() + 15000); // milliseconds
                    show();
                }
                return true;
            } else if (playerControl.canSeekBackward() && (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND
                    || keyCode == KeyEvent.KEYCODE_DPAD_LEFT)) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() - 5000); // milliseconds
                    show();
                }
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }
}
