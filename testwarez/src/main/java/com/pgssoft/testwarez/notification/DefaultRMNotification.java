package com.pgssoft.testwarez.notification;

import android.annotation.SuppressLint;
import android.widget.RemoteViews;

import com.pgssoft.testwarez.R;

import org.joda.time.DateTime;

/**
 * Created by dpodolak on 22.06.16.
 */
@SuppressLint("ParcelCreator")
public class DefaultRMNotification extends RemoteViews {

    public DefaultRMNotification(String packageName) {
        super(packageName, R.layout.notification_layout);
        setTextViewText(R.id.tvNotificationTime, new DateTime().toString("HH:mm"));
    }

    public void setContentText(String text){
        setTextViewText(android.R.id.content, text);
    }
}
