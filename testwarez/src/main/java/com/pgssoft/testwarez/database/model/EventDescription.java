package com.pgssoft.testwarez.database.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

/**
 * Created by rtulaza on 2015-08-07.
 */

@DatabaseTable(tableName = "eventDescriptions")
public class EventDescription {

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String lang;

    @DatabaseField
    private String title;

    @DatabaseField
    private String description;

    @DatabaseField(foreign = true)
    private Event event;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    public int getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventDescription eventDescription = (EventDescription) o;

        return id == eventDescription.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
