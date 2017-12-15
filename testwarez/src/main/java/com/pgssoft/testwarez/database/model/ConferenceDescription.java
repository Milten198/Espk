package com.pgssoft.testwarez.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

/**
 * Created by rtulaza on 2015-08-20.
 */

@DatabaseTable(tableName = "conferenceDescriptions")
public class ConferenceDescription {

    public static final String CONFERENCE = "conference";

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String lang;

    @DatabaseField
    private String goal;

    @DatabaseField
    private DateTime updatedAt;

    @DatabaseField(columnName = CONFERENCE, foreign = true)
    private Conference conference;

    @DatabaseField
    private String header = "";

    @DatabaseField(foreign = true)
    private BEFile banner;

    public ConferenceDescription() {

    }

    public int getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    public String getGoal() {
        return goal;
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

    public BEFile getBanner() {
        return banner;
    }

    public String getHeader() {
        return header;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConferenceDescription conferenceDescription = (ConferenceDescription) o;

        return id == conferenceDescription.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
