package com.pgssoft.testwarez.feature.archive.video;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.database.model.Video;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by brosol on 2017-08-09.
 */

public class YoutubePlayerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    public static final String VIDEO_EXTRA = "video_extra";

    @Bind(R.id.youtube_player_view)
    YouTubePlayerView youTubePlayerView;

    private Video video;
    private Pattern pattern = Pattern.compile("(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_youtube_player);

        ButterKnife.bind(this);

        video = getIntent().getParcelableExtra(VIDEO_EXTRA);
        youTubePlayerView.initialize(getResources().getString(R.string.google_maps_key), this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
            Matcher matcher = pattern.matcher(video.getUrl());

            if (matcher.find()) {
                youTubePlayer.loadVideo(matcher.group(0));
                youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage(com.pgssoft.testwarez.R.string.can_not_load_the_movie)
                        .setPositiveButton(getString(R.string.close_drawer_action), (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        }).create();
                alertDialog.show();
            }
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        youTubeInitializationResult.getErrorDialog(this, 0);
    }
}
