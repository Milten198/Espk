package com.pgssoft.testwarez.database.model.companies;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by brosol on 2016-06-03.
 */
@DatabaseTable(tableName = "company_type_descriptions")
public class CompanyTypeDescriptions {

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String lang;

    @DatabaseField
    private String title;

    @DatabaseField(foreign = true)
    private CompanyType companyType;

    public int getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    public String getTitle() {
        return title;
    }

    public CompanyType getCompanyType() {
        return companyType;
    }

    public void setCompanyType(CompanyType companyType) {
        this.companyType = companyType;
    }
}
