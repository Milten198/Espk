package com.pgssoft.testwarez.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pgssoft.testwarez.service.EventNotificationService;

/**
 * Created by rtulaza on 2015-09-02.
 */
public class EventNotificationReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent(context, EventNotificationService.class);
        newIntent.putExtras(intent);
        context.startService(newIntent);
    }
}
