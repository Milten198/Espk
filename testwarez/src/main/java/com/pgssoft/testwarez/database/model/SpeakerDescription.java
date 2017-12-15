package com.pgssoft.testwarez.database.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

/**
 * Created by rtulaza on 2015-08-07.
 */

@DatabaseTable(tableName = "speakerDescriptions")
public class SpeakerDescription {
    public static final String SPEAKER = "speaker";

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String lang;

    @DatabaseField
    private String job;

    @DatabaseField
    private String company;

    @DatabaseField
    private String biography;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    @DatabaseField(columnName = SPEAKER, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Speaker speaker;

    public SpeakerDescription() {
    }

    public int getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    public String getJob() {
        return job;
    }

    public String getBiography() {
        return biography;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getCompany() {
        return company;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpeakerDescription speakerDescription = (SpeakerDescription) o;

        return id == speakerDescription.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
