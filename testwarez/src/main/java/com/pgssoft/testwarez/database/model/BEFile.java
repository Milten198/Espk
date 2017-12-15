package com.pgssoft.testwarez.database.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.pgssoft.testwarez.database.model.companies.Company;

import org.joda.time.DateTime;

/**
 * Created by brosol on 2017-07-04.
 */

@DatabaseTable(tableName = "befiles")
public class BEFile implements Parcelable {

    public static final String CONFERENCE_ID = "conferenceId";
    public static final String GALLERY_ID = "image_gallery_id";
    public static final String ARCHIVE_ID = "archive_id";
    public static final String SPEAKER_ID = "speaker_id";
    public static final String VIDEO_ID = "video_id";
    public static final String STAFF_ID = "staff_id";
    public static final String PLACE_ID = "place_id";
    public static final String BUILDING_PLAN_ID = "building_plan_id";
    public static final String COMPANY_ID = "company_id";
    public static final String GALLERY_BEFILE_ID = "gallery_befile_id";

    @DatabaseField(columnName = GALLERY_ID, foreign = true)
    private Gallery gallery;

    @DatabaseField(columnName = ARCHIVE_ID, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Archive archive;

    @DatabaseField(columnName = VIDEO_ID, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Video video;

    @DatabaseField(columnName = SPEAKER_ID, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Speaker speaker;

    @DatabaseField(columnName = STAFF_ID, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Staff staff;

    @DatabaseField(columnName = PLACE_ID, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Place place;

    @DatabaseField(columnName = BUILDING_PLAN_ID, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private BuildingPlan buildingPlan;

    @DatabaseField(columnName = COMPANY_ID, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Company company;

    @DatabaseField(columnName = GALLERY_BEFILE_ID, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private GalleryBEFile galleryBEFile;

    @SerializedName("id")
    @DatabaseField(id = true)
    private int fileId;

    @DatabaseField(columnName = CONFERENCE_ID)
    private int conferenceId;

    @DatabaseField
    private String path;

    @DatabaseField
    private String name;

    @SerializedName("mime_type")
    @DatabaseField
    private String mimeType;

    @DatabaseField
    private long size;

    @SerializedName("upload_type")
    @DatabaseField
    private int uploadType;

    @DatabaseField
    private int width;

    @DatabaseField
    private int height;

    @SerializedName("created_at")
    @DatabaseField
    private DateTime createdAt;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    public Gallery getGallery() {
        return gallery;
    }

    public void setGallery(Gallery gallery) {
        this.gallery = gallery;
    }

    public GalleryBEFile getGalleryBEFile() {
        return galleryBEFile;
    }

    public void setGalleryBEFile(GalleryBEFile galleryBEFile) {
        this.galleryBEFile = galleryBEFile;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(int conferenceId) {
        this.conferenceId = conferenceId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getUploadType() {
        return uploadType;
    }

    public void setUploadType(int uploadType) {
        this.uploadType = uploadType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(DateTime createdAt) {
        this.createdAt = createdAt;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(DateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public void setBuildingPlan(BuildingPlan buildingPlan) {
        this.buildingPlan = buildingPlan;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.fileId);
        dest.writeInt(this.conferenceId);
        dest.writeString(this.path);
        dest.writeString(this.name);
        dest.writeString(this.mimeType);
        dest.writeLong(this.size);
        dest.writeInt(this.uploadType);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeSerializable(this.createdAt);
        dest.writeSerializable(this.updatedAt);
    }

    private BEFile() {
    }

    protected BEFile(Parcel in) {
        this.fileId = in.readInt();
        this.conferenceId = in.readInt();
        this.path = in.readString();
        this.name = in.readString();
        this.mimeType = in.readString();
        this.size = in.readLong();
        this.uploadType = in.readInt();
        this.width = in.readInt();
        this.height = in.readInt();
        this.createdAt = (DateTime) in.readSerializable();
        this.updatedAt = (DateTime) in.readSerializable();
    }

    public static final Parcelable.Creator<BEFile> CREATOR = new Parcelable.Creator<BEFile>() {
        @Override
        public BEFile createFromParcel(Parcel source) {
            return new BEFile(source);
        }

        @Override
        public BEFile[] newArray(int size) {
            return new BEFile[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BEFile file = (BEFile) o;

        if (conferenceId != file.conferenceId) return false;
        if (size != file.size) return false;
        if (uploadType != file.uploadType) return false;
        if (width != file.width) return false;
        if (height != file.height) return false;
        if (path != null ? !path.equals(file.path) : file.path != null) return false;
        if (!name.equals(file.name)) return false;
        return mimeType.equals(file.mimeType);
    }

    @Override
    public int hashCode() {
        int result = conferenceId;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + mimeType.hashCode();
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + uploadType;
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }
}
