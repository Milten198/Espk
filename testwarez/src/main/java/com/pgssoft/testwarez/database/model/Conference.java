package com.pgssoft.testwarez.database.model;

import android.support.annotation.IntDef;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.pgssoft.testwarez.database.model.companies.Organizers;
import com.pgssoft.testwarez.database.model.companies.Partners;
import com.pgssoft.testwarez.database.model.companies.Sponsors;
import com.pgssoft.testwarez.util.Utils;

import org.joda.time.DateTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by rtulaza on 2015-08-17.
 */

@DatabaseTable(tableName = "conferences")
public class Conference {

    public static final String ID = "id";
    public static final String STATUS_COLUMN = "status";
    public static final String START_AT_COLUMN = "startAt";
    public static final String IS_SYNC_COLUMN = "isSync";

    public static final int CONFERENCE_INACTIVE = 0;
    public static final int CONFERENCE_ACTIVE = 1;
    public static final int CONFERENCE_DRAFT = 2;
    public static final int CONFERENCE_ARCHIVE = 3;

    public void setStatus(int status) {
        this.status = status;
    }

    @IntDef({CONFERENCE_ACTIVE, CONFERENCE_INACTIVE, CONFERENCE_ARCHIVE, CONFERENCE_DRAFT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status{}

    @DatabaseField(id = true, columnName = ID)
    private int id;

    @DatabaseField
    private String name;

    @SerializedName("start_at")
    @DatabaseField(columnName = START_AT_COLUMN)
    private DateTime startAt;

    @SerializedName("end_at")
    @DatabaseField
    private DateTime endAt;

    /**
     * Describe three states of Conference
     * 0 - is inactive
     * 1 - is active
     * 2 - is draft
     */
    @Status
    @DatabaseField
    private int status;

    @ForeignCollectionField(eager = false, maxEagerLevel = 2)
    private Collection<ConferenceDescription> descriptions;

    @ForeignCollectionField(eager = false, maxEagerLevel = 2)
    private Collection<BuildingPlan> plans;

    @ForeignCollectionField(eager = false, maxEagerLevel = 1)
    private Collection<Organizers> organizers = new ArrayList<>() ;

    @ForeignCollectionField(eager = false, maxEagerLevel = 1)
    private Collection<Partners> partners = new ArrayList<>();

    @ForeignCollectionField(eager = false, maxEagerLevel = 1)
    private Collection<Sponsors> sponsors = new ArrayList<>();

    @ForeignCollectionField(eager = false, maxEagerLevel = 1)
    private Collection<Video> videoCollection = new ArrayList<>();

    @ForeignCollectionField(eager = false, maxEagerLevel = 1)
    private Collection<Archive> archiveCollection = new ArrayList<>();

    @ForeignCollectionField(eager = false, maxEagerLevel = 1)
    private Collection<Speaker> speakerCollection = new ArrayList<>();

    @ForeignCollectionField(eager = false, maxEagerLevel = 1)
    private Collection<Event> eventCollection = new ArrayList<>();

    @ForeignCollectionField(eager = false, maxEagerLevel = 1)
    private Collection<Language> languages = new ArrayList<>();

    @SerializedName("map_longitude")
    @DatabaseField
    private float mapLongitude;

    @SerializedName("map_latitude")
    @DatabaseField
    private float mapLatitude;

    @SerializedName("map_description")
    @DatabaseField
    private String mapDescription;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    @SerializedName("allow_tracks")
    @DatabaseField
    private boolean allowTracks;

    @DatabaseField(columnName = IS_SYNC_COLUMN)
    private boolean isSync = false;

    public Conference() {
    }

    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean sync) {
        isSync = sync;
    }

    public Conference(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DateTime getStartAt() {
        return startAt;
    }

    public DateTime getEndAt() {
        return endAt;
    }

    public ConferenceDescription getDescription() {
        String lang = Utils.getLanguage();

        ConferenceDescription conferenceDescription = null;
        for (ConferenceDescription cd: descriptions){
            if (cd.getLang().equals(lang)){
                conferenceDescription = cd;
            }
        }

        if (conferenceDescription == null ) {
            if (descriptions.size() == 1) {
                conferenceDescription = (ConferenceDescription) descriptions.toArray()[0];
            } else {
                for (ConferenceDescription cd : descriptions) {
                    if (cd.getLang().equals("en")) {
                        conferenceDescription = cd;
                    }
                }
            }
        }

        return conferenceDescription;
    }

    public Collection<BuildingPlan> getPlans() {
        return plans;
    }

    public Collection<Organizers> getOrganizers() {
        return organizers;
    }

    public Collection<Partners> getPartners() {
        return partners;
    }

    public Collection<Sponsors> getSponsors() {
        return sponsors;
    }

    public Collection<Language> getLanguages() {
        return languages;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getMapLongitude() {
        return mapLongitude;
    }

    public float getMapLatitude() {
        return mapLatitude;
    }

    public String getMapDescription() {
        return mapDescription;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public Collection<Organizers> getAllOrganizers() {
        return organizers;
    }

    public Collection<Sponsors> getAllSponsors() {
        return sponsors;
    }

    public Collection<Partners> getAllPartners() {
        return partners;
    }

    /**
     * Check if this conference is active
     * @return
     */
    public boolean isActive(){
        return status == CONFERENCE_ACTIVE;
    }

    public boolean allowTracks() {
        return allowTracks;
    }


    public Collection<Video> getVideoCollection() {
        return videoCollection;
    }

    public Collection<Archive> getArchiveCollection() {

        List<Archive> archiveList = new ArrayList<>();

        for (Event event: eventCollection){
            if (event.isArchival() && !event.getAllArchives().isEmpty() && !event.isTechnical()){
                archiveList.addAll(event.getAllArchives());
            }
        }

        return archiveList;
    }

    public Collection<Event> getEventCollection() {
        return eventCollection;
    }

    public Collection<ConferenceDescription> getDescriptions() {
        return descriptions;
    }

    @Status
    public int getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Conference that = (Conference) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("conference %25s, status %s, isSync %b",  getName(), getStatus(), isSync);
    }

    public ArrayList<Video> getVideos() {
        return new ArrayList<>(videoCollection);
    }

    /**
     *
     * @return true if event is archival and contains archives
     */
    public boolean hasArchiveEventsWithArchives() {

        boolean areArchiveEvents = false;

        for (Event event: eventCollection){
            if (event.isArchival() && !event.getAllArchives().isEmpty()){
                areArchiveEvents = true;
                break;
            }
        }

        return areArchiveEvents;
    }
}
