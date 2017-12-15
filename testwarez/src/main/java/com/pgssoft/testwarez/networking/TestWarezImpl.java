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
import rx.Observable;
import timber.log.Timber;

/**
 * Created by rtulaza on 2015-09-08.
 */
public class TestWarezImpl {
    private TestWarezInterface testWarezInterface;

    public TestWarezImpl(TestWarezInterface testWarezInterface) {
        this.testWarezInterface = testWarezInterface;
    }

    public Observable<CategoriesResponse> loadCategories(int conferenceId) {
        return testWarezInterface.loadCategories(conferenceId)
                .map(categories -> {
                    if (categories == null) {
                        return new CategoriesResponse();
                    }

                    return categories;
                });
    }

    public Observable<EventsResponse> loadEvents(int conferenceId) {
        return testWarezInterface.loadEvents(conferenceId)
                .map(events -> {
                    if (events == null) {
                        return new EventsResponse();
                    }

                    return events;
                });
    }

    public Observable<Event> loadEvent(int eventId) {
        return testWarezInterface.loadEvent(eventId)
                .map(event -> {
                    if (event == null) {
                        return new Event();
                    }

                    return event;
                });
    }

    public Observable<BuildingPlanResponse> loadBuildingPlans(int conference) {
        return testWarezInterface.loadBuildingPlans(conference)
                .map(buildingPlans -> {
                    if (buildingPlans == null) {
                        return new BuildingPlanResponse();
                    }

                    return buildingPlans;
                });
    }

    public Observable<SpeakersResponse> loadSpeakers(int conferenceId) {
        return testWarezInterface.loadSpeakers(conferenceId)
                .map(speakers -> {
                    if (speakers == null) {
                        return new SpeakersResponse();
                    }

                    return speakers;
                });
    }

    public Observable<SocialMessageResponse> loadSocialMessages(int conferenceId) {
        return testWarezInterface.loadSocialMessages(conferenceId)
                .map(socialMessages -> {
                    if (socialMessages == null) {
                        return new SocialMessageResponse();
                    }

                    return socialMessages;
                });
    }

    public Observable<MessageResponse> loadMessages(int conferenceId) {
        return testWarezInterface.loadMessages(conferenceId)
                .map(messages -> {
                    if (messages == null) {
                        return new MessageResponse();
                    }

                    return messages;
                });
    }

    public Observable<ConferencesResponse> loadConferences() {
        return testWarezInterface.loadConferences()
                .map(conferences -> {
                    Timber.i("Conferences: " + conferences.size());

                    if (conferences == null) {
                        return new ConferencesResponse();
                    }

                    return conferences;
                });
    }

    public Observable<PlacesResponse> loadPlaces(int conferenceId) {
        return testWarezInterface.loadPlaces(conferenceId)
                .map(places -> {
                    if (places == null) {
                        return new PlacesResponse();
                    }

                    return places;
                });
    }

    public Observable<TrackResponse> loadTracks(int conferenceId) {
        return testWarezInterface.loadTracks(conferenceId)
                .map(tracks -> {
                    if (tracks == null) {
                        return new TrackResponse();
                    }

                    return tracks;
                });
    }

    public Observable<StaffResponse> loadStaff(int conferenceId) {
        return testWarezInterface.loadStaff(conferenceId)
                .map(staffs -> {
                    if (staffs == null) {
                        return new StaffResponse();
                    }

                    return staffs;
                });
    }

    public Observable<GalleriesDescriptionsResponse> loadGalleriesDescriptionsResponse(int conferenceId) {
        return testWarezInterface.loadGalleries(conferenceId)
                .map(galleryDescriptions -> {
                    if (galleryDescriptions == null) {
                        return new GalleriesDescriptionsResponse();
                    }

                    return galleryDescriptions;
                });
    }

    public Observable<Response<RegisterDeviceResponse>> sendDeviceId(String format, String identifier, ArrayList<Integer> conferenceIds) {
        return testWarezInterface.sendDeviceId(format, identifier, conferenceIds);
    }

    public Observable<ConferencesUpdateTimeResponse> getConferencesUpdateTimes() {
        return testWarezInterface.loadConferencesUpdateTime()
                .map(conferenceUpdateTimes -> {
                    if (conferenceUpdateTimes == null) {
                        return new ConferencesUpdateTimeResponse();
                    }

                    return conferenceUpdateTimes;
                });
    }

    public Observable<LandingPageResponse> loadLandingPage() {
        return testWarezInterface.loadLandingPage()
                .map(landingPageResponse -> {
                    if (landingPageResponse == null) {
                        return new LandingPageResponse();
                    }

                    return landingPageResponse;
                });
    }

    public Observable<Gallery> loadGalleriesResponse(int id) {
        return testWarezInterface.loadGallery(id)
                .map(gallery -> {
                    if (gallery == null) {
                        return new Gallery();
                    }

                    return gallery;
                });
    }

    public Observable<VideoResponse> loadVideo(int conferenceId) {
        return testWarezInterface.loadConferenceVideos(conferenceId)
                .map(videos -> {
                    if (videos == null) {
                        return new VideoResponse();
                    }

                    return videos;
                });
    }

    public Observable<ArchivesResponse> loadArchives(int conferenceId) {
        return testWarezInterface.loadConferenceArchives(conferenceId)
                .map(archives -> {
                    if (archives == null) {
                        return new ArchivesResponse();
                    }

                    return archives;
                });
    }

    public Observable<VideoResponse> loadVideo() {
        return testWarezInterface.loadVideos()
                .map(videos -> {
                    if (videos == null) {
                        return new VideoResponse();
                    }

                    return videos;
                });
    }

    public Observable<EventRateResponse> rateEvent(int eventId, Rate rate) {
        return testWarezInterface.rateEvent(eventId, rate)
                .map(eventRateResponse -> {
                    if (eventRateResponse == null) {
                        return new EventRateResponse();
                    }

                    Timber.i("EventRate" + eventId + ": " + eventRateResponse.getMessage());
                    return eventRateResponse;
                });
    }
}
