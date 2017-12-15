package com.pgssoft.testwarez.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Pair;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseNavigationDrawerActivity;
import com.pgssoft.testwarez.event.SyncDataEvent;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.database.model.ConferenceUpdateTime;
import com.pgssoft.testwarez.database.model.Favorite;
import com.pgssoft.testwarez.networking.response.ArchivesResponse;
import com.pgssoft.testwarez.networking.response.BuildingPlanResponse;
import com.pgssoft.testwarez.networking.response.CategoriesResponse;
import com.pgssoft.testwarez.networking.response.EventsResponse;
import com.pgssoft.testwarez.networking.response.PlacesResponse;
import com.pgssoft.testwarez.networking.response.SpeakersResponse;
import com.pgssoft.testwarez.networking.response.StaffResponse;
import com.pgssoft.testwarez.networking.response.TrackResponse;
import com.pgssoft.testwarez.networking.response.VideoResponse;
import com.pgssoft.testwarez.notification.DefaultRMNotification;
import com.pgssoft.testwarez.notification.ProgressRMNotification;
import com.pgssoft.testwarez.util.DatabaseUtils;
import com.pgssoft.testwarez.util.Observables;
import com.pgssoft.testwarez.util.Utils;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by dpodolak on 13.05.16.
 */
public class SyncService extends Service {

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public static final String DISABLE_SYNC_DATA = "disable_sync_data";

    private static final int NO_CONNECTION_NOTIFICATION_ID = 104;

    private static final int NOTIFICATION_ID = 100;

    private int conferenceToSync, currentConference;

    NotificationCompat.Builder mBuilder;
    ProgressRMNotification progressRMNotification;

    @SyncDataEvent.SyncState
    public int lastSyncEvent = SyncDataEvent.EMPTY_STATE;

    private NotificationManager notificationManager;

    private List<Integer> conferenceIdsToRegister = new ArrayList<>();

    private boolean syncServiceRunning;
    private boolean disableSyncDate;

