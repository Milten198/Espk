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
 * Created by brosol on 2016-03-24.
 */

@DatabaseTable(tableName = "staff")
public class Staff {
    public static final String NAME_COLUMN_NAME = "name";
    public static final String SURNAME_COLUMN_NAME = "surname";
    public static final String CONFERENCE = "conference_id";

    @DatabaseField(id = true)
    private int id;

    @DatabaseField(columnName = NAME_COLUMN_NAME)
    private String name;

    @DatabaseField(columnName = SURNAME_COLUMN_NAME)
    private String surname;

    @DatabaseField(foreign = true)
    private BEFile photo;

    @DatabaseField(columnName = CONFERENCE)
    private int conferenceId;

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
    public Collection<StaffDescription> descriptions = new ArrayList<>();

    @ForeignCollectionField(eager = false)
    private Collection<EventToStaff> eventToStaff = new ArrayList<>();

    public int getId() {
        return id;
    }

    public String getName() {
        return name + " " + surname;
    }

    public BEFile getPhoto() {
        return photo;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
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

    public List<EventToStaff> getEventToStaff() {
        return new ArrayList<>(eventToStaff);
    }

    public List<StaffDescription> getDescriptions() {
        return new ArrayList<>(descriptions);
    }

    public void setConferenceId(int conferenceId) {
        this.conferenceId = conferenceId;
    }

    public StaffDescription getDescription() {
        String lang = Utils.getLanguage();

        for (StaffDescription sd : descriptions) {
            if (sd.getLang().equals(lang)) {
                return sd;
            }
        }

        if (!descriptions.isEmpty()) {
            return new ArrayList<>(descriptions).get(0);
        }

        return null;
    }

    public ArrayList<Event> getActiveEvents() {
        ArrayList<Event> result = new ArrayList<>();
        for (EventToStaff ets : eventToStaff) {
            if (ets.getEvent().isActive()) {
                result.add(ets.getEvent());
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Staff staff = (Staff) o;

        return id == staff.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
