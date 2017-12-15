package com.pgssoft.testwarez.database.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by dpodolak on 06.06.16.
 */

@DatabaseTable(tableName = "language")
public class Language {

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String language;

    @SerializedName("is_default")
    @DatabaseField
    private boolean isDefault;

    @DatabaseField(foreign = true)
    private Conference conference;

    public Language() {
    }

    public boolean isDeafault() {
        return isDefault;
    }

    public String getLanguage() {
        return language;
    }

    public void setConference(Conference conference) {
        this.conference = conference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Language language = (Language) o;

        return id == language.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
