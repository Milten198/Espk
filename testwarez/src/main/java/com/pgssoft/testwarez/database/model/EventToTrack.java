package com.pgssoft.testwarez.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by dpodolak on 24.03.16.
 */
@DatabaseTable(tableName = "eventToTrack")
public class EventToTrack {

    public static final String EVENT_COLUMN_NAME = "event_id";
    public static final String TRACK_COLUMN_NAME = "track_id";
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = EVENT_COLUMN_NAME, uniqueCombo = true, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Event event;

    @DatabaseField(columnName = TRACK_COLUMN_NAME, uniqueCombo = true, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Track track;

    public EventToTrack() {
    }

    public EventToTrack(Event e, Track track) {
        this.event = e;
        this.track = track;
    }

    public Track getTrack() {
        return track;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventToTrack eventToTrack = (EventToTrack) o;

        if (event.getId() != eventToTrack.event.getId()) return false;
        return track.getId() == eventToTrack.track.getId();
    }

    @Override
    public int hashCode() {
        return id;
    }
}
