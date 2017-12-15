package com.pgssoft.testwarez.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.feature.messages.MessagesActivity;
import com.pgssoft.testwarez.event.NotificationMessageEvent;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.notification.DefaultRMNotification;
import com.pgssoft.testwarez.notification.ExpandedRMNotification;
import com.pgssoft.testwarez.receiver.DeleteNotificationReceiver;
import com.pgssoft.testwarez.util.Utils;

import java.sql.SQLException;
import java.util.Map;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by rtulaza on 2015-09-02.
 */
public class MyGcmListenerService extends GcmListenerService {
    private static final int DELETED_REQUEST_CODE = 101;

    private static final int MAX_MESSAGE_IN_NOTIFICATION = 5;
    private final String SERVICE_UPDATED = "service-updated";

    private static final String NOTIFICATION_PREFS = "notification_prefs";

    private static final String CONTENT_GROUP = "content_group";

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private SharedPreferences sharedPreferences;
    private NotificationManager mNotificationManager;
    private PendingIntent deletedPendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(NOTIFICATION_PREFS, MODE_PRIVATE);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent deletedIntent = new Intent(this, DeleteNotificationReceiver.class);
        deletedPendingIntent = PendingIntent.getBroadcast(this, DELETED_REQUEST_CODE, deletedIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Content content = new Content(data.getString("message"));

        if (SERVICE_UPDATED.equals(content.getValue())) {
            if (data.containsKey("conference-id")) {
                int conferenceId = Integer.valueOf(data.getString("conference-id"));

                try {
                    UpdateBuilder<Conference, Integer> conferenceUB = ApplicationController.getDatabaseHelper().getConferenceDao().updateBuilder();
                    conferenceUB.where().eq(Conference.ID, conferenceId);
                    conferenceUB.updateColumnValue(Conference.IS_SYNC_COLUMN, false);
                    int result = conferenceUB.update();

                    startService(new Intent(this, SyncService.class));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        } else if (content != null && !content.isEmpty() && Utils.isNotificationGeneral()) {

            syncMessage();

            if (shouldDisplaySingleNotification()) {
                saveNotification(content);
                showSingleNotification(content);
            } else {
                saveNotification(content);
                showMultiNotifications();
            }
        }

        super.onMessageReceived(from, data);
    }

    private void showMultiNotifications() {

        int notificationCount = getNotificationCount();
        Map<String, String> previousNotifications = getPreviousNotifications();

        ExpandedRMNotification.Builder expandedNotificationBuilder = new ExpandedRMNotification.Builder(getPackageName());
        DefaultRMNotification defaultRMNotification = new DefaultRMNotification(getPackageName());

        mNotificationManager.cancelAll();

        if (notificationCount < MAX_MESSAGE_IN_NOTIFICATION) {
            for (String key : previousNotifications.keySet()) {
                expandedNotificationBuilder.addContentLine(previousNotifications.get(key));
            }
        } else {
            for (int i = 0; i < (MAX_MESSAGE_IN_NOTIFICATION - 1); i++) {
                String key = (String) previousNotifications.keySet().toArray()[i];
                expandedNotificationBuilder.addContentLine(previousNotifications.get(key));
            }
            expandedNotificationBuilder.setSummary(getString(R.string.notification_summary_text, "+", (MAX_MESSAGE_IN_NOTIFICATION - 1)));
        }

        Intent messagesIntent = new Intent(this, MessagesActivity.class);
        messagesIntent.putExtra(MessagesActivity.OPEN_ORGANIZER_MESSAGES, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, messagesIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        defaultRMNotification.setContentText(getString(R.string.notification_summary_text, (notificationCount < MAX_MESSAGE_IN_NOTIFICATION ? "" : "+"), (notificationCount < MAX_MESSAGE_IN_NOTIFICATION ? notificationCount : MAX_MESSAGE_IN_NOTIFICATION - 1)));

        Notification summaryNotification = new NotificationCompat.Builder(this, "")
                .setSmallIcon(R.drawable.ic_notification_icon_grey)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_testwarez_logo_circle))
                .setGroup(CONTENT_GROUP)
                .setContent(defaultRMNotification)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setDeleteIntent(deletedPendingIntent)
                .setColor(getResources().getColor(R.color.primary))
                .setContentIntent(pendingIntent)
                .build();

        summaryNotification.bigContentView = expandedNotificationBuilder.build();

        mNotificationManager.notify(100, summaryNotification);

    }

    private void showSingleNotification(Content content) {


        Intent messagesIntent = new Intent(this, MessagesActivity.class);
        messagesIntent.putExtra(MessagesActivity.OPEN_ORGANIZER_MESSAGES, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, messagesIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        DefaultRMNotification notificationContent = new DefaultRMNotification(getPackageName());

        notificationContent.setContentText(content.getValue());

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "")
                        .setSmallIcon(R.drawable.ic_notification_icon_grey)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_testwarez_logo_circle))
                        .setContent(notificationContent)
                        .setDeleteIntent(deletedPendingIntent)
                        .setContentIntent(pendingIntent)
                        .setShowWhen(true)
                        .setAutoCancel(true);

        Notification notificationCompat = builder.build();

        builder.setAutoCancel(true);
        mNotificationManager.notify(content.hashCode(), notificationCompat);
    }


    private boolean shouldDisplaySingleNotification() {

        return getNotificationCount() == 0;
    }

    private void syncMessage() {
        if (ApplicationController.getActiveConference() != null) {
            Conference conference = ApplicationController.getActiveConference();

            compositeSubscription.add(ApplicationController.getNetworkInterface().loadMessages(conference.getId())
                    .flatMap(Observable::from)
                    .subscribe(message -> {
                        message.setConferenceId(conference.getId());
                        try {
                            ApplicationController.getDatabaseHelper().getMessageDao().createOrUpdate(message);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }, Throwable::printStackTrace, () -> ApplicationController.getBus().post(new NotificationMessageEvent())));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
        compositeSubscription.unsubscribe();
    }


    private void saveNotification(Content content) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(String.valueOf(content.hashCode()), content.getValue());
        edit.commit();
    }

    public static void clearNotification(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NOTIFICATION_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.commit();
    }

    public int getNotificationCount() {
        return sharedPreferences.getAll().size();
    }

    public Map<String, String> getPreviousNotifications() {
        return (Map<String, String>) sharedPreferences.getAll();
    }

    private static class Content {

        private String content;
        private int hashCode = 0;

        public Content(String content) {
            this.content = content;
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                hashCode = (int) (super.hashCode() * System.currentTimeMillis() / 1000);
            }
            return hashCode;
        }

        public boolean isEmpty() {
            return content.isEmpty();
        }

        public String getValue() {
            return content;
        }
    }

}

