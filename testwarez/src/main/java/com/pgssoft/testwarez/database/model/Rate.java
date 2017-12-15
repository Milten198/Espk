package com.pgssoft.testwarez.database.model;

/**
 * Created by brosol on 2017-07-07.
 */

public class Rate {

    private int rate;
    private String message;

    public Rate(int rate, String message) {
        this.rate = rate;
        this.message = message;
    }

    public int getRate() {
        return rate;
    }

    public String getMessage() {
        return message;
    }
}
