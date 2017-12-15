package com.pgssoft.testwarez.networking;

import android.app.IntentService;
import android.content.Intent;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.event.LandingPageResponseEvent;
import com.pgssoft.testwarez.event.SyncDataEvent;
import com.pgssoft.testwarez.database.model.LandingPageDescription;

import java.sql.SQLException;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpodolak on 25.02.16.
 */
public class NetworkSevice extends IntentService {
    public static final String TAG = "NetworkSevice";

    CompositeSubscription compositeSubscription = new CompositeSubscription();

    public static final String SYNC_DATA = "sync_data";
    public static final String REQUEST_LANDING_PAGE = "request_landing_page";

    public NetworkSevice() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case REQUEST_LANDING_PAGE:
                requestLandingPage();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
    }

    private void requestLandingPage() {

        Subscription landingPageSubscription = ApplicationController.getNetworkInterface().loadLandingPage()
                .doOnError(throwable -> {
                    ApplicationController.getBus().post(new SyncDataEvent(SyncDataEvent.SYNC_ERROR));
                })
                .flatMap(landingPageResponse -> {
                            if (landingPageResponse != null) {

                                return Observable.from(landingPageResponse.getDescriptions())
                                        .doOnNext(landingPageDescription -> {
                                            try {
                                                ApplicationController.getDatabaseHelper().getLandingPageDescriptionDao().createOrUpdate((LandingPageDescription) landingPageDescription);
//                                                ImageLoader.getInstance().loadImageSync(getString(R.string.endpoint) + "/" + ((LandingPageDescription) landingPageDescription).getBanner());
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }).map(landingPageDescription -> landingPageResponse)
                                        .doOnNext(landingPageResponse1 -> ApplicationController.getBus().post(new LandingPageResponseEvent(landingPageResponse1)));
                            } else {
                                return Observable.empty();
                            }
                        })
                .subscribe(landingPageResponse -> {}, Throwable::printStackTrace);

        compositeSubscription.add(landingPageSubscription);

    }

}
