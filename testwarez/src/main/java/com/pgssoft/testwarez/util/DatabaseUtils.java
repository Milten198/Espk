package com.pgssoft.testwarez.util;

import android.os.Environment;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.event.FavoriteRefreshEvent;
import com.pgssoft.testwarez.database.model.Archive;
import com.pgssoft.testwarez.database.model.BuildingDescription;
import com.pgssoft.testwarez.database.model.BuildingPlan;
import com.pgssoft.testwarez.database.model.Category;
import com.pgssoft.testwarez.database.model.CategoryDescription;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.database.model.ConferenceDescription;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.EventDescription;
import com.pgssoft.testwarez.database.model.EventToArchives;
import com.pgssoft.testwarez.database.model.EventToSpeaker;
import com.pgssoft.testwarez.database.model.EventToStaff;
import com.pgssoft.testwarez.database.model.EventToTrack;
import com.pgssoft.testwarez.database.model.Favorite;
import com.pgssoft.testwarez.database.model.Gallery;
import com.pgssoft.testwarez.database.model.GalleryBEFile;
import com.pgssoft.testwarez.database.model.Language;
import com.pgssoft.testwarez.database.model.Place;
import com.pgssoft.testwarez.database.model.PlaceDescription;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.database.model.SpeakerDescription;
import com.pgssoft.testwarez.database.model.Staff;
import com.pgssoft.testwarez.database.model.StaffDescription;
import com.pgssoft.testwarez.database.model.Track;
import com.pgssoft.testwarez.database.model.TrackDescription;
import com.pgssoft.testwarez.database.model.Video;
import com.pgssoft.testwarez.database.model.companies.CompanyEntity;
import com.pgssoft.testwarez.database.model.companies.CompanyTypeDescriptions;
import com.pgssoft.testwarez.database.model.companies.Organizers;
import com.pgssoft.testwarez.database.model.companies.Partners;
import com.pgssoft.testwarez.database.model.companies.Sponsors;
import com.pgssoft.testwarez.networking.response.BuildingPlanResponse;
import com.pgssoft.testwarez.networking.response.TrackResponse;
import com.pgssoft.testwarez.networking.response.VideoResponse;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import timber.log.Timber;

/**
 * Created by rtulaza on 2015-08-10.
 */
public class DatabaseUtils {
    public static final String TEST_WAREZ_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/TestWarez/";

