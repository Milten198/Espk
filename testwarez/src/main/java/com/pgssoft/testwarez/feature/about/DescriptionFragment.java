package com.pgssoft.testwarez.feature.about;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.util.SimpleImageLoader;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class DescriptionFragment extends Fragment {

    @Bind(R.id.title)
    protected TextView title;
    @Bind(R.id.about_banner_container)
    protected FrameLayout bannerContainer;
    @Bind(R.id.about_banner)
    protected ImageView banner;
    @Bind(R.id.header_text)
    protected TextView headerText;

    CompositeSubscription compositeSubscription = new CompositeSubscription();
    private DisplayImageOptions options;


    public static DescriptionFragment newInstance() {
        DescriptionFragment fragment = new DescriptionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DescriptionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text, container, false);
        ButterKnife.bind(this, view);

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        options = new DisplayImageOptions.Builder()
                .preProcessor(bitmap -> {
                    if (isTablet) {
                        return bitmap;
                    } else {
                        float ratio = (float) bitmap.getHeight() / bitmap.getWidth();
                        int width = bannerContainer.getWidth();
                        int maxHeight = (int) (width * ratio);
                        return Bitmap.createScaledBitmap(bitmap, width, maxHeight, false);
                    }
                })
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        updateView();

        bannerContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                bannerContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                bannerContainer.setTranslationY(-bannerContainer.getHeight());
                return true;
            }
        });
        return view;
    }

    private void setBanner() {
        compositeSubscription.add(Observable.defer(() -> Observable.just(ApplicationController.getActiveConference()))
                .doOnError(Throwable::printStackTrace)
                .map(Conference::getDescription)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(conferenceDescription -> {
                    if (conferenceDescription != null) {
                        ImageLoader.getInstance().loadImage(getResources().getString(R.string.endpoint) +
                                getResources().getString(R.string.images_url_default, conferenceDescription.getBanner().getFileId()), options, new SimpleImageLoader() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                bannerContainer.setMinimumHeight((int) (getResources().getDisplayMetrics().density * 125));
                                bannerContainer.getLayoutParams().height = loadedImage.getHeight();

                                banner.setImageBitmap(loadedImage);
                                bannerContainer.animate().translationY(0).start();
                            }
                        });
                    } else {
                        bannerContainer.setVisibility(View.GONE);
                    }
                })
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
        compositeSubscription.unsubscribe();
    }

    public void updateView() {
        compositeSubscription.add(Observable.defer(() -> Observable.just(ApplicationController.getActiveConference()))
                .map(Conference::getDescription)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(conferenceDescription -> {
                    headerText.setText(Html.fromHtml(conferenceDescription.getHeader()));
                    title.setText(Html.fromHtml(conferenceDescription.getGoal()));
                    title.setMovementMethod(LinkMovementMethod.getInstance());
                    title.setLinksClickable(true);
                }, Throwable::printStackTrace));

        setBanner();
    }
}
