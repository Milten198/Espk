package com.pgssoft.testwarez.database.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

/**
 * Created by dpodolak on 18.04.16.
 */
@DatabaseTable
public class Video implements Parcelable {

    public static final String VIDEO_ID = "video_id";
    public static final String TAG = "VIDEO_TAG";
    public static final String CONFERENCE = "conference_id";

    @DatabaseField(columnName = VIDEO_ID, id = true)
    private int id;

    @DatabaseField
    private String url;

    @DatabaseField(foreign = true)
    private BEFile file;

    @DatabaseField
    private String name;

    @DatabaseField(columnName = CONFERENCE, foreign = true)
    private Conference conference;

    @SerializedName("created_at")
    @DatabaseField
    private DateTime createdAt;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    public Video() {
    }

    public Video(int id, String url, String name, Conference conference) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.conference = conference;
    }

    public Video(Archive archive) {
        id = archive.getId();
        name = archive.getName();
        url = archive.getUrl();
        conference = archive.getConference();
        file = archive.getFile();
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public BEFile getFile() {
        return file;
    }

    public Conference getConference() {
        return conference;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.url);
        dest.writeString(this.name);
        dest.writeParcelable(file, 0);
        if(conference != null) {
            dest.writeInt(this.conference.getId());
        } else {
            dest.writeInt(0);
        }
    }

    protected Video(Parcel in) {
        this.id = in.readInt();
        this.url = in.readString();
        this.name = in.readString();
        this.file = in.readParcelable(Video.class.getClassLoader());
        this.conference = new Conference(in.readInt());
    }

    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel source) {
            return new Video(source);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public void setConference(Conference conference) {
        this.conference = conference;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Video video = (Video) o;

        return id == video.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public boolean isYoutubeVideo() {
        return url != null && (url.contains("youtube") || url.contains("youtu.be"));
    }

    public boolean isValid() {
        boolean validName = name != null && !name.isEmpty() ;
        boolean validUrlOrFile = (url != null && !url.isEmpty()) || (file != null); //&& file.getName() != null && !file.getName().isEmpty());
        return validName && validUrlOrFile;
    }
}