    public static void updateEvents(List<Event> ev, Conference conference) {

        try {
            List<Event> localEvents = ApplicationController.getDatabaseHelper().getEventDao().queryForEq(Event.CONFERENCE_ID, conference.getId());

            for (Event e : ev) {
                Event oldEvent = ApplicationController.getDatabaseHelper().getEventDao().queryForId(e.getId());

                List<EventDescription> oldDescriptions = oldEvent != null ? oldEvent.getDescriptions() : new ArrayList<>();
                List<EventDescription> newDescriptions = e.getDescriptions();

                for (EventDescription ed : newDescriptions) {
                    ed.setEvent(e);

                    if (oldDescriptions.contains(ed)) {
                        //Update descriptions
                        for (EventDescription oldDescription : oldDescriptions) {
                            if (ed.getId() == oldDescription.getId() && !ed.getUpdatedAt().equals(oldDescription.getUpdatedAt())) {
                                ApplicationController.getDatabaseHelper().getEventDescriptionsDao().createOrUpdate(ed);
                            }
                        }
                    } else {
                        //Add descriptions
                        ApplicationController.getDatabaseHelper().getEventDescriptionsDao().createOrUpdate(ed);
                    }
                }

                removeUselessElements(ApplicationController.getDatabaseHelper().getEventDescriptionsDao(), newDescriptions, oldDescriptions, EventDescription.class);

                clearEventDependencies(ApplicationController.getDatabaseHelper().getEventToTrackDao(), EventToTrack.EVENT_COLUMN_NAME, e.getId(), EventToTrack.class);

                //save tracks
                for (Track track : e.getJsonTracks()) {
                    EventToTrack ett = new EventToTrack(e, track);
                    ApplicationController.getDatabaseHelper().getEventToTrackDao().create(ett);
                }

                clearEventDependencies(ApplicationController.getDatabaseHelper().getEventToSpeakersDao(), EventToSpeaker.EVENT_COLUMN_NAME, e.getId(), EventToSpeaker.class);
                clearEventDependencies(ApplicationController.getDatabaseHelper().getEventToStaffDao(), EventToStaff.EVENT_COLUMN_NAME, e.getId(), EventToStaff.class);

                //save speakers or staff
                if (!e.isTechnical()) {
                    for (Speaker sp : e.getJsonSpeakers()) {
                        EventToSpeaker ets = new EventToSpeaker(e, sp);
                        ApplicationController.getDatabaseHelper().getEventToSpeakersDao().create(ets);
                    }
                } else {
                    for (Staff st : e.getJsonStaff()) {
                        EventToStaff ets = new EventToStaff(e, st);
                        ApplicationController.getDatabaseHelper().getEventToStaffDao().create(ets);
                    }
                }

                clearEventDependencies(ApplicationController.getDatabaseHelper().getEventToArchivesDao(), EventToArchives.EVENT_COLUMN_NAME, e.getId(), EventToArchives.class);

                //save archives
                for (Archive ar : e.getJsonArchives()) {
                    EventToArchives eta = new EventToArchives(e, ar);
                    ApplicationController.getDatabaseHelper().getEventToArchivesDao().create(eta);
                }


                if (oldEvent == null || e.getUpdatedAt().isAfter(oldEvent.getUpdatedAt())) {
                    e.setConference(conference);
                    e.setActive(conference.isActive());
                    e.setRated(oldEvent != null && oldEvent.isRated());
                    ApplicationController.getDatabaseHelper().getEventDao().createOrUpdate(e);
                }
            }

            List<Event> uselessEvents = removeUselessElements(ApplicationController.getDatabaseHelper().getEventDao(), ev, localEvents, Event.class);
            removeUselessEventDependencies(uselessEvents);
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    private static void removeUselessEventDependencies(List<Event> uselessEvents) throws SQLException {
        if (!uselessEvents.isEmpty()) {
            List<EventToSpeaker> eventToSpeakerList = new ArrayList<>();
            List<EventToStaff> eventToStaffList = new ArrayList<>();
            List<EventToTrack> eventToTrackList = new ArrayList<>();
            List<EventToArchives> eventToArchivesList = new ArrayList<>();
            List<EventDescription> eventDescriptionList = new ArrayList<>();

            for (Event event : uselessEvents) {
                eventToSpeakerList.addAll(event.getEventToSpeakers());
                eventToStaffList.addAll(event.getEventToStaff());
                eventToTrackList.addAll(event.getEventToTrack());
                eventToArchivesList.addAll(event.getEventToArchives());
                eventDescriptionList.addAll(event.getDescriptions());
            }

            int speakersDeleted = ApplicationController.getDatabaseHelper().getEventToSpeakersDao().delete(eventToSpeakerList);
            int staffDeleted = ApplicationController.getDatabaseHelper().getEventToStaffDao().delete(eventToStaffList);
            int tracksDeleted = ApplicationController.getDatabaseHelper().getEventToTrackDao().delete(eventToTrackList);
            int archivesDeleted = ApplicationController.getDatabaseHelper().getEventToArchivesDao().delete(eventToArchivesList);
            int descriptionsDeleted = ApplicationController.getDatabaseHelper().getEventDescriptionsDao().delete(eventDescriptionList);

            Timber.i("EventUpdate\n speakers deleted count: %d\nstaff deleted count: %d\n" +
                            "tracks deleted count: %d\narchives deleted count: %d\ndescriptions deleted count: %d",
                    speakersDeleted, staffDeleted, tracksDeleted, archivesDeleted, descriptionsDeleted);
        }
    }

    public static void updateCategories(List<Category> categories, Conference conference) {
        if (!conference.isActive()) {
            return;
        }

        try {
            List<Category> oldCategoryList = ApplicationController.getDatabaseHelper().getCategoryDao().queryForEq(Category.CONFERENCE_ID, conference.getId());

            for (Category c : categories) {
                for (CategoryDescription cd : c.getDescriptions()) {
                    cd.setCategory(c);
                    ApplicationController.getDatabaseHelper().getCategoryDescriptionsDao().createOrUpdate(cd);
                }

                Category old = ApplicationController.getDatabaseHelper().getCategoryDao().queryForId(c.getId());
                if (old != null) {
                    removeUselessElements(ApplicationController.getDatabaseHelper().getCategoryDescriptionsDao(), c.getDescriptions(), old.getDescriptions(), CategoryDescription.class);
                }

                if (old == null || c.getUpdatedAt().isAfter(old.getUpdatedAt())) {
                    ApplicationController.getDatabaseHelper().getCategoryDao().createOrUpdate(c);
                }
            }

            removeUselessElements(ApplicationController.getDatabaseHelper().getCategoryDao(), categories, oldCategoryList, Category.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void updateStaff(List<Staff> st, Conference conference) {
        try {
            List<Staff> oldStaffList = ApplicationController
                    .getDatabaseHelper().getStaffDao().queryForEq(Staff.CONFERENCE, conference.getId());

            for (Staff s : st) {
                for (StaffDescription sd : s.getDescriptions()) {
                    sd.setStaff(s);
                    ApplicationController.getDatabaseHelper().getStaffDescriptionDao().createOrUpdate(sd);
                }

                Staff old = ApplicationController.getDatabaseHelper().getStaffDao().queryForId(s.getId());
                if (old != null) {
                    removeUselessElements(ApplicationController.getDatabaseHelper().getStaffDescriptionDao(), s.getDescriptions(), old.getDescriptions(), StaffDescription.class);
                }

                if (old == null || s.getUpdatedAt().isAfter(old.getUpdatedAt())) {
                    s.setConferenceId(conference.getId());
                    if (s.getPhoto() != null) {
                        s.getPhoto().setConferenceId(conference.getId());
                        s.getPhoto().setStaff(s);
                    }
                    ApplicationController.getDatabaseHelper().getFilesDao().createOrUpdate(s.getPhoto());
                    ApplicationController.getDatabaseHelper().getStaffDao().createOrUpdate(s);
                }
            }

            removeUselessElements(ApplicationController.getDatabaseHelper().getStaffDao(), st, oldStaffList, Staff.class);
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    public static void updateSpeakers(List<Speaker> sp, Conference conference) {
        try {
            List<Speaker> oldSpeakers = ApplicationController
                    .getDatabaseHelper().getSpeakerDao().queryForEq(Speaker.CONFERENCE, conference);

            for (Speaker s : sp) {
                for (SpeakerDescription sd : s.getDescriptions()) {
                    sd.setSpeaker(s);
                    ApplicationController.getDatabaseHelper().getSpeakerDescriptionsDao().createOrUpdate(sd);
                }

                Speaker old = ApplicationController.getDatabaseHelper().getSpeakerDao().queryForId(s.getId());
                if (old != null) {
                    removeUselessElements(ApplicationController.getDatabaseHelper().getSpeakerDescriptionsDao(), s.getDescriptions(), old.getDescriptions(), SpeakerDescription.class);
                }

                if (old == null || s.getUpdatedAt().isAfter(old.getUpdatedAt())) {
                    s.setConference(conference);

                    if (s.getPhoto() != null) {
                        s.getPhoto().setSpeaker(s);
                        s.getPhoto().setConferenceId(conference.getId());
                        ApplicationController.getDatabaseHelper().getFilesDao().createOrUpdate(s.getPhoto());
                    } else if (old != null && old.getPhoto() != null) {
                        ApplicationController.getDatabaseHelper().getFilesDao().delete(old.getPhoto());
                    }

                    ApplicationController.getDatabaseHelper().getSpeakerDao().createOrUpdate(s);
                }
            }

            removeUselessElements(ApplicationController.getDatabaseHelper().getSpeakerDao(), sp, oldSpeakers, Speaker.class);
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    public static Conference updateConference(Conference cf) throws SQLException {

        Conference conferenceFromDB = ApplicationController.getDatabaseHelper().getConferenceDao().queryForId(cf.getId());

        if (conferenceFromDB == null) {
            //call when conference doesn't exist in db
            saveConferenceComponents(cf, null);
            return cf;
        } else if (cf.getStatus() == Conference.CONFERENCE_DRAFT) {
            //call when existed conference change status to draft
            removeConference(conferenceFromDB);
            return cf;
        } else if (conferenceFromDB.getUpdatedAt().getMillis() != cf.getUpdatedAt().getMillis()) {
            //call when conference from db is updated
            cf.setSync(false);
            saveConferenceComponents(cf, conferenceFromDB);
            return cf;
        } else if (!conferenceFromDB.isSync()) {
            //call when conference has sync flag set on false
            saveConferenceComponents(cf, conferenceFromDB);
        }

        return conferenceFromDB;
    }

    private static void removeConference(Conference cf) throws SQLException {
        ApplicationController.getDatabaseHelper().getConferenceDao().delete(cf);
    }

    private static void saveConferenceComponents(Conference cf, Conference oldConference) throws SQLException {

        if (cf.getStatus() == Conference.CONFERENCE_DRAFT) {
            return;
        }

        //This data is needed only in case of active conference
        if (cf.isActive()) {
            //save description to db
            for (ConferenceDescription cd : cf.getDescriptions()) {
                cd.setConference(cf);
                cd.getBanner().setConferenceId(cf.getId());
                ApplicationController.getDatabaseHelper().getFilesDao().createOrUpdate(cd.getBanner());
                ApplicationController.getDatabaseHelper().getConferenceDescriptionsDao().createOrUpdate(cd);
            }

            if (oldConference != null) {
                removeUselessElements(ApplicationController.getDatabaseHelper().getConferenceDescriptionsDao(),
                        new ArrayList<>(cf.getDescriptions()), new ArrayList<>(oldConference.getDescriptions()), ConferenceDescription.class);
                removeUselessElements(ApplicationController.getDatabaseHelper().getPartnersDao(),
                        new ArrayList<>(cf.getPartners()), new ArrayList<>(oldConference.getPartners()), Partners.class);
                removeUselessElements(ApplicationController.getDatabaseHelper().getOrganizersDao(),
                        new ArrayList<>(cf.getOrganizers()), new ArrayList<>(oldConference.getOrganizers()), Organizers.class);
                removeUselessElements(ApplicationController.getDatabaseHelper().getSponsorsDao(),
                        new ArrayList<>(cf.getSponsors()), new ArrayList<>(oldConference.getSponsors()), Sponsors.class);
            }

            //save organizers
            for (Organizers com : cf.getOrganizers()) {
                com.setConference(cf);
                com.getCompany().getLogo().setConferenceId(cf.getId());
                com.getCompany().getLogo().setCompany(com.getCompany());
                ApplicationController.getDatabaseHelper().getFilesDao().createOrUpdate(com.getCompany().getLogo());
                ApplicationController.getDatabaseHelper().getOrganizersDao().createOrUpdate(com);
            }

            //save partners
            for (Partners com : cf.getPartners()) {
                com.setConference(cf);

                if (com.getCompany().getLogo() != null) {
                    com.getCompany().getLogo().setConferenceId(cf.getId());
                    com.getCompany().getLogo().setCompany(com.getCompany());
                }

                ApplicationController.getDatabaseHelper().getFilesDao().createOrUpdate(com.getCompany().getLogo());
                ApplicationController.getDatabaseHelper().getPartnersDao().createOrUpdate(com);
            }

            //save sponsors
            for (Sponsors com : cf.getSponsors()) {
                com.setConference(cf);
                com.getCompany().getLogo().setConferenceId(cf.getId());
                com.getCompany().getLogo().setCompany(com.getCompany());
                ApplicationController.getDatabaseHelper().getFilesDao().createOrUpdate(com.getCompany().getLogo());
                ApplicationController.getDatabaseHelper().getSponsorsDao().createOrUpdate(com);
            }

            updateCompanies(new ArrayList<>(cf.getOrganizers()));
            updateCompanies(new ArrayList<>(cf.getSponsors()));
            updateCompanies(new ArrayList<>(cf.getPartners()));
        }

        if (oldConference != null) {
            removeUselessElements(ApplicationController.getDatabaseHelper().getLanguageDao(),
                    new ArrayList<>(cf.getLanguages()), new ArrayList<>(oldConference.getLanguages()), Language.class);
        }

        for (Language lang : cf.getLanguages()) {
            lang.setConference(cf);
            ApplicationController.getDatabaseHelper().getLanguageDao().createOrUpdate(lang);
        }

        ApplicationController.getDatabaseHelper().getConferenceDao().createOrUpdate(cf);
    }

    public static void updateCompanies(List<CompanyEntity> companies) {

        try {
            for (CompanyEntity company : companies) {
                ApplicationController.getDatabaseHelper().getCompanyDao().createOrUpdate(company.getCompany());
                ApplicationController.getDatabaseHelper().getCompanyTypesDao().createOrUpdate(company.getCompanyType());

                if (company.getCompanyType() != null) {
                    for (CompanyTypeDescriptions ctd : company.getCompanyType().getDescriptions()) {
                        ctd.setCompanyType(company.getCompanyType());
                        ApplicationController.getDatabaseHelper().getCompanyTypeDescriptionsDao().createOrUpdate(ctd);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    public static void updatePlaces(List<Place> pr, Conference conference) {
        if (!conference.isActive()) {
            return;
        }

        try {
            List<Place> oldPlaceList = ApplicationController.getDatabaseHelper().getPlacesDao().queryForAll();

            for (Place p : pr) {
                Place oldPlace = ApplicationController.getDatabaseHelper().getPlacesDao().queryForId(p.getId());
                if (oldPlace == null || p.getUpdatedAt().isAfter(oldPlace.getUpdatedAt())) {
                    // download places
                    if (p.getPlace() != null) {
                        p.getPlace().setPlace(p);
                        p.getPlace().setConferenceId(conference.getId());
                    }
                    ApplicationController.getDatabaseHelper().getFilesDao().createOrUpdate(p.getPlace());
                    ApplicationController.getDatabaseHelper().getPlacesDao().createOrUpdate(p);
                }

                List<PlaceDescription> oldPlaceDescriptionList = ApplicationController.getDatabaseHelper().getPlaceDescriptionsDao().queryBuilder().where().eq(PlaceDescription.PLACE_ID, p.getId()).query();

                for (PlaceDescription pd : p.getDescriptions()) {
                    PlaceDescription oldPlaceDescription = ApplicationController.getDatabaseHelper().getPlaceDescriptionsDao().queryForId(pd.getId());

                    if (oldPlaceDescription == null || (pd.getUpdatedAt().isAfter(oldPlaceDescription.getUpdatedAt()))) {
                        pd.setPlace(p);
                        ApplicationController.getDatabaseHelper().getPlaceDescriptionsDao().createOrUpdate(pd);
                    }
                }

                removeUselessElements(ApplicationController.getDatabaseHelper().getPlaceDescriptionsDao(), p.getDescriptions(), oldPlaceDescriptionList, PlaceDescription.class);
            }

            removeUselessElements(ApplicationController.getDatabaseHelper().getPlacesDao(), pr, oldPlaceList, Place.class);
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    public static void updateArchives(List<Archive> archives, Conference conference) {

        try {
            List<Archive> oldArchiveList = ApplicationController.getDatabaseHelper().getArchivesDao().queryBuilder().where().eq(Archive.CONFERENCE_ID, conference.getId()).query();

            for (Archive archive : archives) {
                Archive old = ApplicationController.getDatabaseHelper().getArchivesDao().queryForId(archive.getId());
                if (old == null || archive.getUpdatedAt().isAfter(old.getUpdatedAt())) {
                    archive.setConference(conference);
                    if (archive.getFile() != null) {
                        archive.getFile().setArchive(archive);
                        archive.getFile().setConferenceId(conference.getId());
                    }
                    ApplicationController.getDatabaseHelper().getFilesDao().createOrUpdate(archive.getFile());
                    ApplicationController.getDatabaseHelper().getArchivesDao().createOrUpdate(archive);
                }
            }

            removeUselessElements(ApplicationController.getDatabaseHelper().getArchivesDao(), archives, oldArchiveList, Archive.class);
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    public static void updateGalleries(List<Gallery> galleries, Conference conference) {
        try {
            List<Gallery> oldGalleries = ApplicationController.getDatabaseHelper().getGalleriesDao().queryForEq(Gallery.CONFERENCE_ID, conference.getId());
            removeUselessElements(ApplicationController.getDatabaseHelper().getGalleriesDao(), galleries, oldGalleries, Gallery.class);

            for (Gallery gallery : galleries) {
                Gallery old = ApplicationController.getDatabaseHelper().getGalleriesDao().queryForId(gallery.getId());
                updateImages(gallery, old);
            }
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    private static void updateImages(Gallery gallery, Gallery old) throws SQLException {
        if (old == null || gallery.getUpdatedAt().isAfter(old.getUpdatedAt())) {
            if (old != null) {
                removeUselessElements(ApplicationController.getDatabaseHelper().getGalleryFilesDao(),
                        new ArrayList<>(gallery.getImages()), new ArrayList<>(old.getImages()), GalleryBEFile.class);
            }

            Collection<GalleryBEFile> images = gallery.getImages();
            for (GalleryBEFile image : images) {
                image.getImage().setGalleryBEFile(image);
                image.getImage().setGallery(gallery);
                image.getImage().setConferenceId(gallery.getConferenceId());
                image.setConferenceId(gallery.getConferenceId());
                image.setGallery(gallery);
                ApplicationController.getDatabaseHelper().getFilesDao().createOrUpdate(image.getImage());
                ApplicationController.getDatabaseHelper().getGalleryFilesDao().createOrUpdate(image);
            }

            ApplicationController.getDatabaseHelper().getGalleriesDao().createOrUpdate(gallery);
        }
    }

    public static void updateTracks(TrackResponse trackResponse, Conference conference) {
        if (trackResponse == null || trackResponse.isEmpty() || !conference.isActive()) {
            return;
        }

        try {
            List<Track> localTracks = ApplicationController.getDatabaseHelper().getTrackDao().queryForEq(Track.CONFERENCE_ID, conference.getId());

            for (Track track : trackResponse) {
                for (TrackDescription td : track.getDescriptions()) {
                    ApplicationController.getDatabaseHelper().getTrackDescriptionDao().createOrUpdate(td);
                }

                Track old = ApplicationController.getDatabaseHelper().getTrackDao().queryForId(track.getId());

                if (old != null) {
                    removeUselessElements(ApplicationController.getDatabaseHelper().getTrackDescriptionDao(), track.getDescriptions(), old.getDescriptions(), TrackDescription.class);
                }

                if (old == null || track.getUpdatedAt().isAfter(old.getUpdatedAt())) {
                    track.setConferenceId(conference.getId());
                    ApplicationController.getDatabaseHelper().getTrackDao().createOrUpdate(track);
                }
            }

            removeUselessElements(ApplicationController.getDatabaseHelper().getTrackDao(), trackResponse, localTracks, Track.class);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public static void updateVideo(VideoResponse videoResponse, Conference conference) {
        if (videoResponse == null) {
            return;
        }

        try {
            List<Video> oldVideoList = ApplicationController.getDatabaseHelper().getVideoDao().queryForEq(Video.CONFERENCE, conference.getId());

            for (Video video : videoResponse) {
                Video old = ApplicationController.getDatabaseHelper().getVideoDao().queryForId(video.getId());
                if (old == null || video.getUpdatedAt().isAfter(old.getUpdatedAt())) {
                    video.setConference(conference);
                    ApplicationController.getDatabaseHelper().getVideoDao().createOrUpdate(video);
                }
            }

            removeUselessElements(ApplicationController.getDatabaseHelper().getVideoDao(), videoResponse, oldVideoList, Video.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateBuildingPlan(BuildingPlanResponse buildingPlanResponse, Conference conference) {
        if (buildingPlanResponse == null || !conference.isActive()) {
            return;
        }

        try {
            List<BuildingPlan> oldBuildingPlanList = ApplicationController.getDatabaseHelper().getBuildingPlanDao().queryForAll();

            //save builidingplan
            for (BuildingPlan buildingPlan : buildingPlanResponse) {
                for (BuildingDescription bd : buildingPlan.getDescriptions()) {
                    bd.setBuildingPlan(buildingPlan);
                    ApplicationController.getDatabaseHelper().getBuildingDescriptionDao().createOrUpdate(bd);
                }

                BuildingPlan old = ApplicationController.getDatabaseHelper().getBuildingPlanDao().queryForId(buildingPlan.getId());
                if (old != null) {
                    removeUselessElements(ApplicationController.getDatabaseHelper().getBuildingDescriptionDao(),
                            new ArrayList<>(buildingPlan.getDescriptions()), new ArrayList<>(old.getDescriptions()), BuildingDescription.class);
                }

                if (old == null || buildingPlan.getUpdatedAt().isAfter(old.getUpdatedAt())) {
                    buildingPlan.setConference(conference);
                    ApplicationController.getDatabaseHelper().getBuildingPlanDao().createOrUpdate(buildingPlan);
                }
            }

            removeUselessElements(ApplicationController.getDatabaseHelper().getBuildingPlanDao(), buildingPlanResponse, oldBuildingPlanList, BuildingPlan.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFavorite(Event event) {
        Favorite fav = getFavorite(event);

        return fav != null;
    }

    public static Favorite getFavorite(Event event) {
        try {
            QueryBuilder<Favorite, Integer> qb = ApplicationController.getDatabaseHelper().getFavoriteDao().queryBuilder();
            Where<Favorite, Integer> where = qb.where();
            where.eq(Favorite.EVENT_COLUMN_NAME, event.getId());
            PreparedQuery<Favorite> preparedQuery = where.prepare();

            List<Favorite> fav = ApplicationController.getDatabaseHelper().getFavoriteDao().query(preparedQuery);

            return fav.size() > 0 ? fav.get(0) : null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Event addFavorite(Event event, boolean override) {
        try {

            List<Favorite> allFavorites = ApplicationController.getDatabaseHelper().getFavoriteDao().queryForAll();
            for (Favorite f : allFavorites) {
                if (f.getEvent().getId() == event.getId()) {
                    return event;
                } else if (Utils.isOverlaping(f.getEvent().getStartAt(), f.getEvent().getEndAt(), event.getStartAt(), event.getEndAt())) {
                    if (!override) {
                        return f.getEvent();
                    } else {
                        removeFavorite(f.getEvent());
                    }
                }
            }

            Favorite favorite = new Favorite(event);
            ApplicationController.getDatabaseHelper().getFavoriteDao().create(favorite);

            if (Utils.areRemindersEnabled()) {
                Utils.setEventAlarm(ApplicationController.getInstance(), favorite);
            } else if (Utils.isCalendarSync()) {
                Utils.setCalendarEvent(ApplicationController.getInstance(), favorite);
            }

            return event;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void removeFavorite(Event event) {
        try {
            Favorite fav = getFavorite(event);
            if (fav == null) {
                return;
            }


            if (Utils.areRemindersEnabled()) {
                Utils.cancelEventAlarm(ApplicationController.getInstance(), event);
            } else if (Utils.isCalendarSync()) {
                Utils.cancelCalendarEvent(ApplicationController.getInstance(), event);
            }

            ApplicationController.getDatabaseHelper().getFavoriteDao().delete(fav);
            ApplicationController.getBus().post(new FavoriteRefreshEvent(event));
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void removeAlarmsFromAllEvents() {
        try {
            List<Favorite> allFavorites = ApplicationController.getDatabaseHelper().getFavoriteDao().queryForAll();

            for (Favorite fav : allFavorites) {
                Utils.cancelEventAlarm(ApplicationController.getInstance(), fav.getEvent());
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void removeAlarmsFromCalendar() {
        try {
            List<Favorite> allFavorites = ApplicationController.getDatabaseHelper().getFavoriteDao().queryForAll();

            for (Favorite fav : allFavorites) {
                Utils.cancelCalendarEvent(ApplicationController.getInstance(), fav.getEvent());
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void addAlarmsToAllFavoriteEvents() {
        try {
            List<Favorite> allFavorites = ApplicationController.getDatabaseHelper().getFavoriteDao().queryForAll();

            if (Utils.areRemindersEnabled()) {
                for (Favorite fav : allFavorites) {
                    Utils.setEventAlarm(ApplicationController.getInstance(), fav);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void addAlarmsToCalendar() {
        try {
            List<Favorite> allFavorites = ApplicationController.getDatabaseHelper().getFavoriteDao().queryForAll();
            if (Utils.isCalendarSync()) {
                for (Favorite fav : allFavorites) {
                    Utils.setCalendarEvent(ApplicationController.getInstance(), fav);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static <T> int clearEventDependencies(Dao<T, Integer> dao, String columnName, int id, Class<T> c) throws SQLException {
        DeleteBuilder<T, Integer> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().eq(columnName, id);
        return deleteBuilder.delete();
    }

    private static <T> List<T> removeUselessElements(Dao<T, Integer> dao, List<T> apiList, List<T> dbList, Class<T> c) throws SQLException {
        List<T> elementsToRemove = getElementsToRemove(apiList, dbList, c);

        if (!elementsToRemove.isEmpty()) {
            int itemsDeleted = dao.delete(elementsToRemove);
            Timber.d("DatabaseUtils:removeUselessElements from %s, count: %d", c.getName(), itemsDeleted);
        }

        return elementsToRemove;
    }

    private static <T> List<T> getElementsToRemove(List<T> apiList, List<T> dbList, Class<T> c) {

        List<T> elementsToRemove = new ArrayList<>(dbList);

        for (int i = 0; i < apiList.size(); i++) {
            if (dbList.contains(apiList.get(i))) {
                elementsToRemove.remove(apiList.get(i));
            }
        }

        return elementsToRemove;
    }
}
