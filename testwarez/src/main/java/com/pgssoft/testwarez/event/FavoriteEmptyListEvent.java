package com.pgssoft.testwarez.event;

/**
 * Created by dpodolak on 27.04.16.
 */
public class FavoriteEmptyListEvent {

    private boolean isEmpty;

    public FavoriteEmptyListEvent(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    public boolean isEmpty() {
        return isEmpty;
    }
}
