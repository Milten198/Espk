package com.pgssoft.testwarez.database.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

/**
 * Created by brosol on 2016-08-08.
 */

@DatabaseTable(tableName = "placeDescriptions")
public class PlaceDescription implements Parcelable {

    public static final String PLACE_ID = "placeIdForDescription";

    @DatabaseField(id = true)
    private int id;

    @SerializedName("parent")
    @DatabaseField
    private int placeId;

    @DatabaseField(foreign = true, columnName = PLACE_ID)
    private Place place;

    @DatabaseField
    private String lang;

    @DatabaseField
    private String title;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    public int getId() {
        return id;
    }

    public int getPlaceId() {
        return placeId;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public String getLang() {
        return lang;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlaceDescription placeDescription = (PlaceDescription) o;

        return id == placeDescription.id;
    }

    @Override
    public int hashCode() {
        return id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.placeId);
        dest.writeString(this.lang);
        dest.writeString(this.title);
        dest.writeSerializable(this.updatedAt);
    }

    public PlaceDescription() {
    }

    protected PlaceDescription(Parcel in) {
        this.id = in.readInt();
        this.placeId = in.readInt();
        this.lang = in.readString();
        this.title = in.readString();
        this.updatedAt = (DateTime) in.readSerializable();
    }

    public static final Creator<PlaceDescription> CREATOR = new Creator<PlaceDescription>() {
        @Override
        public PlaceDescription createFromParcel(Parcel source) {
            return new PlaceDescription(source);
        }

        @Override
        public PlaceDescription[] newArray(int size) {
            return new PlaceDescription[size];
        }
    };
}
