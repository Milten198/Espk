package com.pgssoft.testwarez.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pgssoft.testwarez.service.MyGcmListenerService;

/**
 * Created by dpodolak on 23.05.16.
 */
public class DeleteNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MyGcmListenerService.clearNotification(context);
    }
}
