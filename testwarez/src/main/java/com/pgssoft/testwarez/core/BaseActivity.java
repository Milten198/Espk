package com.pgssoft.testwarez.core;

import android.Manifest;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.pwittchen.reactivenetwork.library.ConnectivityStatus;
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.CalendarRequestPermissionEvent;
import com.pgssoft.testwarez.service.FileDownloadService;
import com.pgssoft.testwarez.service.SyncService;
import com.pgssoft.testwarez.util.Observables;
import com.pgssoft.testwarez.util.Utils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpodolak on 18.04.16.
 */
public class BaseActivity extends AppCompatActivity {

    protected CompositeSubscription compositeSubscription = new CompositeSubscription();
    protected static final int REQUEST_PERMISSION_CALENDAR = 100;
    protected static final int REQUEST_PERMISSION_PHONE = 101;

    protected DownloadManager downloadManager;
    protected long downloadedFileID;
    protected Snackbar internetConnectionSnackbar;
    private static boolean isAppResumedFromBackground;

    /**
     * Secure before twice requesting for calendar permission
     */
    private boolean isCalendarRequest = false;
    public static final String LANG_PREFS = "LANG_PREFS";
    public static final String LANG_KEY = "lang";
    public boolean isDownloadingStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setLanguage(this);
        super.onCreate(savedInstanceState);

