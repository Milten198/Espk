package com.pgssoft.testwarez.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by dpodolak on 22.03.16.
 */
public class RequestConferenceEvent {
    public static final int REQUEST_CONFERENCE_SUCCESS = 0;
    public static final int REQUEST_CONFERENCE_ERROR = 1;

    @IntDef
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status{}

    private int status;

    public RequestConferenceEvent(@Status int status) {
        this.status = status;
    }

    @Status
    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        switch (status){
            case REQUEST_CONFERENCE_ERROR: return "status error";
            case REQUEST_CONFERENCE_SUCCESS: return "status success";
        }

        return String.valueOf(status);
    }
}
