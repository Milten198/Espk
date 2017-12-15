package com.pgssoft.testwarez.feature.plan;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.j256.ormlite.stmt.QueryBuilder;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseNavigationDrawerActivity;
import com.pgssoft.testwarez.event.SearchEvent;
import com.pgssoft.testwarez.event.ShowFloorEvent;
import com.pgssoft.testwarez.database.model.BuildingPlan;
import com.pgssoft.testwarez.database.model.Conference;
import com.squareup.otto.Subscribe;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dpodolak on 24.02.16.
 */
public class BuildingPlanActivity extends BaseNavigationDrawerActivity {

    @Bind(R.id.rvAvtivityBuild)
    RecyclerView mapRecyclerView;

    private MapBuildingAdapter adapter;

    @Override
    public void updateView() {
        compositeSubscription.add(Observable.defer(() -> Observable.just(getBuildingPlanList()))
                .doOnError(Throwable::printStackTrace)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(buildingPlan -> adapter.setPlanList(buildingPlan)));
    }

    @Override
    public SearchEvent.SEARCH_TYPE getSerachType() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_map);
        ButterKnife.bind(this);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        mapRecyclerView.setLayoutManager(manager);
        adapter = new MapBuildingAdapter(this);
        mapRecyclerView.setAdapter(adapter);
        updateView();
    }

    private List<BuildingPlan> getBuildingPlanList() {
        List<BuildingPlan> buildingPlanList = new ArrayList<>();
        try {
            Conference activeCinference = ApplicationController.getActiveConference();

            if (activeCinference != null) {
                int conferenceId = activeCinference.getId();
                QueryBuilder<BuildingPlan, Integer> buildingQB = ApplicationController.getDatabaseHelper().getBuildingPlanDao().queryBuilder();

                buildingQB.where().eq(BuildingPlan.CONFERENCE_ID, conferenceId);

                buildingPlanList = buildingQB.query();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return buildingPlanList;
    }

    @Subscribe
    public void onShowFloorClick(ShowFloorEvent plan) {

        ShowFloorActivity.openActivity(this, plan.getFloorImageView(), plan.getBuildingPlan(), plan.getBuildingPlan().getName(), true);
    }
}
