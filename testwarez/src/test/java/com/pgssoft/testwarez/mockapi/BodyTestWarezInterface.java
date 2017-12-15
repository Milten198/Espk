package com.pgssoft.testwarez.mockapi;

import com.pgssoft.testwarez.networking.response.RegisterDeviceResponse;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by dpodolak on 30.06.16.
 */
public interface BodyTestWarezInterface {
    String API_VERSION = "/api/v1";
    String URL_LIMIT_PARAM = "?limit=9999";

    String URL_CONFERENCES = API_VERSION + "/conferences" ;

    String URL_CATEGORIES = API_VERSION + "/conferencecategories/{id}";

    String URL_EVENTS = URL_CONFERENCES + "/{id}/events" + URL_LIMIT_PARAM;

    String URL_SPEAKERS = URL_CONFERENCES + "/{id}/speakers" ;

    String URL_SOCIAL_MESSAGES = URL_CONFERENCES + "/{id}/socialmessages";

    String URL_MESSAGES = URL_CONFERENCES + "/{id}/messages";

    String URL_PLACES = URL_CONFERENCES + "/{id}/places";

    String URL_BUILDING_PLANS = URL_CONFERENCES + "/{id}/buildingplans";

    String URL_TRACKS = URL_CONFERENCES + "/{id}/tracks";

    String URL_DEVICES = API_VERSION + "/androiddevices";

    String URL_LANDING_PAGE = API_VERSION + "/landingpage";

    String URL_STAFF = URL_CONFERENCES + "/{id}/staff";

    String URL_GALLERIES_DESCRIPTIONS = URL_CONFERENCES + "/{id}/galleries";

    String URL_GALLERIES = API_VERSION + "/galleries/{id}";

    String URL_VIDEO = API_VERSION + "/archivevideos";

    String URL_VIDEO_CONF = URL_CONFERENCES + "/{id}/archivevideos";

    String URL_ARCHIVES = URL_CONFERENCES + "/{id}/archives";

    @GET(URL_CATEGORIES)
    Observable<Response<ResponseBody>> loadCategories(@Path("id") int conferenceId);

    @GET(URL_EVENTS)
    Observable<Response<ResponseBody>> loadEvents(@Path("id") int conferenceId);

    @GET(URL_SPEAKERS)
    Observable<Response<ResponseBody>> loadSpeakers(@Path("id") int conferenceId);

    @GET(URL_SOCIAL_MESSAGES)
    Observable<Response<ResponseBody>> loadSocialMessages(@Path("id") int conferenceId);

    @GET(URL_MESSAGES)
    Observable<Response<ResponseBody>> loadMessages(@Path("id") int conferenceId);

    @GET(URL_CONFERENCES)
    Observable<Response<ResponseBody>> loadConferences();

    @GET(URL_PLACES)
    Observable<Response<ResponseBody>> loadPlaces(@Path("id") int conferenceId);

    @GET(URL_TRACKS)
    Observable<Response<ResponseBody>> loadTracks(@Path("id") int conferenceId);

    @GET(URL_LANDING_PAGE)
    Observable<Response<ResponseBody>> loadLandingPage();

    @GET(URL_STAFF)
    Observable<Response<ResponseBody>> loadStaff(@Path("id") int conferenceId);

    @GET(URL_GALLERIES_DESCRIPTIONS)
    Observable<Response<ResponseBody>> loadGalleries(@Path("id") int conferenceId);

    @GET(URL_GALLERIES)
    Observable<Response<ResponseBody>> loadGallery(@Path("id") int galleryId);

    @GET(URL_BUILDING_PLANS)
    Observable<Response<ResponseBody>> loadBuildingPlans(@Path("id") int conferenceId);

    @GET(URL_VIDEO_CONF)
    Observable<Response<ResponseBody>> loadConferenceVideos(@Path("id") int conferenceId);

    @GET(URL_ARCHIVES)
    Observable<Response<ResponseBody>> loadConferenceArchives(@Path("id") int conferenceId);

    @GET(URL_VIDEO)
    Observable<Response<ResponseBody>> loadVideos();

    @FormUrlEncoded
    @POST(URL_DEVICES)
    Observable<Response<RegisterDeviceResponse>> sendDeviceId(@Field("_format") String format, @Field("identifier") String identifier, @Query("conferences[]") ArrayList<Integer> conferenceIds);

}
