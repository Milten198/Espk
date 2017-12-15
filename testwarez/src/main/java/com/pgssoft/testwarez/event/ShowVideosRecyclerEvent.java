package com.pgssoft.testwarez.event;

/**
 * Created by brosol on 2016-06-21.
 */
public class ShowVideosRecyclerEvent {

    private boolean show;

    public ShowVideosRecyclerEvent(boolean show) {
        this.show = show;
    }

    public boolean isShow() {
        return show;
    }
}
