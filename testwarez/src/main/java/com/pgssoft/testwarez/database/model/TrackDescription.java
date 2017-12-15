package com.pgssoft.testwarez.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by dawidpodolak on 11.03.16.
 */
@DatabaseTable(tableName = "trackDescription")
public class TrackDescription {

    public static final String TRACK = "track";

    @DatabaseField(id = true)
    private int id;

    @DatabaseField(columnName = TRACK, foreign = true)
    private Track track;

    @DatabaseField
    private String lang;

    @DatabaseField
    private String title;

    public int getId() {
        return id;
    }

    public Track getTrack() {
        return track;
    }

    public String getLang() {
        return lang;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackDescription trackDescription = (TrackDescription) o;

        return id == trackDescription.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
