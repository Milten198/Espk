package com.pgssoft.testwarez.feature.plan;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.event.AgendaScrollEvent;
import com.pgssoft.testwarez.event.OpenEventEvent;
import com.pgssoft.testwarez.feature.agenda.all.AgendaAdapter;
import com.pgssoft.testwarez.feature.agenda.filter.PlaceFilter;
import com.pgssoft.testwarez.feature.event.EventActivity;
import com.pgssoft.testwarez.database.model.Place;
import com.pgssoft.testwarez.util.FinishWithoutAnim;
import com.squareup.otto.Subscribe;

import java.sql.SQLException;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dpodolak on 02.03.16.
 */
public class PlaceEventsActivity extends BaseActivity implements FinishWithoutAnim{


    @Bind(R.id.rvActivityPlaceEvents)
    RecyclerView recyclerView;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private AgendaAdapter adapter;

    public static final String PLACE_NAME = "placeName";
    public static final String PLACE_ID = "placeId";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_events);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(getIntent().getStringExtra(PLACE_NAME));
        toolbar.setNavigationOnClickListener(view -> {
            ApplicationController.getParentManager().runParentActivity(this);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        compositeSubscription.add(Observable.defer(() -> Observable.just(getPlace(getIntent().getExtras().getInt(PLACE_ID))))
                .doOnError(Throwable::printStackTrace)
                .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(place -> {
                adapter = new AgendaAdapter(this, new PlaceFilter(place.getId()), true);
                recyclerView.setAdapter(adapter);
            }));

    }

    private Place getPlace(int placeId) {
        Place place = null;

        try {
            place = ApplicationController.getDatabaseHelper().getPlacesDao().queryForId(placeId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return place;
    }

    @Subscribe
    public void onOpenEventEvent(OpenEventEvent event) {
        EventActivity.open(this, event);
    }

    @Subscribe
    public void onAgendaScrollEvent(AgendaScrollEvent event) {
        recyclerView.scrollToPosition(event.getPosition());
    }

    @Override
    public void finishWithoutAnimation() {
        finish();
    }
}
