package com.pgssoft.testwarez.event;

/**
 * Created by brosol on 2017-07-24.
 */

public class AgendaScrollEvent {

    private int position;

    public AgendaScrollEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
