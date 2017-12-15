package com.pgssoft.testwarez;

import android.content.Intent;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.pgssoft.testwarez.event.SyncDataEvent;
import com.pgssoft.testwarez.mockapi.MockTestWarezServer;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.database.model.TrackDescription;
import com.pgssoft.testwarez.service.SyncService;
import com.pgssoft.testwarez.utils.SyncEventValidator;
import com.pgssoft.testwarez.utils.Utils;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ServiceController;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by dpodolak on 28.06.16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class ServiceTest {

    private SyncService syncService;
    private ServiceController<SyncService> serviceController;


    private List<SyncDataEvent> syncDateEventArray = new ArrayList<>();

    private MockTestWarezServer mockTestWarezServer;
    private List<Conference> conferences;
    private String conferenceJson;

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(DateTime.class, (JsonDeserializer<DateTime>) (json, typeOfT, context) -> DateTime.parse(json.getAsString()))

            .registerTypeAdapter(TrackDescription.class, (JsonDeserializer<TrackDescription>) (json, typeOfT, context) -> {

                JsonObject jsonObject = new JsonObject();
                int id = json.getAsJsonObject().get("parent").getAsInt();
                jsonObject.addProperty("id", id);
                json.getAsJsonObject().remove("parent");
                json.getAsJsonObject().add("track", jsonObject);

                return new Gson().fromJson(json, TrackDescription.class);
            }).create();


    @Before
    public void setUp() {

        ApplicationController.getBus().register(this);
        Intent intent = new Intent();
        intent.putExtra(SyncService.DISABLE_SYNC_DATA, true);
        serviceController = Robolectric.buildService(SyncService.class, intent);

        ApplicationController.disableCheckingMainThreadDBOperation();
        mockTestWarezServer = new MockTestWarezServer();
        saveDefaultStateConferences();
        ApplicationController.getNetworkInterface(mockTestWarezServer.getUrl());
        ApplicationController.getDatabaseHelper(RuntimeEnvironment.application);//.getApplicationContext();

        com.pgssoft.testwarez.util.Utils.saveGCMToken("lknsdafojh8qwer9ur0mcr98743yxn7yrc8374yr87xq23zn8r7cty44r98fmz9823ur9834futx34yr873c");
    }

    @Test
    public void testSyncActiveConferenceClearDB() {

        syncService = serviceController.attach().create().get();

        assertNotNull(syncService);
        assertNotNull(ApplicationController.getNetworkInterface());

        syncService.getConferenceObservable()
                .subscribe().unsubscribe();

        assertTrue(SyncEventValidator.isValid(syncDateEventArray, SyncEventValidator.SYNC_ALL_PATTERN));

    }

    @Test
    public void testLandingPageClearDB(){

        ApplicationController.refreshActiveConference();

        for (Conference conference: conferences){
            if (conference.isActive()){
                conference.setStatus(Conference.CONFERENCE_ARCHIVE);
            }
        }

        saveModifiedConferences();

        syncService = serviceController.attach().create().get();

        assertNotNull(syncService);
        assertNotNull(ApplicationController.getNetworkInterface());

        syncService.getConferenceObservable()
                .subscribe().unsubscribe();

        assertTrue(SyncEventValidator.isValid(syncDateEventArray, SyncEventValidator.SYNC_ALL_WITH_LANDING_PAGE_PATTERN));
    }

    @Subscribe
    public void testSync(SyncDataEvent event){
        System.out.println(event);
        syncDateEventArray.add(event);
    }


    @After
    public void tearDown() throws Exception {
        ApplicationController.getBus().unregister(this);
        mockTestWarezServer.shutdown();
        ApplicationController.closeDatabaseHelper();
        restoreDefaultStateConferences();
    }

    private void saveDefaultStateConferences() {
        conferenceJson = Utils.readJson(MockTestWarezServer.CONFERENCES_JSON);
        JsonParser jsonParser = new JsonParser();

        conferences = gson.fromJson(jsonParser.parse(conferenceJson).getAsJsonArray(), new TypeToken<List<Conference>>() {
        }.getType());

    }

    private void restoreDefaultStateConferences() {
        Utils.saveFile(conferenceJson, MockTestWarezServer.CONFERENCES_JSON);
    }


    public void saveModifiedConferences(){


        Gson converterGson = Converters.registerDateTime(new GsonBuilder()).create();
        String json = converterGson.toJson(conferences);

        Utils.saveFile(json, MockTestWarezServer.CONFERENCES_JSON);
//        assertEquals(json.hashCode(), conferenceJson.hashCode());
    }
}