        //todo when connection with internet is weak (e.g. via edge) and not stable, not working properly
        //above issue occures because, backend isn't allowed on outside
        listenerForInternetConnection();
    }

    private void checkSyncedConferenceAfterConnection() {

        Observable<Boolean> conferenceToSync = Observables.getInstance().getConferenceObservables().getArchiveConferences()
                .filter(conference -> !conference.isSync())
                .subscribeOn(Schedulers.newThread())
                .count()
                .map(count -> count > 0);

        Observable<Boolean> tokenToSync = Observable.from(Utils.getGCMTokensFlags())
                .filter(list -> !list)
                .map(list -> !list)
                .distinct();

        compositeSubscription.add(
                Observable.zip(conferenceToSync, tokenToSync, (syncC, syncT) -> syncC || syncT)
                        .subscribe(sync -> {
                            if (sync) {
                                startService(new Intent(this, SyncService.class));
                            }
                        }, Throwable::printStackTrace));
    }

    private void listenerForInternetConnection() {
        compositeSubscription.add(new ReactiveNetwork()
                .observeConnectivity(this)
                .skip(1)
                .delay(4, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectivityStatus -> {
                    if (ConnectivityStatus.OFFLINE == connectivityStatus) {
                        showNoInternetConnectionSnackbar(findViewById(R.id.flABaseContainer), true);
                    } else {
                        if (internetConnectionSnackbar != null && internetConnectionSnackbar.isShown()) {
                            internetConnectionSnackbar.dismiss();
                        }

                        showInternetConnectionSnackbar();
                        checkSyncedConferenceAfterConnection();
                    }
                }, Throwable::printStackTrace));
    }

    public void showNoInternetConnectionSnackbar(View view, boolean withAction) {
        internetConnectionSnackbar = Snackbar.make(
                view, getString(R.string.no_network), Snackbar.LENGTH_LONG);
        if (withAction) {
            internetConnectionSnackbar.setAction(getString(R.string.user_accept), v -> {
                internetConnectionSnackbar.dismiss();
            })
                    .setActionTextColor(getResources().getColor(R.color.primary));
            internetConnectionSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        }

        internetConnectionSnackbar.show();
    }

    private void showInternetConnectionSnackbar() {
        internetConnectionSnackbar = Snackbar.make(
                findViewById(R.id.flABaseContainer),
                getString(R.string.network_connection),
                Snackbar.LENGTH_LONG);
        internetConnectionSnackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationController.getBus().register(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
                new IntentFilter("file_downloaded"));
        downloadedFileID = Utils.getDownloadedFileId();
        if (downloadedFileID != -1) {
            if (Utils.getSnackbarElapsedTime() + 1200 > System.currentTimeMillis()) {
                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = downloadManager.getUriForDownloadedFile(downloadedFileID);
                String mimeType = downloadManager.getMimeTypeForDownloadedFile(downloadedFileID);

                final Snackbar snackbar = initSnackbar(uri, mimeType);
                snackbar.show();
                new Handler().postDelayed(snackbar::dismiss, 8000);
            }
        }

        if (isAppResumedFromBackground) {
            if (!Utils.isInternetConnection(this)) {
                showNoInternetConnectionSnackbar(findViewById(R.id.flABaseContainer), false);
            }

            isAppResumedFromBackground = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationController.getBus().unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);

        if (isApplicationInBackground()) {
            isAppResumedFromBackground = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
    }

    private boolean isApplicationInBackground() {
        final ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> tasks = manager.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            final ComponentName topActivity = tasks.get(0).topActivity;
            return !topActivity.getPackageName().equals(getPackageName());
        }
        return false;
    }

    public boolean calendarPermissionGranted(boolean showExplanation) {

        if (isCalendarRequest) {
            return false;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR)) && showExplanation) {

                AlertDialog.Builder explanationPermision = new AlertDialog.Builder(this);
                explanationPermision.setMessage(R.string.calendar_permission_request);
                explanationPermision.setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    isCalendarRequest = false;
                });
                explanationPermision.setPositiveButton(R.string.submit, (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(BaseActivity.this, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, REQUEST_PERMISSION_CALENDAR);
                });
                explanationPermision.create().show();
                isCalendarRequest = true;
                return false;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, REQUEST_PERMISSION_CALENDAR);
                isCalendarRequest = true;
                return false;
            }
        }

        return true;
    }

    public boolean phonePermissionGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PERMISSION_CALENDAR);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CALENDAR: {
                isCalendarRequest = false;
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ApplicationController.getBus().post(new CalendarRequestPermissionEvent());
                }
            }
            break;
        }
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long fileDownloadedId = intent.getLongExtra("file_id", -1L);
            isDownloadingStarted = false;
            fileHasBeenDownloaded(fileDownloadedId);
        }
    };

    protected void fileHasBeenDownloaded(long id) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = downloadManager.getUriForDownloadedFile(id);
        String mimeType = downloadManager.getMimeTypeForDownloadedFile(id);

        final Snackbar snackbar = initSnackbar(uri, mimeType);
        snackbar.show();
        new Handler().postDelayed(() -> {
            snackbar.dismiss();
            Utils.clearDownloadedFileId();
        }, 8000);
    }

    private Snackbar initSnackbar(Uri uri, String mimeType) {
        Utils.setSnackbarElapsedTime(System.currentTimeMillis());

        return Snackbar.make(
                ButterKnife.findById(BaseActivity.this, R.id.drawer_layout),
                getString(R.string.download_completed),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getResources().getString(R.string.open), v -> {
                    openFile(uri, mimeType);
                })
                .setActionTextColor(getResources().getColor(R.color.primary));
    }

    protected boolean isFileDownloading() {

        if (isDownloadingStarted) {
            return true;
        }

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo info : am.getRunningServices(Integer.MAX_VALUE)) {
            if (FileDownloadService.class.getName().equals(info.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    public void openFile(Uri uri, String mimeType) {

        try {
            //We can not use queryIntentActivities from PackageManage, because on Marshmallow we get empty list
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(uri, mimeType);

            startActivity(target);
        } catch (Exception e) {
            initAlertDialog(mimeType);
        }
    }

    public void openMarket(String mimeType) {
        String fileType = Utils.convertMimeTypeToStoreExtension(mimeType);
        Uri marketUri = Uri.parse("market://search?q=" + fileType);
        Intent linkToMarket = new Intent(Intent.ACTION_VIEW, marketUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(linkToMarket);
        } catch (ActivityNotFoundException a) {
            Snackbar.make(
                    ButterKnife.findById(this, R.id.clActivityEvent),
                    getResources().getString(R.string.no_google_play),
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    public void initAlertDialog(final String mimeType) {
        new AlertDialog.Builder(BaseActivity.this)
                .setTitle(getResources().getString(R.string.no_app_title))
                .setMessage(getResources().getString(R.string.no_app_message))
                .setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
                    openMarket(mimeType);
                })
                .setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }
}
