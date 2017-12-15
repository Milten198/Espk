package com.pgssoft.testwarez.database.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

/**
 * Created by brosol on 2016-03-29.
 */

@DatabaseTable(tableName = "galleries_description")
public class GalleryDescription {

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String name;

    @SerializedName("created_at")
    @DatabaseField
    private DateTime createdAt;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GalleryDescription galleryDescription = (GalleryDescription) o;

        return id == galleryDescription.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
