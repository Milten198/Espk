package com.pgssoft.testwarez.feature.map;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.ProgressBar;

import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseNavigationDrawerActivity;
import com.pgssoft.testwarez.event.SearchEvent;
import com.pgssoft.testwarez.widget.MapView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by dpodolak on 24.02.16.
 */
public class MapActivity extends BaseNavigationDrawerActivity {

    @Bind(R.id.mvMapView)
    MapView mapView;
    @Bind(R.id.map_progress_bar)
    ProgressBar progressBar;

    private Subscription subscription;

    @Override
    public void updateView() {
        mapView.updateView(progressBar);
    }

    @Override
    public SearchEvent.SEARCH_TYPE getSerachType() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.primary), PorterDuff.Mode.SRC_ATOP);

        subscription = Observable.timer(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> {
                mapView.setUp(getSupportFragmentManager());
                mapView.updateView(progressBar);
            }, Throwable::printStackTrace);
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }
}
