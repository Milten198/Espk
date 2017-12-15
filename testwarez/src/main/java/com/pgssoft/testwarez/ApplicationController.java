package com.pgssoft.testwarez;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.pgssoft.testwarez.database.model.Category;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.TrackDescription;
import com.pgssoft.testwarez.networking.TestWarezImpl;
import com.pgssoft.testwarez.networking.TestWarezInterface;
import com.pgssoft.testwarez.util.DatabaseUtils;
import com.pgssoft.testwarez.util.ParentManager;
import com.pgssoft.testwarez.database.TestWarezDatebaseHelper;
import com.pgssoft.testwarez.util.Utils;
import com.squareup.otto.Bus;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import timber.log.Timber;

/**
 * Created by rtulaza on 2015-08-05.
 */
public class ApplicationController extends MultiDexApplication {

    private MainThreadBus bus;
    private static ApplicationController instance;
    private TestWarezDatebaseHelper databaseHelper;
    private static TestWarezInterface networkInterface;
    private TestWarezImpl networkImpl;
    private Retrofit retrofit;

    private static Conference activeConference;
    private static List<Conference> archiveConferences;

    public static String getLocale() {
        return "pl";
    }

    private boolean disableMainThreadDbOperationChecking = false;

    public ParentManager parentManager = new ParentManager();


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        ImageLoaderConfiguration.Builder imageLoaderBuilder = new ImageLoaderConfiguration.Builder(this)
                .imageDownloader(new BaseImageDownloader(this, 5000, 5000)) // connectTimeout (5 s), readTimeout (5 s)
                .memoryCacheExtraOptions(3000, 3000)
                .defaultDisplayImageOptions(
                        new DisplayImageOptions.Builder()
                                .imageScaleType(ImageScaleType.NONE)
                                .cacheOnDisk(true)
                                .build())
                .diskCache(new LimitedAgeDiskCache(new File(DatabaseUtils.TEST_WAREZ_DIRECTORY + "cache/"), 60 * 60 * 24 * 30)); // 30 days in cache

        ImageLoader.getInstance().init(imageLoaderBuilder.build());

        instance = this;

        JodaTimeAndroid.init(this);

        Utils.clearAgendaScrollPosition();

