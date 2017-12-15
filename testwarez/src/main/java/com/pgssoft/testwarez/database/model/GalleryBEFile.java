package com.pgssoft.testwarez.database.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

/**
 * Created by brosol on 2017-07-19.
 */

@DatabaseTable(tableName = "gallery_befile")
public class GalleryBEFile implements Parcelable {

    public static final String CONFERENCE_ID = "conference_id";
    public static final String GALLERY_ID = "gallery_id";
    public static final String GALLERY_BEFILE_ID = "gallery_befile_id";

    @DatabaseField(columnName = GALLERY_BEFILE_ID, id = true)
    private int id;

    @DatabaseField(columnName = CONFERENCE_ID)
    private int conferenceId;

    @DatabaseField(foreign = true, columnName = GALLERY_ID)
    private Gallery gallery;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private BEFile image;

    @SerializedName("created_at")
    @DatabaseField
    private DateTime createdAt;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    public int getId() {
        return id;
    }

    public int getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(int conferenceId) {
        this.conferenceId = conferenceId;
    }

    public Gallery getGallery() {
        return gallery;
    }

    public void setGallery(Gallery gallery) {
        this.gallery = gallery;
    }

    public BEFile getImage() {
        return image;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.conferenceId);
        dest.writeParcelable(this.gallery, flags);
        dest.writeParcelable(this.image, flags);
        dest.writeSerializable(this.createdAt);
        dest.writeSerializable(this.updatedAt);
    }

    public GalleryBEFile() {
    }

    protected GalleryBEFile(Parcel in) {
        this.id = in.readInt();
        this.conferenceId = in.readInt();
        this.gallery = in.readParcelable(Gallery.class.getClassLoader());
        this.image = in.readParcelable(BEFile.class.getClassLoader());
        this.createdAt = (DateTime) in.readSerializable();
        this.updatedAt = (DateTime) in.readSerializable();
    }

    public static final Parcelable.Creator<GalleryBEFile> CREATOR = new Parcelable.Creator<GalleryBEFile>() {
        @Override
        public GalleryBEFile createFromParcel(Parcel source) {
            return new GalleryBEFile(source);
        }

        @Override
        public GalleryBEFile[] newArray(int size) {
            return new GalleryBEFile[size];
        }
    };
}
