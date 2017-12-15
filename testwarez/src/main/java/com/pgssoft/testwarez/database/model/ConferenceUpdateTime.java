package com.pgssoft.testwarez.database.model;

import org.joda.time.DateTime;

/**
 * Created by brosol on 2017-07-20.
 */

public class ConferenceUpdateTime {

    private int id;
    private DateTime updatedAt;

    public int getId() {
        return id;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }
}
