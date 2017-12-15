package com.pgssoft.testwarez.database.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.pgssoft.testwarez.util.Utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by rtulaza on 2015-08-07.
 */

@DatabaseTable(tableName = "speakers")
public class Speaker {
    public static final String NAME_COLUMN_NAME = "name";
    public static final String SURNAME_COLUMN_NAME = "surname";
    public static final String CONFERENCE = "conference";

    @DatabaseField(id = true)
    private int id;

    @DatabaseField(columnName = NAME_COLUMN_NAME)
    private String name;

    @DatabaseField(columnName = SURNAME_COLUMN_NAME)
    private String surname;

    @DatabaseField(foreign = true, columnName = CONFERENCE)
    private Conference conference;

    @DatabaseField(foreign = true)
    private BEFile photo;

    @DatabaseField
    private String email;

    @DatabaseField
    private String phone;

    @DatabaseField
    private String skype;

    @DatabaseField
    private String facebook;

    @DatabaseField
    private String twitter;

    @DatabaseField
    private String linkedin;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    @ForeignCollectionField(eager = false)
    public Collection<SpeakerDescription> descriptions = new ArrayList<>();

    @ForeignCollectionField(eager = false)
    private Collection<EventToSpeaker> eventToSpeakers = new ArrayList<>();

    private Collection<Event> agenda;

    public Speaker() {
    }

    public int getId() {
        return id;
    }

    public BEFile getPhoto() {
        return photo;
    }

    public void setPhoto(BEFile photo) {
        this.photo = photo;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

        public List<SpeakerDescription> getDescriptions() {
            return new ArrayList<>(descriptions);
        }

    public SpeakerDescription getDescription(){
        String lang = Utils.getLanguage();

        for (SpeakerDescription sd : descriptions){
            if (sd.getLang().equals(lang)){
                return sd;
            }
        }

        if (!descriptions.isEmpty()){
            return new ArrayList<>(descriptions).get(0);
        }

        return null;
    }

    public List<Event> getAgenda() {
        return new ArrayList<>(agenda);
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getSkype() {
        return skype;
    }

    public String getFacebook() {
        return facebook;
    }

    public String getTwitter() {
        return twitter;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public List<EventToSpeaker> getEventToSpeakers() {
        return new ArrayList<>(eventToSpeakers);
    }

    public ArrayList<Event> getAllEvents() {
        ArrayList<Event> result = new ArrayList<>();
        for(EventToSpeaker ets : eventToSpeakers) {
            result.add(ets.getEvent());
        }
        return result;
    }

    public void setConference(Conference conference) {
        this.conference = conference;
    }

    public String getFullName() {
        return String.format("%s %s", name, surname);
    }

    public Conference getConference() {
        return conference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Speaker speaker = (Speaker) o;

        return id == speaker.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
