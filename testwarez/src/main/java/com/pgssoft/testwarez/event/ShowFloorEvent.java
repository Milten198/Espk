package com.pgssoft.testwarez.event;

import android.widget.ImageView;

import com.pgssoft.testwarez.database.model.BuildingPlan;

/**
 * Created by dpodolak on 29.02.16.
 */
public class ShowFloorEvent {
    BuildingPlan buildingPlan;
    ImageView floorImageView;

    public ShowFloorEvent(ImageView floorImageView, BuildingPlan buildingPlan) {
        this.buildingPlan = buildingPlan;
        this.floorImageView = floorImageView;
    }

    public ImageView getFloorImageView() {
        return floorImageView;
    }

    public BuildingPlan getBuildingPlan() {
        return buildingPlan;
    }
}
