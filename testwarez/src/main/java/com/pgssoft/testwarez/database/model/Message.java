package com.pgssoft.testwarez.database.model;

import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by rtulaza on 2015-08-14.
 */
@DatabaseTable(tableName = "message")
public class Message {

    public static final String FACEBOOK = "F";
    public static final String TWITTER = "T";
    public static final String ORGANIZER = "O";
    public static final String CONFERENCE_ID = "conference_id";

    @StringDef({FACEBOOK, TWITTER, ORGANIZER})
    @Retention(RetentionPolicy.SOURCE)
    private @interface MediumType{}

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    @SerializedName("created_at")
    private DateTime createdAt;

    @DatabaseField
    private String medium = ORGANIZER;

    @DatabaseField
    private String descriptions;

    @DatabaseField
    @SerializedName("updated_at")
    private DateTime updatedAt;

    @DatabaseField(columnName = CONFERENCE_ID)
    private int conferenceId;

    public Message() {}

    public Message(int id, String content, DateTime dateTime) {
        this.id = id;
        descriptions = content;
        createdAt = dateTime;
        updatedAt = dateTime;
       setMedium(ORGANIZER);
    }

    public int getId() {
        return id;
    }

    public void setMedium(@MediumType String type){
        medium = type;
    }


    public DateTime getCreatedAt() {
        return createdAt;
    }

    public String getMedium() {
        return medium.toUpperCase();
    }

    public String getDescriptions() {
        return descriptions;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Message: " + descriptions + ", medium: " + getMedium() + "\n";
    }

    public int getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(int conferenceId) {
        this.conferenceId = conferenceId;
    }
}
