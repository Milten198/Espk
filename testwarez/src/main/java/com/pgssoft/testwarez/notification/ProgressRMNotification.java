package com.pgssoft.testwarez.notification;

import android.annotation.SuppressLint;
import android.widget.RemoteViews;

import com.pgssoft.testwarez.R;

import org.joda.time.DateTime;

/**
 * Created by dpodolak on 22.06.16.
 */
@SuppressLint("ParcelCreator")
public class ProgressRMNotification extends RemoteViews {

    public ProgressRMNotification(String packageName) {
        super(packageName, R.layout.notification_progress_layout);
        setTextViewText(R.id.tvNotificationTime, new DateTime().toString("HH:mm"));
    }

    public void setContentText(String text){
        setTextViewText(android.R.id.content, text);
    }
    public void setProgress(int max, int progress){
        setProgressBar(R.id.pbNotificationProgressBar, max, progress, false);
    }


    public void setContentTitle(String title) {
        setTextViewText(android.R.id.title, title);
    }
}
