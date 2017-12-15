package com.pgssoft.testwarez.mockapi;


import android.content.UriMatcher;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.database.model.GalleryDescription;
import com.pgssoft.testwarez.database.model.TrackDescription;
import com.pgssoft.testwarez.utils.Utils;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created by dpodolak on 30.06.16.
 */
public class MockTestWarezServer {

    private static final Logger logger = Logger.getLogger(MockWebServer.class.getName());

    public static final String CONFERENCES_JSON = "Conferences.json";
    public static final String LANDING_PAGE_JSON = "LandingPage.json";
    public static final String VIDEOS_JSON = "Videos.json";
    public static final String EVENTS_JSON = "Events.json";
    public static final String SPEAKERS_JSON = "Speakers.json";
    public static final String STAFFS_JSON = "Staffs.json";
    public static final String BUILDING_PLANS_JSON = "BuildingPlans.json";
    public static final String PLACES_JSON = "Places.json";
    public static final String CATEGORIES_JSON = "Categories.json";
    public static final String TRACKS_JSON = "Tracks.json";
    public static final String CONFERENCE_ARCHIVE_JSON = "ConferenceArchive.json";
    public static final String CONFERENCE_VIDEO_JSON = "ConferenceVideo.json";
    public static final String GALLERIES_JSON = "Galleries.json";
    public static final String GALLERY_JSON = "Gallery.json";

    public static final int CONFERENCE_CODE = 1;
    public static final int EVENT_CODE = 2;
    public static final int SPEAKER_CODE = 3;
    public static final int CATEGORIES_CODE = 4;
    public static final int BUILDING_PLAN_CODE = 5;
    public static final int ARCHIVES_CODE = 6;
    public static final int GALLERIES_CODE = 7;
    public static final int PLACE_CODE = 8;
    public static final int STAFF_CODE = 9;
    public static final int TRACK_CODE = 10;
    public static final int LANDING_PAGE_CODE = 11;
    public static final int ARCHIVE_VIDEO_CODE = 12;
    public static final int GALLERY_CODE = 13;
    public static final int CONFERENCE_VIDEO_CODE = 14;
    public static final int GCM_REGISTRATION_CODE = 15;

    private MockWebServer mockWebServer;
    private HttpUrl httpUrl;
    private JsonParser jsonParser = new JsonParser();
    private Gson gson;
    private List<Integer> conferencesToRegister = new ArrayList<>();

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONFERENCES_PATTERN = "/api/v1/conferences";
    public static final String AUTHORITY = "testwarez";
    public static final String API_PATTERN = "/api/v1";


    static {
        uriMatcher.addURI(AUTHORITY, CONFERENCES_PATTERN, CONFERENCE_CODE);
        uriMatcher.addURI(AUTHORITY, CONFERENCES_PATTERN + "/#/events", EVENT_CODE);
        uriMatcher.addURI(AUTHORITY, CONFERENCES_PATTERN + "/#/speakers", SPEAKER_CODE);
        uriMatcher.addURI(AUTHORITY, API_PATTERN + "/conferencecategories/#", CATEGORIES_CODE);
        uriMatcher.addURI(AUTHORITY, CONFERENCES_PATTERN + "/#/buildingplans", BUILDING_PLAN_CODE);
        uriMatcher.addURI(AUTHORITY, CONFERENCES_PATTERN + "/#/archives", ARCHIVES_CODE);
        uriMatcher.addURI(AUTHORITY, CONFERENCES_PATTERN + "/#/galleries", GALLERIES_CODE);
        uriMatcher.addURI(AUTHORITY, CONFERENCES_PATTERN + "/#/places", PLACE_CODE);
        uriMatcher.addURI(AUTHORITY, CONFERENCES_PATTERN + "/#/archivevideos", CONFERENCE_VIDEO_CODE);

        uriMatcher.addURI(AUTHORITY, CONFERENCES_PATTERN + "/#/staff", STAFF_CODE);
        uriMatcher.addURI(AUTHORITY, CONFERENCES_PATTERN + "/#/tracks", TRACK_CODE);
        uriMatcher.addURI(AUTHORITY, API_PATTERN + "/landingpage", LANDING_PAGE_CODE);
        uriMatcher.addURI(AUTHORITY, API_PATTERN + "/archivevideos/#", ARCHIVE_VIDEO_CODE);
        uriMatcher.addURI(AUTHORITY, API_PATTERN + "/galleries/#", GALLERY_CODE);
        uriMatcher.addURI(AUTHORITY, API_PATTERN + "/androiddevices", GCM_REGISTRATION_CODE);
    }

