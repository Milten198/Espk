package com.pgssoft.testwarez.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by brosol on 2016-03-24.
 */
@DatabaseTable(tableName = "eventToStaff")
public class EventToStaff {
    public static final String EVENT_COLUMN_NAME = "event_id";
    public static final String STAFF_COLUMN_NAME = "staff_id";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(uniqueCombo = true, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Event event;

    @DatabaseField(uniqueCombo = true, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Staff staff;

    public EventToStaff() {
    }

    public EventToStaff(Event event, Staff staff) {
        this.event = event;
        this.staff = staff;
    }

    public int getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public Staff getStaff() {
        return staff;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventToStaff that = (EventToStaff) o;

        if (event.getId() != that.event.getId()) return false;
        return staff.getId() == that.staff.getId();
    }

    @Override
    public int hashCode() {
        return id;
    }
}
