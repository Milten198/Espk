package com.pgssoft.testwarez.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import timber.log.Timber;

/**
 * Created by dpodolak on 25.02.16.
 */
public class SyncDataEvent {

    public final static int EMPTY_STATE = -1;
    public final static int IS_ACTIVE_CONFERENCE = 0;
    public final static int DOWNLOAD_ACTIVE_CONFERENCE_CONTENT = 1;
    public final static int DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT = 2;
    public final static int SYNC_ARCHIVE_COMPLETE = 3;
    public final static int IS_LANDING_PAGE = 4;
    public final static int START_SYNC_ACTIVE_CONFERENCE = 5;
    public final static int SYNC_ERROR = 6;
    public final static int START_SYNCING = 7;

    @IntDef({EMPTY_STATE, SYNC_ARCHIVE_COMPLETE,
            IS_ACTIVE_CONFERENCE, DOWNLOAD_ACTIVE_CONFERENCE_CONTENT, DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT,
            IS_LANDING_PAGE, START_SYNC_ACTIVE_CONFERENCE, SYNC_ERROR, START_SYNCING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SyncState {}

    @SyncState
    int state = EMPTY_STATE;



    public SyncDataEvent() {

    }
    public SyncDataEvent(@SyncState int state) {
        this.state = state;
        Timber.d("SyncDataEvent:SyncDataEvent: %d", state);
    }

    @SyncState
    public int getSyncState() {
        return state;
    }

    @Override
    public String toString() {
        return String.format("SyncDataEvent: %15s(%d)", getName(), state);
    }

    public String getName(){
        switch (state){
            case EMPTY_STATE: return "EMPTY_STATE";
            case IS_ACTIVE_CONFERENCE: return "IS_ACTIVE_CONFERENCE";
            case DOWNLOAD_ACTIVE_CONFERENCE_CONTENT: return "DOWNLOAD_ACTIVE_CONFERENCE_CONTENT";
            case DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT: return "DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT";
            case SYNC_ARCHIVE_COMPLETE: return "SYNC_ARCHIVE_COMPLETE";
            case IS_LANDING_PAGE: return "IS_LANDING_PAGE";
            case START_SYNC_ACTIVE_CONFERENCE: return "START_SYNC_ACTIVE_CONFERENCE";
            case SYNC_ERROR: return "SYNC_ERROR";
            case START_SYNCING: return "START_SYNCING";
            default: return "SYNC_ERROR";
        }
    }
}
