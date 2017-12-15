package com.pgssoft.testwarez.service;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

/**
 * Created by brosol on 2016-05-16.
 */
public class FileDownloadService extends IntentService {

    public FileDownloadService() {
        super(FileDownloadService.class.getName());
    }

    public FileDownloadService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String url = intent.getStringExtra("url");
        String fileName = intent.getStringExtra("fileName");
        String mimeType = intent.getStringExtra("mimeType");

        downloadAndOpenFile(url, fileName, mimeType);
    }

    private void downloadAndOpenFile(final String url, final String fileName, final String mimeType) {

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request;

        request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(url.substring(url.lastIndexOf('/') + 1));
        request.setMimeType(mimeType);
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, fileName);
        request.setTitle(fileName);

        downloadManager.enqueue(request);
    }
}