    /**
     * Synchronize only active conference (if exists)
     *
     * @param conference active conference
     * @return observable which execute all necessary operation related to active conference
     */
    @NonNull
    private Observable<Conference> getActiveConferenceObservable(Conference conference) {
        return Observable.just(conference)
                .flatMap(conf -> {
                    if (conf.isSync()) {
                        return Observable.just(conference)
                                .flatMap(conference2 -> Observables.getInstance().refreshAgendaElements(this).takeLast(1).map(listItems -> conference2))
                                .doOnNext(conference1 -> {
                                    ApplicationController.getActiveConference();
                                    if (conference1.isSync()) {
                                        ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.IS_ACTIVE_CONFERENCE));
                                    }
                                });
                    } else {
                        return Observable.just(conf)
                                .doOnNext(conference1 -> ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.START_SYNC_ACTIVE_CONFERENCE)))
                                .flatMap(this::getSyncConferenceObservable)
                                .doOnError(throwable -> {
                                    showNoConnectionNotification();
                                    ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.SYNC_ERROR));
                                })
                                .doOnNext(conference1 -> {

                                    conf.setSync(true);
                                    try {

                                        ApplicationController.getDatabaseHelper().beginTransaction();
                                        ApplicationController.getDatabaseHelper().getConferenceDao().update(conf);
                                        ApplicationController.getDatabaseHelper().endTransaction();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    ApplicationController.refreshActiveConference();
                                })
                                .flatMap(conference2 -> Observables.getInstance().refreshAgendaElements(this).takeLast(1).map(listItems -> conference2))
                                .doOnNext(conference3 ->
                                        ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.DOWNLOAD_ACTIVE_CONFERENCE_CONTENT)));
                    }
                })
                .doOnNext(conference2 -> {
                    ApplicationController.refreshActiveConference();
                    checkAndRemoveFavorites(conference2);
                });
    }

    /**
     * Register conference with deviceID in Backend
     *
     * @return Observable with above conference
     */
    private Observable<Integer> getRegisterObservable() {
        if (!conferenceIdsToRegister.isEmpty()) {
            return getGCMRegisterObservable()
                    .flatMap(token -> ApplicationController.getNetworkInterface().sendDeviceId("json", token, (ArrayList<Integer>) conferenceIdsToRegister))
                    .doOnNext(objectResponse -> {
                        if (objectResponse.code() == 201) {
                            for (Integer id : conferenceIdsToRegister) {
                                Utils.setGCMTokenPerConference(id);
                            }
                        }
                    })
                    .doOnError(throwable -> {
                        throwable.printStackTrace();
                        stopForeground(true);
                        showNoConnectionNotification();
                        ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.SYNC_ERROR));
                    })
                    .flatMap(objectResponse -> Observable.just(1));
        } else {
            return Observable.just(1);
        }
    }

    /**
     * Synchronize archive conferences with all components which are not synced
     */
    private Observable<Integer> getArchiveConference() {
        Utils.setIsAllArchiveDataDownloaded(false);
        return Observables.getInstance().getConferenceObservables().refreshArchiveConferences()

                .filter(conference -> conference.getStatus() == Conference.CONFERENCE_ARCHIVE)
                .filter(conference -> !conference.isSync())
                .toSortedList((conference, conference2) -> new Long(conference2.getStartAt().getMillis()).compareTo(conference.getStartAt().getMillis()))
                .flatMap(conferences -> {
                    conferenceToSync = conferences.size();

                    if (conferenceToSync > 0) {


                        if (progressRMNotification == null) {
                            progressRMNotification = new ProgressRMNotification(getPackageName());
                            progressRMNotification.setContentTitle(getString(R.string.notification_sync_archive));

                            mBuilder = new NotificationCompat.Builder(getApplicationContext(), "");
                        }

                        progressRMNotification.setProgress(conferenceToSync, currentConference++);
                        progressRMNotification.setContentText("0/" + conferenceToSync);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mBuilder.setSmallIcon(R.drawable.notification_icon_grey_svg);
                        } else {
                            mBuilder.setSmallIcon(R.drawable.ic_notification_icon_grey);
                        }

                        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_testwarez_logo_circle));

                        mBuilder.setColor(ContextCompat.getColor(this, R.color.icon_bg));
                        mBuilder.setContent(progressRMNotification);


                        startForeground(NOTIFICATION_ID, mBuilder.build());
                        return Observable.from(conferences)
                                .flatMap(this::getSyncConferenceObservable)

                                .flatMap(conference1 -> ApplicationController.getNetworkInterface().loadGalleriesDescriptionsResponse(conference1.getId())
                                        .flatMap(galleryDescriptions -> {
                                            if (galleryDescriptions != null) {
                                                return Observable.from(galleryDescriptions)
                                                        .flatMap(galleryDescription ->
                                                                ApplicationController.getNetworkInterface().loadGalleriesResponse(galleryDescription.getId()))
                                                        .map(gallery -> {
                                                            gallery.setConferenceId(conference1.getId());
                                                            return gallery;
                                                        })
                                                        .toList()
                                                        .doOnNext(galleries1 -> DatabaseUtils.updateGalleries(galleries1, (Conference) conference1))
                                                        .map(galleries -> conference1);
                                            }

                                            return Observable.just(conference1);
                                        }))
                                .doOnError(throwable -> {
                                    throwable.printStackTrace();
                                    stopForeground(true);
                                    showNoConnectionNotification();
                                    ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.SYNC_ERROR));
                                })
                                .map(o -> (Conference) o)
                                .doOnNext(conference -> {
                                    int i = currentConference++;

                                    progressRMNotification.setProgress(conferenceToSync, i);
                                    progressRMNotification.setContentText(i + "/" + conferenceToSync);

                                    mBuilder.setContent(progressRMNotification);
                                    startForeground(NOTIFICATION_ID, mBuilder.build());
                                    conference.setSync(true);
                                    try {
                                        ApplicationController.getDatabaseHelper().beginTransaction();
                                        ApplicationController.getDatabaseHelper().getConferenceDao().update(conference);
                                        ApplicationController.getDatabaseHelper().endTransaction();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT));
                                })
                                .takeLast(1)
                                .flatMap(conference2 -> {
                                    stopForeground(true);
                                    return inflateArchiveObservable();
                                });
                    } else {
                        return inflateArchiveObservable();
                    }
                });
    }

    private void showNoConnectionNotification() {
        Intent messagesIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, messagesIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        DefaultRMNotification defaultRMNotification = new DefaultRMNotification(getPackageName());
        defaultRMNotification.setContentText(getResources().getString(R.string.synchronization_error));

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContent(defaultRMNotification)
                        .setContentIntent(pendingIntent)
                        .setColor(getResources().getColor(R.color.icon_bg))
                        .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.notification_icon_grey_svg);
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_icon_grey);
        }

        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_testwarez_logo_circle));

        Notification notificationCompat = builder.build();

        builder.setAutoCancel(true);


        notificationManager.notify(NO_CONNECTION_NOTIFICATION_ID, notificationCompat);
    }

    long confExec;

    /**
     * Synchronize all allow conferences
     */
    private Observable<Integer> conferencesObservable = Observable.just(1)
            .doOnNext(i -> Utils.setUpdateInProgress(true))
            .flatMap(aVoid -> ApplicationController.getNetworkInterface().loadConferences())
            .doOnError(throwable -> {
                if (ApplicationController.getActiveConference() == null || !ApplicationController.getActiveConference().isSync()) {
                    showNoConnectionNotification();
                }
                ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.SYNC_ERROR));
            })
            .doOnNext(conferences -> ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.START_SYNCING)))
            .map(conferences -> {
                Collections.sort(conferences, (c1, c2) -> c2.getStatus() == Conference.CONFERENCE_ACTIVE ? 1 : -1);
                confExec = System.currentTimeMillis();
                return conferences;
            })
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(Observable::from)
            .observeOn(Schedulers.io())
            .flatMap(conference -> {
                try {
                    ApplicationController.getDatabaseHelper().beginTransaction();
                    conference = DatabaseUtils.updateConference(conference);
                    ApplicationController.getDatabaseHelper().endTransaction();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                addConferenceToRegister(conference);

                if (conference.getStatus() == Conference.CONFERENCE_ACTIVE) {
                    return getActiveConferenceObservable(conference);
                } else {
                    return Observable.just(conference);
                }
            })
            .toList()
            .flatMap(confList -> {
                Timber.d("SyncService: conference save execution in time: %d ms", System.currentTimeMillis() - confExec);
                if (ApplicationController.getActiveConference() == null) {
                    ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.IS_LANDING_PAGE));
                }
                return getArchiveConference().flatMap(integer -> getRegisterObservable());
            });


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager.cancel(NO_CONNECTION_NOTIFICATION_ID);
        currentConference = 0;

        if (lastSyncEvent != SyncDataEvent.EMPTY_STATE) {
            ApplicationController.getBus().post(new SyncDataEvent(lastSyncEvent));
        }

        disableSyncDate = intent != null && intent.getBooleanExtra(DISABLE_SYNC_DATA, false);

        if (Utils.isInternetConnection(this)) {
            checkIfDataIsUpToDate();
        } else {
            compositeSubscription.add(Observable.defer(() -> Observable.just(ApplicationController.getActiveConference()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(conference -> {
                        if (conference != null && conference.isSync()) {
                            ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.IS_ACTIVE_CONFERENCE));
                            stopSelf();
                        } else {
                            showNoConnectionNotification();
                            ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.SYNC_ERROR));
                        }
                    }, this::showError));
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        ApplicationController.getBus().register(this);
    }

    private void checkIfDataIsUpToDate() {
        compositeSubscription.add(ApplicationController.getNetworkInterface().getConferencesUpdateTimes()
                .doOnError(Timber::i)
                .map(conferenceUpdateTimes -> {
                    Conference activeConference = ApplicationController.getActiveConference();
                    boolean isActiveConferenceUpToDate = false;

                    for (ConferenceUpdateTime conferenceUpdateTime : conferenceUpdateTimes) {
                        if (activeConference != null && activeConference.isSync() && activeConference.getId() == conferenceUpdateTime.getId()
                                && (activeConference.getUpdatedAt().isAfter(conferenceUpdateTime.getUpdatedAt())
                                || activeConference.getUpdatedAt().isEqual(conferenceUpdateTime.getUpdatedAt()))) {
                            isActiveConferenceUpToDate = true;
                            break;
                        }
                    }

                    return new Pair<Boolean, List<ConferenceUpdateTime>>(isActiveConferenceUpToDate, conferenceUpdateTimes);
                })
                .flatMap(pair -> {
                    if (pair.first) {
                        // archive conferences update
                        ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.IS_ACTIVE_CONFERENCE));

                        // For every archive conference set isSync flag to false if conference should be synchronized
                        for (Conference conference : ApplicationController.getArchiveConferences()) {
                            for (ConferenceUpdateTime conferenceUpdateTime : pair.second) {
                                if (conferenceUpdateTime != null && conferenceUpdateTime.getId() == conferenceUpdateTime.getId()
                                        && (conferenceUpdateTime.getUpdatedAt().isAfter(conferenceUpdateTime.getUpdatedAt())
                                        || conferenceUpdateTime.getUpdatedAt().isEqual(conferenceUpdateTime.getUpdatedAt()))) {
                                    conference.setSync(false);
                                    Utils.setIsAllArchiveDataDownloaded(false);
                                    Utils.setUpdateInProgress(true);

                                    try {
                                        ApplicationController.getDatabaseHelper().beginTransaction();
                                        ApplicationController.getDatabaseHelper().getConferenceDao().update(conference);
                                        ApplicationController.getDatabaseHelper().endTransaction();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        return getArchiveConference().flatMap(integer -> {
                            Utils.setIsAllArchiveDataDownloaded(true);
                            Utils.setUpdateInProgress(false);
                            return getRegisterObservable();
                        });
                    } else {
                        if (!disableSyncDate && !syncServiceRunning) {
                            syncServiceRunning = true;
                            return syncData();
                        }

                        return Observable.empty();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                }, this::showError));
    }

    long execTime;

    private Observable<Integer> syncData() {
        execTime = System.currentTimeMillis();
        return conferencesObservable
                .doOnError(Throwable::printStackTrace)
                .doOnCompleted(() -> {
                    Timber.d("SyncService:syncData: execution in time %d ms", System.currentTimeMillis() - execTime);
                    Utils.setUpdateInProgress(false);
                    Utils.setIsAllArchiveDataDownloaded(true);
                    stopSelf();
                });
    }

    private Observable<Conference> getSyncConferenceObservable(Conference conference) {
        return Observable.zip(ApplicationController.getNetworkInterface().loadTracks(conference.getId()),
                ApplicationController.getNetworkInterface().loadEvents(conference.getId()),
                ApplicationController.getNetworkInterface().loadCategories(conference.getId()),
                ApplicationController.getNetworkInterface().loadPlaces(conference.getId()),
                ApplicationController.getNetworkInterface().loadSpeakers(conference.getId()),
                ApplicationController.getNetworkInterface().loadStaff(conference.getId()),
                ApplicationController.getNetworkInterface().loadBuildingPlans(conference.getId()),
                ApplicationController.getNetworkInterface().loadVideo(conference.getId()),
                ApplicationController.getNetworkInterface().loadArchives(conference.getId()),

                (TrackResponse tracks, EventsResponse events, CategoriesResponse categories,
                 PlacesResponse places, SpeakersResponse speakers, StaffResponse staffs,
                 BuildingPlanResponse buildingPlanResponse, VideoResponse videoResponse, ArchivesResponse archivesResponse) -> {

                    ApplicationController.getDatabaseHelper().beginTransaction();
                    if (conference.getStatus() != Conference.CONFERENCE_DRAFT) {
                        DatabaseUtils.updateTracks(tracks, conference);
                        DatabaseUtils.updateCategories(categories, conference);
                        DatabaseUtils.updatePlaces(places, conference);
                        DatabaseUtils.updateSpeakers(speakers, conference);
                        DatabaseUtils.updateStaff(staffs, conference);
                        DatabaseUtils.updateBuildingPlan(buildingPlanResponse, conference);
                        DatabaseUtils.updateVideo(videoResponse, conference);
                        DatabaseUtils.updateArchives(archivesResponse, conference);
                        DatabaseUtils.updateEvents(events, conference);
                    }

                    ApplicationController.getDatabaseHelper().endTransaction();
                    return conference;
                }).doOnError(throwable -> {
            throwable.printStackTrace();
            stopForeground(true);
            showNoConnectionNotification();
            ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.SYNC_ERROR));
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
        compositeSubscription.unsubscribe();
        ApplicationController.getBus().unregister(this);
    }

    private void showError(Throwable e) {
        Timber.e(e);
        compositeSubscription.unsubscribe();
        ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.SYNC_ERROR));
    }

    public Observable<Integer> inflateArchiveObservable() {
        return Observable.zip(
                Observables.getInstance().getEventObservables().refreshEventsObservable().toList(),
                Observables.getInstance().getConferenceObservables().refreshArchiveConferences().toList(),
                Observables.getInstance().refreshArchiveAgendaObservable().toList(),
                (conferences, events, listItem) -> 1)
                .flatMap(integer -> {
                    Utils.setIsAllArchiveDataDownloaded(true);
                    ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.SYNC_ARCHIVE_COMPLETE));
                    return Observable.just(1);
                });
    }

    @Subscribe
    public void onSyncEvent(SyncDataEvent event) {
        switch (event.getSyncState()) {
            case SyncDataEvent.DOWNLOAD_ACTIVE_CONFERENCE_CONTENT:
            case SyncDataEvent.IS_ACTIVE_CONFERENCE:
            case SyncDataEvent.IS_LANDING_PAGE:
            case SyncDataEvent.START_SYNC_ACTIVE_CONFERENCE: {
                lastSyncEvent = event.getSyncState();
            }
            break;
            case SyncDataEvent.SYNC_ERROR: {
                Utils.setUpdateInProgress(false);
                stopSelf();
            }
            break;
            case SyncDataEvent.SYNC_ARCHIVE_COMPLETE: {
                Intent updateIntent = new Intent(BaseNavigationDrawerActivity.ACTION_UPDATE);
                sendBroadcast(updateIntent);
            }
            break;
        }
    }

    private void checkAndRemoveFavorites(Conference conference) {
        DeleteBuilder<Favorite, Integer> deleteBuilder = ApplicationController.getDatabaseHelper().getFavoriteDao().deleteBuilder();
        try {
            deleteBuilder.where().ne(Favorite.CONFERENCE_ID_COLUMN, conference.getId());
            deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Observable<String> getGCMRegisterObservable() {
        return Observable.defer(() -> Observable.just(Utils.getGCMToken()))
                .flatMap(s -> {
                    if (s != null) {
                        return Observable.just(s);
                    } else {
                        return Observable.just(registerFoGcmToken())
                                .doOnError(throwable -> {
                                    throwable.printStackTrace();
                                    stopForeground(true);
                                    showNoConnectionNotification();
                                    ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.SYNC_ERROR));
                                });
                    }
                });
    }

    public void addConferenceToRegister(Conference conference) {
        if (!Utils.isGCMTokenPerConference(conference.getId())) {
            conferenceIdsToRegister.add(conference.getId());
        }
    }

    @Nullable
    private String registerFoGcmToken() {
        InstanceID instanceID = InstanceID.getInstance(this.getBaseContext());
        String token = null;
        try {
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Utils.saveGCMToken(token);
        } catch (IOException e) {
            e.printStackTrace();
            stopForeground(true);
            showNoConnectionNotification();
            ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.SYNC_ERROR));
        }
        return token;
    }

    public Observable<Integer> getConferenceObservable() {
        return conferencesObservable;
    }
}
