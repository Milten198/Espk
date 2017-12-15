package com.pgssoft.testwarez.event;

import android.net.Uri;

/**
 * Created by brosol on 2016-05-16.
 */
public class FileOpenEvent {

    private Uri uri;
    private String mimeType;

    public FileOpenEvent() {
    }

    public FileOpenEvent(Uri uri, String mimeType) {
        this.uri = uri;
        this.mimeType = mimeType;
    }

    public Uri getUri() {
        return uri;
    }

    public String getMimeType() {
        return mimeType;
    }

}
