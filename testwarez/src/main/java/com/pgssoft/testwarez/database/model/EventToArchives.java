package com.pgssoft.testwarez.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by brosol on 2016-05-10.
 */
@DatabaseTable(tableName = "eventToArchives")
public class EventToArchives {
    public static final String EVENT_COLUMN_NAME = "event_id";
    public static final String ARCHIVE_COLUMN_NAME = "archive_id";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(uniqueCombo = true, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Event event;

    @DatabaseField(uniqueCombo = true, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Archive archive;

    public EventToArchives() {
    }

    public EventToArchives(Event event, Archive archive) {
        this.event = event;
        this.archive = archive;
    }

    public int getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public Archive getArchive() {
        return archive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventToArchives that = (EventToArchives) o;

        if (event.getId() != that.event.getId()) return false;
        return archive.getId() == that.archive.getId();
    }

    @Override
    public int hashCode() {
        return id;
    }
}
