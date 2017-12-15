package com.pgssoft.testwarez.event;

import android.net.Uri;

/**
 * Created by brosol on 2016-05-16.
 */
public class FileDownloadedEvent {
    private Uri uri;
    private String mimeType;

    public FileDownloadedEvent(Uri uri, String mimeType) {
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
