package com.pgssoft.testwarez.database.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

/**
 * Created by brosol on 2016-03-25.
 */
@DatabaseTable(tableName = "staffDescriptions")
public class StaffDescription {

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String lang;

    @DatabaseField
    private String job;

    @DatabaseField
    private String company;

    @DatabaseField
    private String biography;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Staff staff;

    public StaffDescription() {
    }

    public int getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    public String getJob() {
        return job;
    }

    public String getBiography() {
        return biography;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getCompany() {
        return company;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StaffDescription staffDescription = (StaffDescription) o;

        return id == staffDescription.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
