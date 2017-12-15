package com.pgssoft.testwarez.database.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.pgssoft.testwarez.util.Utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by rtulaza on 2015-08-07.
 */

@DatabaseTable(tableName = "events")
public class Event {

    public static final String PLACE_ID = "place_id";
    public static final String CONFERENCE_ID = "conferenceId";
    public static final String TECHNICAL = "technical";
    public static final String ACTIVE = "active";
    public static final String ARCHIVAL = "archival";
    public static final String RATING = "rating";
    public static final String IS_RATED = "is_rated";
    public static final String CATEGORY_ID = "category_id";

    @DatabaseField(id = true)
    private int id;

    @SerializedName("start_at")
    @DatabaseField
    private DateTime startAt;

    @SerializedName("end_at")
    @DatabaseField
    private DateTime endAt;

    @SerializedName("conference_id")
    @DatabaseField(foreign = true, columnName = CONFERENCE_ID)
    private Conference conference;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Category category;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    @DatabaseField
    private int position;

    @ForeignCollectionField(eager = false)
    private Collection<EventDescription> descriptions;

    private Collection<Speaker> speakers;

    private Collection<Track> tracks;

    private Collection<Staff> staff;

    @ForeignCollectionField(eager = false)
    private Collection<EventToSpeaker> eventToSpeakers = new ArrayList<>();

    @ForeignCollectionField(eager = false)
    private Collection<EventToTrack> eventToTracks = new ArrayList<>();

    @ForeignCollectionField(eager = false)
    private Collection<EventToStaff> eventToStaff = new ArrayList<>();

    @ForeignCollectionField(eager = false)
    private Collection<EventToArchives> eventToArchives = new ArrayList<>();

    @DatabaseField(foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Place place;

    @DatabaseField(columnName = TECHNICAL)
    private boolean technical;

    @DatabaseField(columnName = ACTIVE)
    private boolean active;

    @DatabaseField(columnName = ARCHIVAL)
    private boolean archival;

    @SerializedName("avg_feedback_rate")
    @DatabaseField(columnName = RATING)
    private float averageRating;

    @DatabaseField(columnName = IS_RATED, defaultValue = "0")
    private boolean isRated = false;

    private Collection<Archive> archives = new ArrayList<>();

    public Event() {
    }

    public int getId() {
        return id;
    }

    public DateTime getStartAt() {
        return startAt;
    }

    public DateTime getEndAt() {
        return endAt;
    }

    public Track getTracks() {
        if (!eventToTracks.isEmpty()){
            return new ArrayList<>(eventToTracks).get(0).getTrack();
        }
        return null;
    }

    public List<Track> getTrackList() {
        List<Track> trackList = new ArrayList<>();

        for (EventToTrack ett: eventToTracks){
            trackList.add(ett.getTrack());
        }

        return trackList;
    }

    public Place getPlace() {
        return place;
    }

    public Category getCategory() {
        return category;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<EventDescription> getDescriptions() {
        return new ArrayList<>(descriptions);
    }

    public List<Speaker> getJsonSpeakers() {
        if(speakers != null && !speakers.isEmpty()) {
            return new ArrayList<>(speakers);
        }

        return new ArrayList<>();
    }

    public List<Archive> getJsonArchives() {
        if(archives != null && !archives.isEmpty()) {
            return new ArrayList<>(archives);
        }

        return new ArrayList<>();
    }

    public List<Track> getJsonTracks(){
        if(tracks != null && !tracks.isEmpty()) {
            return new ArrayList<>(tracks);
        }

        return new ArrayList<>();
    }

    public List<Staff> getJsonStaff() {
        if(staff != null && !staff.isEmpty()) {
            return new ArrayList<>(staff);
        }

        return new ArrayList<>();
    }

    public List<EventToSpeaker> getEventToSpeakers() {
        return new ArrayList<>(eventToSpeakers);
    }

    public List<EventToArchives> getEventToArchives() {
        return new ArrayList<>(eventToArchives);
    }

    public List<EventToStaff> getEventToStaff() {
        return new ArrayList<>(eventToStaff);
    }

    public ArrayList<Speaker> getAllSpeakers() {
        ArrayList<Speaker> result = new ArrayList<>();
        for(EventToSpeaker ets : eventToSpeakers) {
            result.add(ets.getSpeaker());
        }
        return result;
    }

    public ArrayList<Staff> getAllStaff() {
        ArrayList<Staff> result = new ArrayList<>();
        for(EventToStaff ets : eventToStaff) {
            result.add(ets.getStaff());
        }
        return result;
    }

    public ArrayList<Archive> getAllArchives() {
        ArrayList<Archive> result = new ArrayList<>();
        for(EventToArchives eta : eventToArchives) {
            result.add(eta.getArchive());
        }
        return result;
    }


    public String getTitle() {

        String lang = Utils.getLanguage();

        for (EventDescription ed: getDescriptions()){
            if (ed.getLang().equals(lang)){
                return ed.getTitle();
            }
        }

        //if description doesn's containt specific language, and has at least one, get title from first item
        if (!getDescriptions().isEmpty()){
            return getDescriptions().get(0).getTitle();
        }

        return "";
    }

    public String getDescription() {

        String lang = Utils.getLanguage();

        for (EventDescription ed: getDescriptions()){
            if (ed.getLang().equals(lang)){
                return ed.getDescription();
            }
        }
        return "";
    }

    public boolean isTechnical() {
        return technical;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isArchival() {
        return archival;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getConferenceId() {
        return conference.getId();
    }

    public void setConference(Conference conference) {
        this.conference = conference;
    }

    public List<EventToTrack> getEventToTrack() {
        return new ArrayList<>(eventToTracks);
    }

    public String getFormattedTime() {
        return String.format("%s - %s ", getStartAt().toString("HH:mm"), getEndAt().toString("HH:mm"));
    }

    public int getPosition() {
        return position;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public boolean isRated() {
        return isRated;
    }

    public void setRated(boolean rated) {
        isRated = rated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return id == event.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public Track getLowestTrack() {
        return Collections.min(getTrackList());
    }

    public Track getSecondLowestTrack() {

        List<Track> tracks = getTrackList();
        Collections.sort(tracks, (t1, t2) -> new Integer(t1.getId()).compareTo(t2.getId()));
        return tracks.get(1);
    }
}
