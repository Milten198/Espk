package com.pgssoft.testwarez.database.model.companies;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.pgssoft.testwarez.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by brosol on 2016-06-03.
 */
@DatabaseTable(tableName = "company_types")
public class CompanyType {

    @DatabaseField(id = true)
    private int id;

    @ForeignCollectionField(eager = false, maxEagerLevel = 2)
    private Collection<CompanyTypeDescriptions> descriptions = new ArrayList<>();

    @DatabaseField
    private int position;

    public int getId() {
        return id;
    }

    public Collection<CompanyTypeDescriptions> getDescriptions() {
        return descriptions;
    }

    public String getTitle(){
        String lang = Utils.getLanguage();

        List<CompanyTypeDescriptions> companyTypeDescriptionsList = new ArrayList<>(descriptions);
        for (CompanyTypeDescriptions ctd: companyTypeDescriptionsList){
            if (ctd.getLang().equals(lang)){
                return ctd.getTitle();
            }
        }

        if (companyTypeDescriptionsList.size() > 0){
            return companyTypeDescriptionsList.get(0).getTitle();
        }

        return null;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompanyType that = (CompanyType) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
