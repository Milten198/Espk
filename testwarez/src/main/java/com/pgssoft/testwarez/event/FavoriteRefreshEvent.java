package com.pgssoft.testwarez.event;


import com.pgssoft.testwarez.database.model.Event;

/**
 * Created by rtulaza on 2015-08-17.
 */
public class FavoriteRefreshEvent {

    private Event event;


    public FavoriteRefreshEvent(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }
}
