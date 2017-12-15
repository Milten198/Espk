package com.pgssoft.testwarez.database.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

/**
 * Created by brosol on 2016-03-29.
 */
@DatabaseTable(tableName = "images")
public class Image implements Parcelable {

    public static final String CONFERENCE_ID = "conferenceId";
    public static final String GALLERY_ID = "image_gallery_id";

    @DatabaseField(columnName = GALLERY_ID, foreign = true)
    private Gallery gallery;

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String image;

    @SerializedName("thumbnail_min")
    @DatabaseField
    private String thumbnailMin;

    @SerializedName("thumbnail_mid")
    @DatabaseField
    private String thumbnailMid;

    @SerializedName("thumbnail_big")
    @DatabaseField
    private String thumbnailBig;

    @DatabaseField
    private DateTime createdAt;

    @DatabaseField
    private DateTime updatedAt;

    @DatabaseField
    private DateTime takenAt;

    @SerializedName("conference_id")
    @DatabaseField(columnName = CONFERENCE_ID)
    private int conferenceId;

    public int getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public DateTime getTakenAt() {
        return takenAt;
    }

    public Gallery getGallery() {
        return gallery;
    }

    public void setGallery(Gallery gallery) {
        this.gallery = gallery;
    }

    public int getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(int conferenceId) {
        this.conferenceId = conferenceId;
    }

    public String getThumbnailMin() {
        return thumbnailMin;
    }

    public String getThumbnailMid() {
        return thumbnailMid;
    }

    public String getThumbnailBig() {
        return thumbnailBig;
    }

    private Image() {

    }

    private Image(Parcel in) {
        this.id = in.readInt();
        this.image = in.readString();
        this.thumbnailMin = in.readString();
        this.thumbnailMid = in.readString();
        this.thumbnailBig = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(image);
        dest.writeString(thumbnailMin);
        dest.writeString(thumbnailMid);
        dest.writeString(thumbnailBig);
    }

    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {

        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Image image = (Image) o;

        return id == image.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
