package com.pgssoft.testwarez.event;

/**
 * Created by dpodolak on 11.04.16.
 */
public class EmptyListEvent {

    private boolean isEmpty;

    public EmptyListEvent(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    public boolean isEmpty() {
        return isEmpty;
    }
}
