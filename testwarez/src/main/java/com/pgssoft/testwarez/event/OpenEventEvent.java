package com.pgssoft.testwarez.event;

import android.view.View;
import android.widget.TextView;

import com.pgssoft.testwarez.database.model.Event;

/**
 * Created by rtulaza on 2015-08-17.
 */
public class OpenEventEvent {
    public Event event;

    TextView title;
    View track;

    public OpenEventEvent(Event event) {
        this.event = event;
    }

    public TextView getTitle() {
        return title;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }

    public View getTrack() {
        return track;
    }

    public void setTrack(View track) {
        this.track = track;
    }
}
