package com.pgssoft.testwarez.database.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.pgssoft.testwarez.util.Utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by dpodolak on 07.03.16.
 */
@DatabaseTable(tableName = "track")
public class Track implements Comparable<Track>{

    public static final String COLUMN_LANG = "lang";
    public static String CONFERENCE_ID = "conference_id" ;

    @DatabaseField(id = true)
    private int id;

    @DatabaseField(columnName = "conference_id")
    private int conferenceId;

    @ForeignCollectionField(eager = false)
    private Collection<EventToTrack> events;

    @ForeignCollectionField(eager = false)
    private Collection<TrackDescription> descriptions;

    @DatabaseField
    private String color;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    public String getColor() {
        if (color.length() == 4) {
            return "#" + color.charAt(1) + color.charAt(1) + color.charAt(2) + color.charAt(2) + color.charAt(3) + color.charAt(3);
        }

        return color;
    }

    public int getId() {
        return id;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getTitle() {
        //todo language
        String lang = Utils.getLanguage();
        for (TrackDescription td: getDescriptions()){
            if (td.getLang().equals(lang)){
                return td.getTitle();
            }
        }
        return null;
    }
    public ArrayList<TrackDescription> getDescriptions() {
        return new ArrayList<>(descriptions);
    }

    public int getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(int conferenceId) {
        this.conferenceId = conferenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Track track = (Track) o;

        return id == track.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int compareTo(Track o) {
        return new Integer(id).compareTo(o.getId());
    }
}
