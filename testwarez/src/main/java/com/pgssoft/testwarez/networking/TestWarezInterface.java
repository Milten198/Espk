package com.pgssoft.testwarez.networking;

import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Gallery;
import com.pgssoft.testwarez.database.model.Rate;
import com.pgssoft.testwarez.networking.response.ArchivesResponse;
import com.pgssoft.testwarez.networking.response.BuildingPlanResponse;
import com.pgssoft.testwarez.networking.response.CategoriesResponse;
import com.pgssoft.testwarez.networking.response.ConferencesResponse;
import com.pgssoft.testwarez.networking.response.ConferencesUpdateTimeResponse;
import com.pgssoft.testwarez.networking.response.EventRateResponse;
import com.pgssoft.testwarez.networking.response.EventsResponse;
import com.pgssoft.testwarez.networking.response.GalleriesDescriptionsResponse;
import com.pgssoft.testwarez.networking.response.LandingPageResponse;
import com.pgssoft.testwarez.networking.response.MessageResponse;
import com.pgssoft.testwarez.networking.response.PlacesResponse;
import com.pgssoft.testwarez.networking.response.RegisterDeviceResponse;
import com.pgssoft.testwarez.networking.response.SocialMessageResponse;
import com.pgssoft.testwarez.networking.response.SpeakersResponse;
import com.pgssoft.testwarez.networking.response.StaffResponse;
import com.pgssoft.testwarez.networking.response.TrackResponse;
import com.pgssoft.testwarez.networking.response.VideoResponse;

import java.util.ArrayList;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by rtulaza on 2015-08-07.
 */
public interface TestWarezInterface {
    String API_VERSION = "/api/v2";
    String URL_LIMIT_PARAM = "?limit=9999";

    String URL_CONFERENCES = API_VERSION + "/conferences" ;

    String URL_CATEGORIES = API_VERSION + "/conferencecategories/{id}";

    String URL_EVENTS = URL_CONFERENCES + "/{id}/events" + URL_LIMIT_PARAM;

    String URL_EVENT = API_VERSION + "/events/{id}";

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

    String URL_FILES = URL_CONFERENCES + "/{id}/archives";

    String URL_RATE_EVENT = API_VERSION + "/eventfeedbacks/{id}";

    String URL_CONFERENCES_UPDATE_TIME = API_VERSION + "/conferences/updatetime";

    @GET(URL_CATEGORIES)
    Observable<CategoriesResponse> loadCategories(@Path("id") int conferenceId);

    @GET(URL_EVENTS)
    Observable<EventsResponse> loadEvents(@Path("id") int conferenceId);

    @GET(URL_EVENT)
    Observable<Event> loadEvent(@Path("id") int eventId);

    @GET(URL_SPEAKERS)
    Observable<SpeakersResponse> loadSpeakers(@Path("id") int conferenceId);

    @GET(URL_SOCIAL_MESSAGES)
    Observable<SocialMessageResponse> loadSocialMessages(@Path("id") int conferenceId);

    @GET(URL_MESSAGES)
    Observable<MessageResponse> loadMessages(@Path("id") int conferenceId);

    @GET(URL_CONFERENCES)
    Observable<ConferencesResponse> loadConferences();

    @GET(URL_PLACES)
    Observable<PlacesResponse> loadPlaces(@Path("id") int conferenceId);

    @GET(URL_TRACKS)
    Observable<TrackResponse> loadTracks(@Path("id") int conferenceId);

    @GET(URL_LANDING_PAGE)
    Observable<LandingPageResponse> loadLandingPage();

    @GET(URL_STAFF)
    Observable<StaffResponse> loadStaff(@Path("id") int conferenceId);

    @GET(URL_GALLERIES_DESCRIPTIONS)
    Observable<GalleriesDescriptionsResponse> loadGalleries(@Path("id") int conferenceId);

    @GET(URL_GALLERIES)
    Observable<Gallery> loadGallery(@Path("id") int galleryId);

    @GET(URL_BUILDING_PLANS)
    Observable<BuildingPlanResponse> loadBuildingPlans(@Path("id") int galleryId);

    @GET(URL_VIDEO_CONF)
    Observable<VideoResponse> loadConferenceVideos(@Path("id") int conferenceId);

    @GET(URL_ARCHIVES)
    Observable<ArchivesResponse> loadConferenceArchives(@Path("id") int conferenceId);

    @GET(URL_VIDEO)
    Observable<VideoResponse> loadVideos();

    @GET(URL_CONFERENCES_UPDATE_TIME)
    Observable<ConferencesUpdateTimeResponse> loadConferencesUpdateTime();

    @FormUrlEncoded
    @POST(URL_DEVICES)
    Observable<Response<RegisterDeviceResponse>> sendDeviceId(@Field("_format") String format, @Field("identifier") String identifier, @Field("conferences[]") ArrayList<Integer> conferenceIds);

    @Headers("Accept: application/json")
    @POST(URL_RATE_EVENT)
    Observable<EventRateResponse> rateEvent(@Path("id") int id, @Body Rate rate);
}
