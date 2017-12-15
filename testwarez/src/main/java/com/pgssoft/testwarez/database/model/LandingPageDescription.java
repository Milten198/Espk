package com.pgssoft.testwarez.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by dpodolak on 15.04.16.
 */
@DatabaseTable(tableName = "landingPageDescription")
public class LandingPageDescription {

    public static final String LANDING_PAGE_ID = "landingPageId";
    public static final String LANG = "lang";

    @DatabaseField(id = true)
    private int id;

    @DatabaseField(columnName = LANDING_PAGE_ID)
    private int landingPageId;

    @DatabaseField(columnName = LANG)
    private String lang;

    @DatabaseField
    private String title;

    @DatabaseField
    private String description;

    @DatabaseField(foreign = true)
    private BEFile banner;

    public void setLandingPageId(int landingPageId) {
        this.landingPageId = landingPageId;
    }

    public String getDescription() {
        return description;
    }

    public BEFile getBanner() {
        return banner;
    }

    public String getTitle() {
        return title;
    }

    public String getLang() {
        return lang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LandingPageDescription landingPageDescription = (LandingPageDescription) o;

        return id == landingPageDescription.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
