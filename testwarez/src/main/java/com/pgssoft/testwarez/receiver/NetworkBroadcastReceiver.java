package com.pgssoft.testwarez.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.event.NetworkAccessEvent;
import com.pgssoft.testwarez.event.NoNetworkAccessEvent;

/**
 * Created by rtulaza on 2015-09-02.
 */
public class NetworkBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();

        if(info != null && info.isConnected()) {
            ApplicationController.getBus().post(new NetworkAccessEvent());
        } else {
            ApplicationController.getBus().post(new NoNetworkAccessEvent());
        }
    }
}
