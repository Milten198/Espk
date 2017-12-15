package com.pgssoft.testwarez.feature.speaker.detail;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.event.FavoriteRefreshEvent;
import com.pgssoft.testwarez.event.OpenEventEvent;
import com.pgssoft.testwarez.event.OpenPersonEvent;
import com.pgssoft.testwarez.feature.event.EventActivity;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.database.model.Staff;
import com.pgssoft.testwarez.util.FinishWithoutAnim;
import com.pgssoft.testwarez.util.helper.AnimatorHelperListener;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PersonActivity extends BaseActivity implements FinishWithoutAnim {
    private static final int OPEN_EVENT_REQUEST = 12;
    public static final String PERSON_ID = "person_id";
    public static final String PERSON_TYPE = "person_type";
    public static final int REFRESH_NEEDED = 1;

    public static final String PHOTO_LEFT = "photo_left";
    public static final String PHOTO_TOP = "photo_top";
    public static final String PHOTO_WIDTH = "photo_width";
    public static final String PHOTO_HEIGHT = "photo_height";
    public static final String PHOTO_BITMAP = "photo_bitmap";

    public static final String NAME_COLOR = "name_color";
    public static final String NAME_LEFT = "name_left";
    public static final String NAME_TOP = "name_top";
    public static final String NAME_WIDTH = "name_width";
    public static final String NAME_HEIGHT = "name_height";

    public static final String ANIMATION_BUNDLE = "animation_bundle";
    private static final long ANIM_DURATION = 300;


    private SpeakerDataAdapter speakerDataAdapter;
    private StaffDataAdapter staffDataAdapter;

    private int personId = -1;
    private int personType;
    private Speaker speaker;
    private Staff staff;

    private Bundle animationBundle;
    private float deltaX, deltaY, scaleX, scaleY;

    private Point screenSize = new Point();

    @Bind(R.id.toolbar)
    protected Toolbar toolbar;

    @Bind(R.id.collapsing_toolbar)
    protected net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout collapsingToolbar;

    @Bind(R.id.photo_mini)
    protected ImageView photoMini;

    @Bind(R.id.nestedscrollview)
    protected RecyclerView recyclerView;

    @Bind(R.id.app_bar_layout)
    protected AppBarLayout appBarLayout;

    private ColorDrawable mBackground;

    @Bind(R.id.clActivitySpeaker)
    protected CoordinatorLayout containerCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker);
        ButterKnife.bind(this);

        if (getIntent().getExtras() != null) {
            personId = getIntent().getExtras().getInt(PERSON_ID, -1);
            personType = getIntent().getExtras().getInt(PERSON_TYPE, -1);
        }
        if (personId == -1 || personType == -1) {
            finish();
        }

        animationBundle = getIntent().getBundleExtra(ANIMATION_BUNDLE);

        getWindowManager().getDefaultDisplay().getSize(screenSize);

        mBackground = new ColorDrawable(Color.WHITE);
        containerCoordinatorLayout.setBackground(mBackground);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.user_placeholder)
                .showImageOnFail(R.drawable.user_placeholder)
                .showImageOnLoading(R.drawable.user_placeholder)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> {
            ApplicationController.getParentManager().runParentActivity(this);
        });

        collapsingToolbar.setTitle("");
        getSupportActionBar().setTitle("");
        byte[] b = getIntent().getBundleExtra(ANIMATION_BUNDLE).getByteArray(PHOTO_BITMAP);
        Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
        photoMini.setImageBitmap(bmp);

        photoMini.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                photoMini.getViewTreeObserver().removeOnPreDrawListener(this);
                prepareViewsToAnim();
                enterAnimation();
                return true;
            }
        });

        compositeSubscription.add(
                Observable.just(personType)
                        .observeOn(Schedulers.newThread())
                        .flatMap(type -> {
                            try {
                                if (type == 0) {
                                    return Observable.just(ApplicationController.getDatabaseHelper().getSpeakerDao().queryForId(personId));
                                } else {
                                    return Observable.just(ApplicationController.getDatabaseHelper().getStaffDao().queryForId(personId));
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            return Observable.empty();
                        }).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> {

                            int photoId = -1;

                            if (o instanceof Speaker) {
                                speaker = (Speaker) o;
                                speakerDataAdapter = new SpeakerDataAdapter(this, speaker, screenSize, getIntent().getBooleanExtra("is_archive", false));
                                recyclerView.setAdapter(speakerDataAdapter);

                                collapsingToolbar.setTitle(speaker.getFullName());

                                if (speaker.getPhoto() != null) {
                                    photoId = speaker.getPhoto().getFileId();
                                }
                            } else if (o instanceof Staff) {
                                staff = (Staff) o;
                                staffDataAdapter = new StaffDataAdapter(this, staff, screenSize, getIntent().getBooleanExtra("is_archive", false));
                                staffDataAdapter.setCompositeSubscription(compositeSubscription);
                                recyclerView.setAdapter(staffDataAdapter);

                                collapsingToolbar.setTitle(staff.getName());

                                if (staff.getPhoto() != null) {
                                    photoId = staff.getPhoto().getFileId();
                                }
                            }

                            ImageLoader.getInstance().displayImage(getResources().getString(R.string.endpoint) + getResources().getString(R.string.images_url_default, photoId), photoMini, options);

                            new Handler().post(() -> recyclerView.scrollToPosition(0));
                        }, Throwable::printStackTrace));

    }

    private void enterAnimation() {

        AnimatorSet animatorSet = new AnimatorSet();


        ObjectAnimator photAnimatorLeft = ObjectAnimator.ofFloat(photoMini, View.TRANSLATION_X, 0f);
        ObjectAnimator photAnimatorTop = ObjectAnimator.ofFloat(photoMini, View.TRANSLATION_Y, 0f);
        ObjectAnimator photAnimatorWidth = ObjectAnimator.ofFloat(photoMini, View.SCALE_X, 1f);
        ObjectAnimator photAnimatorHeight = ObjectAnimator.ofFloat(photoMini, View.SCALE_Y, 1f);
        ObjectAnimator appBarAnimation = ObjectAnimator.ofFloat(appBarLayout, View.ALPHA, 1f);
        ObjectAnimator recycleAnimationY = ObjectAnimator.ofFloat(recyclerView, View.TRANSLATION_Y, 0);
        ObjectAnimator bfAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);

        animatorSet.play(photAnimatorLeft)
                .with(photAnimatorTop)
                .with(photAnimatorHeight)
                .with(photAnimatorWidth)
                .with(recycleAnimationY)
                .after(50)
                .after(appBarAnimation)
                .after(bfAnim);


        animatorSet.setDuration(ANIM_DURATION);
        animatorSet.start();
    }

    private void prepareViewsToAnim() {
        int[] displayLocation = new int[2];
        photoMini.getLocationOnScreen(displayLocation);
        deltaX = animationBundle.getInt(PHOTO_LEFT) - displayLocation[0];
        deltaY = animationBundle.getInt(PHOTO_TOP) - displayLocation[1];
        scaleX = (float) animationBundle.getInt(PHOTO_WIDTH) / photoMini.getWidth();
        scaleY = (float) animationBundle.getInt(PHOTO_HEIGHT) / photoMini.getHeight();


        mBackground.setAlpha(0);

        photoMini.setPivotX(0f);
        photoMini.setPivotY(0f);

        photoMini.setTranslationY(deltaY);
        photoMini.setTranslationX(deltaX);
        photoMini.setScaleX(scaleX);
        photoMini.setScaleY(scaleY);
        appBarLayout.setAlpha(0f);
        recyclerView.setTranslationY(recyclerView.getHeight());
    }

    private void exitAnimation() {

        AnimatorSet animatorSet = new AnimatorSet();

        appBarLayout.setExpanded(true, false);

        ObjectAnimator photAnimatorLeft = ObjectAnimator.ofFloat(photoMini, View.TRANSLATION_X, deltaX);
        ObjectAnimator photAnimatorTop = ObjectAnimator.ofFloat(photoMini, View.TRANSLATION_Y, deltaY);
        ObjectAnimator photAnimatorWidth = ObjectAnimator.ofFloat(photoMini, View.SCALE_X, scaleX);
        ObjectAnimator photAnimatorHeight = ObjectAnimator.ofFloat(photoMini, View.SCALE_Y, scaleY);
        ObjectAnimator appBarAnimation = ObjectAnimator.ofFloat(appBarLayout, View.ALPHA, 0f);

        ObjectAnimator bfAnim = ObjectAnimator.ofInt(mBackground, "alpha", 255, 0);
        ObjectAnimator recycleAnimationY = ObjectAnimator.ofFloat(recyclerView, View.TRANSLATION_Y, 0f, recyclerView.getHeight());

        animatorSet.play(photAnimatorLeft)
                .with(photAnimatorTop)
                .with(photAnimatorHeight)
                .with(photAnimatorWidth)
                .with(recycleAnimationY)
                .before(appBarAnimation)
                .before(bfAnim);

        animatorSet.setDuration(ANIM_DURATION);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.addListener(new AnimatorHelperListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                PersonActivity.super.finish();
                overridePendingTransition(0, 0);
            }
        });
        animatorSet.start();
    }

    @Override
    public void finish() {
        exitAnimation();
    }


    @Subscribe
    public void onOpenEventEvent(OpenEventEvent event) {
        EventActivity.open(this, event);
    }

    @Subscribe
    public void onFavoriteRefreshEvent(FavoriteRefreshEvent event) {
        setResult(REFRESH_NEEDED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_EVENT_REQUEST && resultCode == EventActivity.REFRESH_NEEDED) {
            if (personType == 0)
                speakerDataAdapter.notifyDataSetChanged();
            else
                staffDataAdapter.notifyDataSetChanged();

            setResult(REFRESH_NEEDED);
        }
    }

    public static void open(Activity activity, OpenPersonEvent event) {
        Intent intent = new Intent(activity, PersonActivity.class);

        if (event.speaker != null) {
            intent.putExtra(PersonActivity.PERSON_ID, event.speaker.getId());
            intent.putExtra(PersonActivity.PERSON_TYPE, 0);
        } else if (event.staff != null) {
            intent.putExtra(PersonActivity.PERSON_ID, event.staff.getId());
            intent.putExtra(PersonActivity.PERSON_TYPE, 1);
        }

        int[] displayLocation = new int[2];
        event.getPhoto().getLocationOnScreen(displayLocation);
        Bundle animBundle = new Bundle();
        animBundle.putInt(PersonActivity.PHOTO_LEFT, displayLocation[0]);
        animBundle.putInt(PersonActivity.PHOTO_TOP, displayLocation[1]);
        animBundle.putInt(PersonActivity.PHOTO_WIDTH, event.getPhoto().getWidth());
        animBundle.putInt(PersonActivity.PHOTO_HEIGHT, event.getPhoto().getHeight());

        Bitmap floorBitmap = ((BitmapDrawable) event.getPhoto().getDrawable()).getBitmap();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        floorBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] b = bos.toByteArray();
        animBundle.putByteArray(PersonActivity.PHOTO_BITMAP, b);
        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        intent.putExtra(PersonActivity.ANIMATION_BUNDLE, animBundle);
        intent.putExtra("is_archive", event.isArchive());
        activity.startActivityForResult(intent, EventActivity.OPEN_PERSON_REQUEST);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speakerDataAdapter.onPhonePermissionGranted();
            }
        }
    }

    @Override
    public void finishWithoutAnimation() {
        super.finish();
    }
}