        registerActivityLifecycleCallbacks(parentManager);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }


    public static ParentManager getParentManager() {
        return instance.parentManager;
    }

    @Override
    public void onTerminate() {
        unregisterActivityLifecycleCallbacks(parentManager);
        super.onTerminate();
    }

    public static Bus getBus() {
        if (instance.bus == null) {
            instance.bus = new MainThreadBus();
        }

        return instance.bus;
    }

    /**
     * Get active conference from db
     *
     * @return Conference if such exists, null otherwise
     */
    public static Conference refreshActiveConference() {
        activeConference = null;
        return getActiveConference();
    }

    public static Conference getActiveConference() {

        if (activeConference == null) {
            try {
                QueryBuilder<Conference, Integer> conferenceQB = ApplicationController.getDatabaseHelper().getConferenceDao().queryBuilder();
                Where where = conferenceQB.where();
                where.eq(Conference.STATUS_COLUMN, Conference.CONFERENCE_ACTIVE);
                List<Conference> conferenceList = conferenceQB.query();
                if (!conferenceList.isEmpty()) {
                    activeConference = conferenceList.get(0);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return activeConference;
    }

    public static List<Conference> getArchiveConferences() {

        if (archiveConferences == null || archiveConferences.isEmpty()) {
            try {
                QueryBuilder<Conference, Integer> conferenceQB = ApplicationController.getDatabaseHelper().getConferenceDao().queryBuilder();
                Where where = conferenceQB.where();
                where.eq(Conference.STATUS_COLUMN, Conference.CONFERENCE_ARCHIVE);
                archiveConferences = conferenceQB.query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return archiveConferences;
    }

    public static ApplicationController getInstance() {
        return instance;
    }

    public static TestWarezDatebaseHelper getDatabaseHelper() {

        if (Thread.currentThread() == Looper.getMainLooper().getThread() && !instance.disableMainThreadDbOperationChecking) {
            throw new IllegalThreadStateException("Database operation on mainThread");
        }

        boolean dbIsNotOpen = instance.databaseHelper != null && !instance.databaseHelper.isOpen();

        if (instance.databaseHelper == null || dbIsNotOpen) {
            instance.databaseHelper = new TestWarezDatebaseHelper(instance);
        }

        return instance.databaseHelper;
    }

    public static void disableCheckingMainThreadDBOperation() {
        instance.disableMainThreadDbOperationChecking = true;
    }

    public static TestWarezDatebaseHelper getDatabaseHelper(Context context) {

        boolean dbIsNotOpen = instance.databaseHelper != null && !instance.databaseHelper.isOpen();

        if (instance.databaseHelper == null || dbIsNotOpen) {
            instance.databaseHelper = OpenHelperManager.getHelper(context, TestWarezDatebaseHelper.class);
        }

        return instance.databaseHelper;
    }


    public static TestWarezImpl getNetworkInterface() {
        if (networkInterface == null) {
            if (instance.retrofit == null) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(DateTime.class, (JsonDeserializer<DateTime>) (json, typeOfT, context) -> DateTime.parse(json.getAsString()))
                        .registerTypeAdapter(TrackDescription.class, (JsonDeserializer<TrackDescription>) (json, typeOfT, context) -> {
                            JsonObject jsonObject = new JsonObject();
                            int id = json.getAsJsonObject().get("parent").getAsInt();
                            jsonObject.addProperty("id", id);
                            json.getAsJsonObject().remove("parent");
                            json.getAsJsonObject().add("track", jsonObject);

                            return new Gson().fromJson(json, TrackDescription.class);
                        })
                        .registerTypeAdapter(Conference.class, (JsonDeserializer<Conference>) (json, typeOfT, context) -> {
                            JsonObject jsonObject = json.getAsJsonObject();

                            if (jsonObject.has("allow_tracks")) {
                                int allowTracks = jsonObject.get("allow_tracks").getAsInt();
                                boolean allowTracksBoolean = allowTracks != 0 && (allowTracks == 1);
                                jsonObject.remove("allow_tracks");
                                jsonObject.addProperty("allow_tracks", allowTracksBoolean);
                            }

                            return getDateTimeGsonTypeAdapter().fromJson(json, Conference.class);
                        })
                        .registerTypeAdapter(Category.class, (JsonDeserializer<Category>) (json, typeOfT, context) -> {
                            JsonObject jsonObject = json.getAsJsonObject();

                            if (jsonObject.has("hidden")) {
                                int hidden = jsonObject.get("hidden").getAsInt();
                                boolean hiddenBoolean = hidden != 0 && (hidden == 1);
                                jsonObject.remove("hidden");
                                jsonObject.addProperty("hidden", hiddenBoolean);
                            }

                            return getDateTimeGsonTypeAdapter().fromJson(json, Category.class);
                        })
                        .registerTypeAdapter(Event.class, (JsonDeserializer<Event>) (json, typeOfT, context) -> {
                            JsonObject jsonObject = json.getAsJsonObject();

                            if (jsonObject.has("technical")) {
                                int isTechnical = jsonObject.get("technical").getAsInt();
                                boolean isTechnicalBoolean = isTechnical != 0 && (isTechnical == 1);
                                jsonObject.remove("technical");
                                jsonObject.addProperty("technical", isTechnicalBoolean);
                            }

                            if (jsonObject.has("archival")) {
                                int isArchival = jsonObject.get("archival").getAsInt();
                                boolean isArchivalBoolean = isArchival != 0 && (isArchival == 1);
                                jsonObject.remove("archival");
                                jsonObject.addProperty("archival", isArchivalBoolean);
                            }

                            return getDateTimeGsonTypeAdapter().fromJson(json, Event.class);
                        })
                        .create();
                OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClient.addNetworkInterceptor(new StethoInterceptor());
                httpClient.addInterceptor(logging);
                instance.retrofit = new Retrofit.Builder()
                        .baseUrl(instance.getResources().getString(R.string.endpoint))
                        .client(httpClient.build())
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            }

            networkInterface = instance.retrofit.create(TestWarezInterface.class);
            instance.networkImpl = new TestWarezImpl(networkInterface);
        }

        return instance.networkImpl;
    }

    private static Gson getDateTimeGsonTypeAdapter() {
        return new GsonBuilder().registerTypeAdapter(DateTime.class,
                (JsonDeserializer<DateTime>) (json, typeOfT, context) ->
                        DateTime.parse(json.getAsString())).create();
    }

    public static TestWarezImpl getNetworkInterface(String url) {
        if (networkInterface == null) {
            if (instance.retrofit == null) {
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
                OkHttpClient httpClient = new OkHttpClient();
                instance.retrofit = new Retrofit.Builder()
                        .baseUrl(url)
                        .client(httpClient)
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            }

            networkInterface = instance.retrofit.create(TestWarezInterface.class);
            instance.networkImpl = new TestWarezImpl(networkInterface);
        }

        return instance.networkImpl;
    }

    public static void closeDatabaseHelper() {

        instance.databaseHelper.close();
        instance.databaseHelper = null;
    }

    private static class MainThreadBus extends Bus {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void post(final Object event) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
            } else {
                handler.post(() -> MainThreadBus.super.post(event));
            }
        }
    }
}
