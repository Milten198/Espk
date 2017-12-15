package com.pgssoft.testwarez.database.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.pgssoft.testwarez.util.Utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by rtulaza on 2015-08-20.
 */

@DatabaseTable(tableName = "buildingPlans")
public class BuildingPlan implements Parcelable {

    public static final String CONFERENCE_ID = "conference_id";

    @DatabaseField(id = true)
    private int id;

    @DatabaseField(foreign = true)
    private BEFile plan;

    @SerializedName("places_plan")
    @DatabaseField(foreign = true)
    private BEFile placesPlan;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    @DatabaseField(columnName = CONFERENCE_ID, foreign = true)
    private Conference conference;

    @ForeignCollectionField(eager = false)
    private Collection<Place> places;

    @ForeignCollectionField(eager = false)
    private Collection<BuildingDescription> descriptions;

    public int getId() {
        return id;
    }

    public BEFile getPlan() {
        return plan;
    }

    public BEFile getPlacesPlan() {
        return placesPlan;
    }

    public void setPlan(BEFile plan) {
        this.plan = plan;
    }

    public void setPlacesPlan(BEFile placesPlan) {
        this.placesPlan = placesPlan;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public Conference getConference() {
        return conference;
    }

    public void setConference(Conference conference) {
        this.conference = conference;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeSerializable(this.updatedAt);
    }

    public BuildingPlan() {
    }

    protected BuildingPlan(Parcel in) {
        this.id = in.readInt();
        this.updatedAt = (DateTime) in.readSerializable();
    }

    public static final Creator<BuildingPlan> CREATOR = new Creator<BuildingPlan>() {
        public BuildingPlan createFromParcel(Parcel source) {
            return new BuildingPlan(source);
        }

        public BuildingPlan[] newArray(int size) {
            return new BuildingPlan[size];
        }
    };

    public Collection<BuildingDescription> getDescriptions() {
        return descriptions;
    }

    public String getName() {
        String lang = Utils.getLanguage();

        for (BuildingDescription bd : descriptions) {
            if (bd.getLang().contains(lang)) {
                return bd.getName();
            }
        }

        if (!descriptions.isEmpty()) {
            return new ArrayList<>(descriptions).get(0).getName();
        }

        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuildingPlan bp = (BuildingPlan) o;

        return id == bp.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
