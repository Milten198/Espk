package com.pgssoft.testwarez.event;

import android.widget.TextView;

import com.pgssoft.testwarez.database.model.Event;

/**
 * Created by brosol on 2016-04-07.
 */
public class OpenArchiveEventEvent {
    public Event event;

    private TextView title;

    public OpenArchiveEventEvent(Event event, TextView title) {
        this.event = event;
        this.title = title;
    }

    public Event getEvent() {
        return event;
    }

    public TextView getTitle() {
        return title;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }
}
