package com.pgssoft.testwarez.database.model.companies;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.pgssoft.testwarez.database.model.Conference;

/**
 * Created by brosol on 2016-06-03.
 */
public class CompanyEntity {

    @DatabaseField(id = true)
    private int id;

    @NonNull
    @DatabaseField(foreign = true, foreignAutoRefresh=true, maxForeignAutoRefreshLevel=3)
    private Company company;

    @DatabaseField
    private int position;

    @SerializedName("company_type")
    @DatabaseField(foreign = true, foreignAutoRefresh=true, maxForeignAutoRefreshLevel=3)
    private CompanyType companyType;

    @DatabaseField(foreign = true)
    private Conference conference;

    public CompanyEntity() {
    }

    public int getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public int getPosition() {
        return position;
    }

    public CompanyType getCompanyType() {
        return companyType;
    }

    @Override
    public String toString() {
        return String.format("Company type position %d, company position %d", getCompanyType().getPosition(), getPosition());
    }

    public void setConference(Conference conference) {
        this.conference = conference;
    }
}
