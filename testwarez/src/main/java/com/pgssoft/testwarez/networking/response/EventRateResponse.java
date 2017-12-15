package com.pgssoft.testwarez.networking.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brosol on 2017-07-07.
 */

public class EventRateResponse {

    private int id;
    private String message;
    private String status;
    @SerializedName("avg_feedback_rate")
    private float averageRate;

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public float getAverageRate() {
        return averageRate;
    }
}
