package com.pgssoft.testwarez.database.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

import java.util.Collection;

/**
 * Created by brosol on 2016-03-29.
 */

@DatabaseTable(tableName = "galleries")
public class Gallery implements Parcelable {

    public static final String GALLERY_ID = "gallery_id";
    public static final String CONFERENCE_ID = "conferenceId";

    @DatabaseField(columnName = GALLERY_ID, id = true)
    private int id;

    @DatabaseField(foreign = true)
    private Conference conference;

    @SerializedName("conference_id")
    @DatabaseField(columnName = CONFERENCE_ID)
    private int conferenceId;

    @SerializedName("created_at")
    @DatabaseField
    private DateTime createdAt;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    @ForeignCollectionField(eager = false)
    private Collection<GalleryBEFile> images;

    public int getId() {
        return id;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public Collection<GalleryBEFile> getImages() {
        return images;
    }

    public Conference getConference() {
        return conference;
    }

    public int getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(int conferenceId) {
        this.conferenceId = conferenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gallery gallery = (Gallery) o;

        return id == gallery.id;
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
        dest.writeInt(this.conferenceId);
        dest.writeSerializable(this.createdAt);
        dest.writeSerializable(this.updatedAt);
    }

    public Gallery() {
    }

    protected Gallery(Parcel in) {
        this.id = in.readInt();
        this.conferenceId = in.readInt();
        this.createdAt = (DateTime) in.readSerializable();
        this.updatedAt = (DateTime) in.readSerializable();
    }

    public static final Parcelable.Creator<Gallery> CREATOR = new Parcelable.Creator<Gallery>() {
        @Override
        public Gallery createFromParcel(Parcel source) {
            return new Gallery(source);
        }

        @Override
        public Gallery[] newArray(int size) {
            return new Gallery[size];
        }
    };
}
