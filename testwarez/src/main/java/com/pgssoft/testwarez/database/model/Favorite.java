package com.pgssoft.testwarez.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by rtulaza on 2015-08-13.
 */

@DatabaseTable(tableName = "favorites")
public class Favorite {
    public static final String EVENT_COLUMN_NAME = "event_id";
    public static final String CONFERENCE_ID_COLUMN = "conferenceId";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Event event;

    @DatabaseField
    private long calendarId;

    @DatabaseField
    private long reminderId;

    @DatabaseField(columnName = CONFERENCE_ID_COLUMN)
    private int conferenceId;

    public Favorite() {
    }

    public Favorite(Event event) {
        this.event = event;
        conferenceId =  event.getConferenceId();
    }

    public Event getEvent() {
        return event;
    }

    public long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(long calendarId) {
        this.calendarId = calendarId;
    }

    public long getReminderId() {
        return reminderId;
    }

    public void setReminderId(long reminderId) {
        this.reminderId = reminderId;
    }

    public static String getConferenceIdColumn() {
        return CONFERENCE_ID_COLUMN;
    }
}
