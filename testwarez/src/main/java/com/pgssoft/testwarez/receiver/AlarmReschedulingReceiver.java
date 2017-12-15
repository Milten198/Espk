package com.pgssoft.testwarez.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pgssoft.testwarez.service.AlarmReschedulingIntentService;

/**
 * Created by rtulaza on 2015-09-04.
 */
public class AlarmReschedulingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
       context.startService(new Intent(context, AlarmReschedulingIntentService.class));
    }
}
