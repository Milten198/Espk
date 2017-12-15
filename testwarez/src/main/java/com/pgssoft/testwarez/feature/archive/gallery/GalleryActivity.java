package com.pgssoft.testwarez.feature.archive.gallery;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.event.ArchiveGalleryClickEvent;
import com.pgssoft.testwarez.event.OpenGalleryImageEvent;
import com.pgssoft.testwarez.feature.archive.ArchiveActivity;
import com.pgssoft.testwarez.database.model.GalleryBEFile;
import com.pgssoft.testwarez.util.helper.AnimatorHelperListener;
import com.pgssoft.testwarez.util.helper.PageChangeHelper;
import com.pgssoft.testwarez.widget.FixedViewPager;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by brosol on 2016-04-06.
 */
public class GalleryActivity extends BaseActivity {

    private static final String PHOTO_LEFT = "photo_left";
    private static final String PHOTO_TOP = "photo_top";
    private static final String PHOTO_WIDTH = "photo_width";
    private static final String PHOTO_HEIGHT = "photo_height";
    private static final String PHOTO_BITMAP = "photo_bitmap";
    private static final String PHOTO_BUNDLE = "photo_bundle";
    private static final int DELAY_TIME = 3000;

    List<GalleryBEFile> images;
    private int position = -1;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.rlActivityGalleryContainer)
    RelativeLayout containerGallery;

    @Bind(R.id.gallery_image_counter)
    TextView galleryImageCounter;

    @Bind(R.id.gallery_navigation_layout)
    LinearLayout navigationLinearLayout;

    @Bind(R.id.vpActivityGallery)
    FixedViewPager galleryViewPager;


    private ColorDrawable backgroundColor;
    private boolean landscape;
    private static Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_gallery);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_activity_archive));
        toolbar.setNavigationOnClickListener(view -> finish());

        images = getIntent().getParcelableArrayListExtra("images");
        position = getIntent().getIntExtra("position", -1);

        images.add(0, images.get(images.size() - 1));
        images.add(images.size(), images.get(1));

        GalleryAdapter adapter = new GalleryAdapter(this, images);

        galleryViewPager.setAdapter(adapter);
        galleryViewPager.setCurrentItem(position);
        galleryViewPager.addOnPageChangeListener(new PageChangeHelper(){

            @Override
            public void onPageSelected(int position) {
                galleryImageCounter.setText(String.format("%2d z %2d", position, images.size() - 2));

                if(position == images.size() - 1) {
                    galleryViewPager.setCurrentItem(1, false);
                } else if(position == 0) {
                    galleryViewPager.setCurrentItem(images.size() - 2, false);
                }
            }
        });

        galleryImageCounter.setText(String.format("%2d z %2d", position, images.size() - 2));

        backgroundColor = new ColorDrawable(Color.BLACK);
        containerGallery.setBackground(backgroundColor);

        galleryViewPager.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                galleryViewPager.getViewTreeObserver().removeOnPreDrawListener(this);
                enterAnimation();
                return true;
            }
        });

        runnable = () -> {
            if(landscape) {
                hideNavigationLayout();
                stopHandler();
            }
        };

        startHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Subscribe
    public void onArchiveGalleryClickEvent(ArchiveGalleryClickEvent event) {
        if(landscape) {
            if(navigationLinearLayout.getVisibility() == View.VISIBLE) {
                hideNavigationLayout();
                stopHandler();
            } else {
                showNavigationLayout();
                startHandler();
            }
        }
    }

    @Override
    public void finish() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setResult(RESULT_OK);
        exitAnimation();
    }

    private void enterAnimation() {
        ObjectAnimator animAlpha = ObjectAnimator.ofFloat(galleryViewPager, View.ALPHA, 0, 1);
        ObjectAnimator animToolbar = ObjectAnimator.ofFloat(toolbar, View.ALPHA, 0, 1);
        ObjectAnimator animBackground = ObjectAnimator.ofInt(backgroundColor, "alpha", 0, 255);
        ObjectAnimator animNavigation = ObjectAnimator.ofFloat(navigationLinearLayout, View.ALPHA, 0, 1);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animAlpha, animToolbar, animBackground, animNavigation);
        animatorSet.setDuration(500);
        animatorSet.start();
    }

    private void exitAnimation() {
        ObjectAnimator animAlpha = ObjectAnimator.ofFloat(galleryViewPager, View.ALPHA, 1, 0);
        ObjectAnimator animToolbar = ObjectAnimator.ofFloat(toolbar, View.ALPHA, 1, 0);
        ObjectAnimator animBackground = ObjectAnimator.ofInt(backgroundColor, "alpha", 255, 0);
        ObjectAnimator animNavigation = ObjectAnimator.ofFloat(navigationLinearLayout, View.ALPHA, 1, 0);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animAlpha, animToolbar, animBackground, animNavigation);
        animatorSet.setDuration(500);
        animatorSet.addListener(new AnimatorHelperListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                GalleryActivity.super.finish();
                overridePendingTransition(0, 0);
            }
        });
        animatorSet.start();
    }

    @OnClick(R.id.gallery_image_next)
    protected void onGalleryNextButtonClick() {

        int position = galleryViewPager.getCurrentItem();
        int newPosition = position == images.size()-1 ? position : ++position ;

        galleryViewPager.setCurrentItem(newPosition);

        restartHandler();
    }

    @OnClick(R.id.gallery_image_previous)
    protected void onGalleryPreviousButtonClick() {
            int position = galleryViewPager.getCurrentItem();

            int newPosition = position == 0 ? position : --position;
            galleryViewPager.setCurrentItem(newPosition);

            restartHandler();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch(newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                toolbar.setVisibility(View.VISIBLE);
                if(navigationLinearLayout.getVisibility() == View.GONE) {
                    showNavigationLayout();
                }
                landscape = false;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                toolbar.setVisibility(View.GONE);
                landscape = true;
                startHandler();
                break;
        }
    }

    public static void open(Context context, OpenGalleryImageEvent event) {

        try {
            Intent intent = new Intent(context, GalleryActivity.class);
            intent.putParcelableArrayListExtra("images", new ArrayList<>(event.getImageList()));
            intent.putExtra("position", event.getPosition());

            Bundle photoData = new Bundle();

            intent.putExtra(PHOTO_BUNDLE, photoData);

            ((Activity) context).startActivityForResult(intent, ArchiveActivity.GALLERY_REQUEST);
            ((Activity) context).overridePendingTransition(0, 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startHandler() {
        if(landscape) {
            handler.postDelayed(runnable, DELAY_TIME);
        }
    }

    private void stopHandler() {
        handler.removeCallbacks(runnable);
    }

    private void restartHandler() {
        if(landscape) {
            stopHandler();
            startHandler();
        }
    }

    private void showNavigationLayout() {
        ViewCompat.animate(navigationLinearLayout)
                .alpha(1.0f)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                        navigationLinearLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                    }

                    @Override
                    public void onAnimationCancel(View view) {

                    }
                })
                .start();
    }

    private void hideNavigationLayout() {
        ViewCompat.animate(navigationLinearLayout)
                .alpha(0.0f)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {

                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        navigationLinearLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(View view) {

                    }
                })
                .start();
    }
}
