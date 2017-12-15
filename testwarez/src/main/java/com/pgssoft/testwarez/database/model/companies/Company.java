package com.pgssoft.testwarez.database.model.companies;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.pgssoft.testwarez.database.model.BEFile;

import org.joda.time.DateTime;

/**
 * Created by rtulaza on 2015-08-17.
 */

@DatabaseTable(tableName = "company")
public class Company {

    @DatabaseField(id = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh=true, maxForeignAutoRefreshLevel=4)
    private BEFile logo;

    @DatabaseField
    private String homepage;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    public int getId() {
        return id;
    }

    public BEFile getLogo() {
        return logo;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getHomepage() {
        return homepage;
    }
}
