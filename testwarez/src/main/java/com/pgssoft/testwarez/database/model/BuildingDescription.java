package com.pgssoft.testwarez.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by dpodolak on 26.04.16.
 */
@DatabaseTable(tableName = "buildingDescription")
public class BuildingDescription {

    public static final String BUILDING_PLAN = "building_plan";

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String lang;

    @DatabaseField
    private String name;

    @DatabaseField(columnName = BUILDING_PLAN, foreign = true)
    private BuildingPlan buildingPlan;

    public void setBuildingPlan(BuildingPlan buildingPlan) {
        this.buildingPlan = buildingPlan;
    }

    public String getLang() {
        return lang;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuildingDescription bd = (BuildingDescription) o;

        return id == bd.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
