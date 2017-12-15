package com.pgssoft.testwarez.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.feature.event.EventActivity;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.util.Utils;
import com.pgssoft.testwarez.widget.SettingsView;

import java.sql.SQLException;

/**
 * Created by dpodolak on 07.06.16.
 */
public class EventNotificationService extends IntentService {

    public static final String EXTRA_EVENT_ID = "extra_event_id";
    
    public static final String TAG = "EventNotificationService";

    public EventNotificationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int id = intent.getIntExtra(EXTRA_EVENT_ID, -1);

        if(id == -1) {
            return;
        }


        try {
            Event event = ApplicationController.getDatabaseHelper().getEventDao().queryForId(id);
            if(event == null) {
                return;
            }

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this, "")
                            .setSmallIcon(R.drawable.ic_notification_icon_grey)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_testwarez_logo_circle))
                            .setColor(getResources().getColor(R.color.icon_bg))
                            .setContentTitle(getResources().getString(R.string.notification_title));

            Intent eventIntent = new Intent(this, EventActivity.class);
            eventIntent.putExtra(EventActivity.EVENT_ID, id);
            PendingIntent eventPendingIntent = PendingIntent.getActivity(this, 0, eventIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(eventPendingIntent);
            mBuilder.setAutoCancel(true);

            String message;

            if(Utils.getNotificationsTime() == SettingsView.REMINDER_TIMES.ON_START) {
                message = getResources().getString(R.string.notification_content_onstart,
                                event.getDescriptions().size() > 0 ? event.getDescriptions().get(0).getTitle() : "");

            } else {
                message = getResources().getString(R.string.notification_content_time,
                                event.getDescriptions().size() > 0 ? event.getDescriptions().get(0).getTitle() : "",
                                getResources().getStringArray(R.array.notification_times)[Utils.getNotificationsTime().ordinal()]);
            }

            mBuilder.setContentText(message);
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message)
                    .setBigContentTitle(getResources().getString(R.string.notification_title)));

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(event.getId(), mBuilder.build());
        }
        catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