    public MockTestWarezServer() {

        if (!checkConferencesFile()) {
            makeDumpAPI();
        }

        logger.setLevel(Level.WARNING);
        Logger.getLogger(getClass().getName()).setLevel(Level.WARNING);
        Logger.getLogger(Utils.class.getName()).setLevel(Level.WARNING);

        this.mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new TestWarezDispatcher(conferencesToRegister));
        try {
            mockWebServer.start();
            System.out.println("Init mock server on port: " + mockWebServer.getPort());
            httpUrl = mockWebServer.url("testwarez/");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void makeDumpAPI() {

        gson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, (JsonDeserializer<DateTime>) (json, typeOfT, context) -> DateTime.parse(json.getAsString()))
                .registerTypeAdapter(TrackDescription.class, (JsonDeserializer<TrackDescription>) (json, typeOfT, context) -> {

                    JsonObject jsonObject = new JsonObject();
                    int id = json.getAsJsonObject().get("parent").getAsInt();
                    jsonObject.addProperty("id", id);
                    json.getAsJsonObject().remove("parent");
                    json.getAsJsonObject().add("track", jsonObject);

                    return new Gson().fromJson(json, TrackDescription.class);
                }).create();

        OkHttpClient httpClient = new OkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.10.70.31")
                .client(httpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        BodyTestWarezInterface bodyTestWarezInterface = retrofit.create(BodyTestWarezInterface.class);

        bodyTestWarezInterface.loadLandingPage()
                .filter(responseBodyResponse -> responseBodyResponse.code() == 200)
                .map(this::mapResponseBody)
                .flatMap(s1 -> {
                    Utils.saveFile(s1, LANDING_PAGE_JSON);
                    return bodyTestWarezInterface.loadVideos();
                }).subscribe();

        bodyTestWarezInterface.loadVideos()
                .filter(responseBodyResponse -> responseBodyResponse.code() == 200)
                .map(this::mapResponseBody)
                .flatMap(s1 -> {
                    Utils.saveFile(s1, VIDEOS_JSON);
                    return bodyTestWarezInterface.loadVideos();
                }).subscribe();

        bodyTestWarezInterface.loadConferences().map(this::mapResponseBody)
                .flatMap(bodyString -> {
                    Utils.saveFile(bodyString, CONFERENCES_JSON);
                    List<Conference> conferences = gson.fromJson(jsonParser.parse(bodyString).getAsJsonArray(), new TypeToken<List<Conference>>() {
                    }.getType());
                    return Observable.from(conferences);
                })
                .flatMap(c -> Observable.zip(
                        bodyTestWarezInterface.loadEvents(c.getId()).map(rbp -> rbp.code() == 200 ? rbp : null).map(this::mapResponseBody),
                        bodyTestWarezInterface.loadSpeakers(c.getId()).map(rbp -> rbp.code() == 200 ? rbp : null).map(this::mapResponseBody),
                        bodyTestWarezInterface.loadStaff(c.getId()).map(rbp -> rbp.code() == 200 ? rbp : null).map(this::mapResponseBody),
                        bodyTestWarezInterface.loadBuildingPlans(c.getId()).map(rbp -> rbp.code() == 200 ? rbp : null).map(this::mapResponseBody),
                        bodyTestWarezInterface.loadCategories(c.getId()).map(rbp -> rbp.code() == 200 ? rbp : null).map(this::mapResponseBody),
                        bodyTestWarezInterface.loadTracks(c.getId()).map(rbp -> rbp.code() == 200 ? rbp : null).map(this::mapResponseBody),
                        bodyTestWarezInterface.loadConferenceArchives(c.getId()).map(rbp -> rbp.code() == 200 ? rbp : null).map(this::mapResponseBody),
                        bodyTestWarezInterface.loadConferenceVideos(c.getId()).map(rbp -> rbp.code() == 200 ? rbp : null).map(this::mapResponseBody),
                        bodyTestWarezInterface.loadGalleries(c.getId()).map(rbp -> rbp.code() == 200 ? rbp : null).map(this::mapResponseBody),
                        (String events, String speakers, String staffs, String buildingPlans, String categories, String tracks, String archives, String videos, String galleries) -> {

                            Utils.saveFile(events, EVENTS_JSON, c.getId());
                            Utils.saveFile(speakers, SPEAKERS_JSON, c.getId());
                            Utils.saveFile(staffs, STAFFS_JSON, c.getId());
                            Utils.saveFile(buildingPlans, BUILDING_PLANS_JSON, c.getId());
                            Utils.saveFile(categories, CATEGORIES_JSON, c.getId());
                            Utils.saveFile(tracks, TRACKS_JSON, c.getId());
                            Utils.saveFile(archives, CONFERENCE_ARCHIVE_JSON, c.getId());
                            Utils.saveFile(videos, CONFERENCE_VIDEO_JSON, c.getId());
                            Utils.saveFile(galleries, GALLERIES_JSON, c.getId());
                            return c;
                        }
                ))
                .flatMap(conference -> getGallery(conference, bodyTestWarezInterface))
                .flatMap(conference1 ->
                        bodyTestWarezInterface.loadPlaces(conference1.getId()).map(rbp -> rbp.code() == 200 ? rbp : null).map(this::mapResponseBody).map(places -> {
                            Utils.saveFile(places, PLACES_JSON, conference1.getId());
                            return conference1;
                        }))

                .subscribe(endConference -> {}, Throwable::printStackTrace);

    }

    public Observable<Conference> getGallery(Conference conference, BodyTestWarezInterface btwi) {
        return btwi.loadGalleries(conference.getId()).map(this::mapResponseBody).flatMap(galleriesJson -> {
            if (galleriesJson != null) {
                List<GalleryDescription> gd = gson.fromJson(jsonParser.parse(galleriesJson), new TypeToken<List<GalleryDescription>>() {
                }.getType());
                return Observable.from(gd);
            } else {
                return Observable.empty();
            }
        })
                .flatMap(gd -> btwi.loadGallery(gd.getId()).map(this::mapResponseBody)
                        .doOnNext(galleryJson -> Utils.saveGalleryFile(galleryJson, GALLERY_JSON, gd.getId())))
                .map(g -> conference);
    }

    private String mapResponseBody(Response<ResponseBody> responseBodyResponse) {

        if (responseBodyResponse != null && responseBodyResponse.body() != null) {
            try {
                String content = responseBodyResponse.body().string();
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }



    private boolean checkConferencesFile() {
        File conferenceJsonFile = new File(Utils.getAssetDir(), CONFERENCES_JSON);
        return conferenceJsonFile.exists();
    }

    public String getUrl() {
        return httpUrl.url().toString();
    }

    public void shutdown() {
        try {
            mockWebServer.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getConferencesToRegister() {
        return conferencesToRegister;
    }

    private static class TestWarezDispatcher extends Dispatcher {

        private List<Integer> conferencesToRegister;

        public TestWarezDispatcher(List<Integer> conferencesToRegister) {
            this.conferencesToRegister = conferencesToRegister;
        }

        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

            Uri uri = Uri.parse("//" + AUTHORITY + request.getPath());

            int conferenceId = 0;

            int code = uriMatcher.match(uri);

            if (request.getPath().contains("conferences/")) {
                List<String> pathSgments = uri.getPathSegments();
                int indexOf = pathSgments.indexOf("conferences");
                String conferenceIdS = pathSgments.get(++indexOf);
                try {
                    conferenceId = Integer.parseInt(conferenceIdS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            switch (code) {
                case CONFERENCE_CODE:
                    return getMockResponse(Utils.readJson(CONFERENCES_JSON));
                case EVENT_CODE:
                    return getMockResponse(Utils.readJson(EVENTS_JSON, conferenceId));
                case SPEAKER_CODE:
                    return getMockResponse(Utils.readJson(SPEAKERS_JSON, conferenceId));
                case STAFF_CODE:
                    return getMockResponse(Utils.readJson(STAFFS_JSON, conferenceId));
                case CATEGORIES_CODE: {
                    List<String> pathSgments = uri.getPathSegments();
                    int indexOf = pathSgments.indexOf("conferencecategories");
                    String conferenceIdS = pathSgments.get(++indexOf);
                    conferenceId = Integer.parseInt(conferenceIdS);
                    return getMockResponse(Utils.readJson(CATEGORIES_JSON, conferenceId));
                }
                case BUILDING_PLAN_CODE:
                    return getMockResponse(Utils.readJson(BUILDING_PLANS_JSON, conferenceId));
                case GALLERIES_CODE:
                    return getMockResponse(Utils.readJson(GALLERIES_JSON, conferenceId));
                case ARCHIVE_VIDEO_CODE:
                    return getMockResponse(Utils.readJson(CONFERENCE_VIDEO_JSON, conferenceId));
                case PLACE_CODE:
                    return getMockResponse(Utils.readJson(PLACES_JSON, conferenceId));
                case CONFERENCE_VIDEO_CODE:
                    return getMockResponse(Utils.readJson(CONFERENCE_VIDEO_JSON, conferenceId));
                case ARCHIVES_CODE:
                    return getMockResponse(Utils.readJson(CONFERENCE_ARCHIVE_JSON, conferenceId));
                case TRACK_CODE:
                    return getMockResponse(Utils.readJson(TRACKS_JSON, conferenceId));
                case LANDING_PAGE_CODE:
                    return getMockResponse(Utils.readJson(LANDING_PAGE_JSON));
                case GALLERY_CODE: {

                    List<String> pathSgments = uri.getPathSegments();
                    int indexOf = pathSgments.indexOf("galleries");
                    String conferenceIdS = pathSgments.get(++indexOf);
                    int galleryId = Integer.parseInt(conferenceIdS);
                    String json = Utils.readGalleryJson(GALLERY_JSON, galleryId);
                    return getMockResponse(json);
                }
                case GCM_REGISTRATION_CODE:
                    Buffer body = request.getBody();

                    String bodyString = body.readUtf8();
                    System.out.println("GCM register code body:" + bodyString);
                    System.out.println("GCM register code path:" + request.getPath());

                    String[] fields = bodyString.split("&");



                    for (String field: fields){
                        if (field.startsWith("conferences")){
                            String id = field.split("=")[1];
                            conferencesToRegister.add(Integer.parseInt(id));
                        }
                    }

                    System.out.println("ids to register:" + String.valueOf(conferencesToRegister));

                    return new MockResponse().setResponseCode(201).setBody("{\n" +
                            "  \"status\": \"success\",\n" +
                            "  \"message\": \"AndroidDevice created\"\n" +
                            "}");
            }
            return new MockResponse().setResponseCode(204);
        }

        @NonNull
        private MockResponse getMockResponse(String json) {
            MockResponse mockResponse = new MockResponse();

            if (json == null || json.isEmpty()) {
                mockResponse.setResponseCode(204);
            } else {
                mockResponse.setResponseCode(200);
                mockResponse.setBody(json);
            }
            return mockResponse;
        }


    }

}
