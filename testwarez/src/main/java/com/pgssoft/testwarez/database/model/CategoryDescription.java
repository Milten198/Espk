package com.pgssoft.testwarez.database.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

/**
 * Created by rtulaza on 2015-08-07.
 */

@DatabaseTable(tableName = "categoryDescriptions")
public class CategoryDescription {

    public static final String CATEGORY_ID = "category_id";

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String lang;

    @DatabaseField(columnName = CATEGORY_ID, foreign = true)
    private Category category;

    @DatabaseField
    private String title;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    public int getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    public Category getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CategoryDescription categoryDescription = (CategoryDescription) o;

        return id == categoryDescription.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
