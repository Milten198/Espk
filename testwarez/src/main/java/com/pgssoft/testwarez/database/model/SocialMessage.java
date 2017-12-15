package com.pgssoft.testwarez.database.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

/**
 * Created by dpodolak on 06.05.16.
 */
@DatabaseTable(tableName = "socialMessage")
public class SocialMessage extends Message{

    @DatabaseField
    @SerializedName("published_at")
    private DateTime publishedAt;

    @DatabaseField
    private String foto;

    @DatabaseField
    private String link;


    public DateTime getPublishedAt() {
        return publishedAt;
    }

    public String getFoto() {
        return foto;
    }

    public String getLink() {
        return link;
    }
}
