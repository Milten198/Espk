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
import java.util.List;
import java.util.Locale;

/**
 * Created by rtulaza on 2015-08-20.
 */


@DatabaseTable(tableName = "places")
public class Place implements Parcelable {

    public static final String BUILDING_PLAN_ID = "buildingPlan_id";
    public static final String ID = "id";

    @DatabaseField(id = true)
    private int id;

    private Conference conference;

    @ForeignCollectionField(eager = false)
    private Collection<PlaceDescription> descriptions;

    @DatabaseField(foreign = true)
    private BEFile place;

    @DatabaseField
    private String color;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    @ForeignCollectionField
    private Collection<Event> events;

    @SerializedName("building_plan")
    @DatabaseField(foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private BuildingPlan buildingPlan;

    public int getId() {
        return id;
    }

    public Conference getConference() {
        return conference;
    }

    public List<PlaceDescription> getDescriptions() {
        return new ArrayList<>(descriptions);
    }

    public String getName() {
        String lang = Utils.getLanguage();

        for (PlaceDescription pd: getDescriptions()){
            if (pd.getLang().equals(lang)){
                return pd.getTitle();
            }
        }

        if (!getDescriptions().isEmpty()){
            return getDescriptions().get(0).getTitle();
        }

        return "";
    }

    public String getFullName() {
        return String.format("%s, %s", buildingPlan != null ? buildingPlan.getName() : "", getName());
    }

    public BEFile getPlace() {
        return place;
    }

    public void setPlace(BEFile place) {
        this.place = place;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getColor() {
        if (color.length() == 4) {
            return "" + color.charAt(0) + color.charAt(1) + color.charAt(1) + color.charAt(2) + color.charAt(2) + color.charAt(3) + color.charAt(3);
        }
        return color;
    }

    public BuildingPlan getBuildingPlan() {
        return buildingPlan;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.color);
        dest.writeSerializable(this.updatedAt);
        dest.writeParcelable(this.buildingPlan, flags);

        List<PlaceDescription> descriptionList = new ArrayList<>(descriptions);
        dest.writeList(descriptionList);
    }

    public Place() {
    }

    protected Place(Parcel in) {
        this.id = in.readInt();
        this.color = in.readString();
        this.updatedAt = (DateTime) in.readSerializable();
        this.buildingPlan = in.readParcelable(BuildingPlan.class.getClassLoader());
        this.descriptions = in.readArrayList(PlaceDescription.class.getClassLoader());
    }

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel source) {
            return new Place(source);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Place place = (Place) o;

        return id == place.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "PlaceId: %d, name: %s", id, getFullName());
    }
}
