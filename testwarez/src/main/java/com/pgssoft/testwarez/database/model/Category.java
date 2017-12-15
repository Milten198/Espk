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

@DatabaseTable(tableName = "categories")
public class Category{

    public static final String START_AT_COLUMN_NAME = "startAt";
    public static final String CONFERENCE_ID = "conference_id";

    @DatabaseField(id = true)
    private int id;

    @SerializedName("start_at")
    @DatabaseField(columnName = START_AT_COLUMN_NAME)
    private DateTime startAt;

    @SerializedName("end_at")
    @DatabaseField
    private DateTime endAt;

    @ForeignCollectionField(eager = false, maxEagerLevel = 2)
    private Collection<CategoryDescription> descriptions;

    @ForeignCollectionField(eager = false)
    private Collection<Event> events;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    @DatabaseField(columnName = CONFERENCE_ID, foreign = true)
    private Conference conference;

    @DatabaseField
    private boolean hidden;


    public int getId() {
        return id;
    }

    public DateTime getStartAt() {
        return startAt;
    }

    public DateTime getEndAt() {
        return endAt;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<CategoryDescription> getDescriptions() {
        return new ArrayList<>(descriptions);
    }

    public List<Event> getEvents() {
        return new ArrayList<>(events);
    }

    public String getTitle() {

        String lang = Utils.getLanguage();

        List<CategoryDescription> descriptions = getDescriptions();
        for (CategoryDescription cd : descriptions){
            if (lang.equals(cd.getLang())){
                return cd.getTitle();
            }
        }

        if (!descriptions.isEmpty()){
            return descriptions.get(0).getTitle();
        }
        return null;

    }

    public boolean isHidden() {
        return hidden;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return id == category.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
