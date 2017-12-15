package com.pgssoft.testwarez.feature.plan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.david422.areatouchdetector.AreaTouchableView;
import com.david422.areatouchdetector.OnAreaTouchListener;
import com.j256.ormlite.stmt.QueryBuilder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.database.model.BuildingPlan;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Place;
import com.pgssoft.testwarez.util.SimpleImageLoader;
import com.pgssoft.testwarez.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dpodolak on 29.02.16.
 */
public class ShowFloorActivity extends BaseActivity implements OnAreaTouchListener<Place> {

    @Bind(R.id.atvActivityShowFloor)
    AreaTouchableView areaTouchableView;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.ivActivityShowFloor)
    ImageView animationImageView;

    @Bind(R.id.pbActivityShowFloorLoading)
    ProgressBar loadingProgressBar;

    @Bind(R.id.info_text_view)
    TextView infoTextView;


    private ImageLoader loader;
    private DisplayImageOptions mainOptions;

    private Rect animationRect = new Rect();
    private DisplayMetrics metrics;

    private static final int ANIM_DURATION = 300;
    /**
     * Deltas using to animate imaqeView
     */
    private int leftDelta;
    private int topDelta;
    private float widthScale;
    private float heightScale;
    private List<Place> placesWithEvent = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_floor);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("name"));
        toolbar.setNavigationOnClickListener(view -> finish());

        loadingProgressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.primary), PorterDuff.Mode.SRC_ATOP);

        metrics = new DisplayMetrics();
        BuildingPlan plan = null;

        Bundle dataBundle = getIntent().getExtras();
        Place place = null;
        if (dataBundle.getParcelable("plan") instanceof BuildingPlan) {
            plan = dataBundle.getParcelable("plan");
            if (plan != null) {
                plan.setPlacesPlan(dataBundle.getParcelable("places_plan"));
                plan.setPlan(dataBundle.getParcelable("plan_plan"));
            }
        } else {
            place = dataBundle.getParcelable("plan");
            
            if (place != null) {
                place.setPlace(dataBundle.getParcelable("plan_place"));
            }
        }

        for (Event e : ApplicationController.getActiveConference().getEventCollection()) {
            placesWithEvent.add(e.getPlace());
        }

        if (dataBundle.containsKey("left")) {
            animationRect.set(
                    getIntent().getExtras().getInt("left"),
                    getIntent().getExtras().getInt("left") + getIntent().getExtras().getInt("top"),
                    getIntent().getExtras().getInt("width"),
                    getIntent().getExtras().getInt("top") + getIntent().getExtras().getInt("height")
            );
            byte[] b = getIntent().getExtras().getByteArray("floor");
            Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
            animationImageView.setImageBitmap(bmp);
        }

        loader = ImageLoader.getInstance();
        mainOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.NONE)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();

        if (plan != null) {
            initPlan(plan);
        } else {
            initPlace(place);
        }

        animationImageView.setVisibility(View.VISIBLE);

        if (dataBundle.containsKey("left")) {
            ViewTreeObserver observer = animationImageView.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    animationImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    calculateDelta();
                    animateEnterImageView();
                    return true;
                }
            });
        } else {
            areaTouchableView.setVisibility(View.VISIBLE);
        }
    }

    private void initPlace(Place place) {
        if (place.getPlace() != null) {
            loader.loadImage(getResources().getString(R.string.endpoint) + getResources().getString(R.string.images_url_default, place.getPlace().getFileId()), mainOptions, mainImageLoader);
        }
    }

    private void initPlan(BuildingPlan plan) {
        DisplayImageOptions maskOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.NONE)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();

        compositeSubscription.add(Observable.defer(() -> Observable.from(getPlaces(plan.getId())))
                .doOnError(Throwable::printStackTrace)
                .filter(place1 -> Utils.validHexColor(place1.getColor()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(eventPlace -> {
                    areaTouchableView.addExistingColor(Color.parseColor(eventPlace.getColor()), eventPlace);
                        },
                    Throwable::printStackTrace));

        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        if (plan.getPlan() != null) {
            loader.loadImage(getResources().getString(R.string.endpoint) + getResources().getString(R.string.images_url_default, plan.getPlan().getFileId()), mainOptions, mainImageLoader);
        }
        areaTouchableView.setOnAreaTouchListener(this);

        if (plan.getPlacesPlan() != null) {
            loader.loadImage(getString(R.string.endpoint) + getResources().getString(R.string.images_url_default, plan.getPlacesPlan().getFileId()), maskOptions, maskImageLoader);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        animateExitImageView();
    }

    private void calculateDelta() {
        int[] screenLocation = new int[2];
        animationImageView.getLocationOnScreen(screenLocation);

        leftDelta = animationRect.left - screenLocation[0];
        topDelta = animationRect.top - screenLocation[1];
        widthScale = (float) animationRect.width() / animationImageView.getWidth();
        heightScale = (float) animationRect.height() / animationImageView.getHeight();
    }

    private void animateEnterImageView() {
        animationImageView.setPivotX(0);
        animationImageView.setPivotY(0);
        animationImageView.setScaleX(widthScale);
        animationImageView.setScaleY(heightScale);
        animationImageView.setTranslationX(leftDelta);
        animationImageView.setTranslationY(topDelta);

        animationImageView.animate()
                .setDuration(ANIM_DURATION)
                .scaleX(1f).scaleY(1f)
                .translationX(0).translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    animationImageView.setVisibility(View.GONE);
                    areaTouchableView.setVisibility(View.VISIBLE);
                });
    }

    private void animateExitImageView() {

        areaTouchableView.setVisibility(View.GONE);
        animationImageView.setVisibility(View.VISIBLE);

        animationImageView.animate()
                .setDuration(ANIM_DURATION)
                .scaleX(widthScale).scaleY(heightScale)
                .translationX(leftDelta).translationY(topDelta)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    ShowFloorActivity.super.finish();
                    overridePendingTransition(0, 0);
                });
    }

    private List<Place> getPlaces(int id) {
        List<Place> placeList = new ArrayList<>();
        try {
            QueryBuilder<Place, Integer> placesQB = ApplicationController.getDatabaseHelper().getPlacesDao().queryBuilder();
            placesQB.where().eq(Place.BUILDING_PLAN_ID, id);
            placeList = placesQB.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return placeList;
    }

    @Override
    public void onAreaTouch(int color, Place place) {

        if (placesWithEvent.contains(place)) {
            Intent placeIntent = new Intent(this, PlaceEventsActivity.class);
            placeIntent.putExtra(PlaceEventsActivity.PLACE_ID, place.getId());
            placeIntent.putExtra(PlaceEventsActivity.PLACE_NAME, place.getFullName());
            startActivity(placeIntent);
        }
    }

    SimpleImageLoader mainImageLoader = new SimpleImageLoader() {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage.getWidth() >= metrics.widthPixels) {
                areaTouchableView.setMainView(loadedImage);
            } else {
                float ratio = ((float) metrics.widthPixels) / loadedImage.getWidth();
                areaTouchableView.setMainView(Bitmap.createScaledBitmap(loadedImage, metrics.widthPixels, (int) (loadedImage.getHeight() * ratio), true));
            }

            if (!getIntent().getExtras().getBoolean("clickable")) {
                areaTouchableView.disableClick();
                infoTextView.setVisibility(View.GONE);
            }
            loadingProgressBar.setVisibility(View.GONE);
        }
    };

    SimpleImageLoader maskImageLoader = new SimpleImageLoader() {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            try {
                if (loadedImage.getWidth() >= metrics.widthPixels) {
                    areaTouchableView.setMaskView(loadedImage);
                } else {
                    float ratio = ((float) metrics.widthPixels)  / loadedImage.getWidth();

                    areaTouchableView.setMaskView(Bitmap.createScaledBitmap(loadedImage,
                            metrics.widthPixels, (int) (loadedImage.getHeight() * ratio), true));
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    };

    public static void openActivity(Activity activity, ImageView planImageView, Object plan, String name, boolean clickable) {

        if (!(plan instanceof BuildingPlan) && !(plan instanceof Place)) {
            throw new IllegalStateException("The plan object has unproperly instance");
        }
        if (planImageView == null || planImageView.getDrawable() == null || plan == null) {
            return;
        }

        Intent buildingIntent = new Intent(activity, ShowFloorActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("plan", plan instanceof Place ? (Place) plan : (BuildingPlan) plan);
        bundle.putParcelable("plan_place", plan instanceof Place ? ((Place) plan).getPlace() : null);
        bundle.putParcelable("places_plan", plan instanceof BuildingPlan ? ((BuildingPlan) plan).getPlacesPlan() : null);
        bundle.putParcelable("plan_plan", plan instanceof BuildingPlan ? ((BuildingPlan) plan).getPlan() : null);


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            int[] screenLocattion = new int[2];
            planImageView.getLocationOnScreen(screenLocattion);

            Bitmap floorBitmap = ((BitmapDrawable) planImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            floorBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] b = bos.toByteArray();
            bundle.putByteArray("floor", b);
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            bundle.putInt("left", screenLocattion[0]);
            bundle.putInt("top", screenLocattion[1]);
            bundle.putInt("width", planImageView.getWidth());
            bundle.putInt("height", planImageView.getHeight());

        }
        bundle.putString("name", name);
        bundle.putBoolean("clickable", clickable);
        buildingIntent.putExtras(bundle);

        activity.startActivity(buildingIntent);
        activity.overridePendingTransition(0, 0);
    }
}
