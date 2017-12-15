package com.pgssoft.testwarez.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.pgssoft.testwarez.util.Utils;

/**
 * Created by brosol on 2016-05-16.
 */
public class FileDownloadedReceiver extends BroadcastReceiver {

    public static Long downloadedFileID = -1L;

    public FileDownloadedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        Intent fileDownloadedIntent = new Intent("file_downloaded");
        fileDownloadedIntent.putExtra("file_id", bundle.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
        LocalBroadcastManager.getInstance(context).sendBroadcast(fileDownloadedIntent);
        Utils.setDownloadedFileId(downloadedFileID);
    }
}
