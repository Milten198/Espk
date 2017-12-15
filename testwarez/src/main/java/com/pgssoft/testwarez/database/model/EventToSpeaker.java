package com.pgssoft.testwarez.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by rtulaza on 2015-08-10.
 */

@DatabaseTable(tableName = "eventToSpeaker")
public class EventToSpeaker {
    public static final String EVENT_COLUMN_NAME = "event_id";
    public static final String SPEAKER_COLUMN_NAME = "speaker_id";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(uniqueCombo = true, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Event event;

    @DatabaseField(uniqueCombo = true, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Speaker speaker;

    public EventToSpeaker() {
    }

    public EventToSpeaker(Event event, Speaker speaker) {
        this.event = event;
        this.speaker = speaker;
    }

    public int getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventToSpeaker that = (EventToSpeaker) o;

        if (event.getId() != that.event.getId()) return false;
        return speaker.getId() == that.speaker.getId();
    }

    @Override
    public int hashCode() {
        return id;
    }
}
