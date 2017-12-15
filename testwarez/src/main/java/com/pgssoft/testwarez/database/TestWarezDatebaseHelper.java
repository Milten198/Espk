package com.pgssoft.testwarez.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.pgssoft.testwarez.database.model.Archive;
import com.pgssoft.testwarez.database.model.BEFile;
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
import com.pgssoft.testwarez.database.model.Image;
import com.pgssoft.testwarez.database.model.LandingPageDescription;
import com.pgssoft.testwarez.database.model.Language;
import com.pgssoft.testwarez.database.model.Message;
import com.pgssoft.testwarez.database.model.Place;
import com.pgssoft.testwarez.database.model.PlaceDescription;
import com.pgssoft.testwarez.database.model.SocialMessage;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.database.model.SpeakerDescription;
import com.pgssoft.testwarez.database.model.Staff;
import com.pgssoft.testwarez.database.model.StaffDescription;
import com.pgssoft.testwarez.database.model.Track;
import com.pgssoft.testwarez.database.model.TrackDescription;
import com.pgssoft.testwarez.database.model.Video;
import com.pgssoft.testwarez.database.model.companies.Company;
import com.pgssoft.testwarez.database.model.companies.CompanyType;
import com.pgssoft.testwarez.database.model.companies.CompanyTypeDescriptions;
import com.pgssoft.testwarez.database.model.companies.Organizers;
import com.pgssoft.testwarez.database.model.companies.Partners;
import com.pgssoft.testwarez.database.model.companies.Sponsors;

import java.sql.SQLException;

/**
 * Created by rtulaza on 2015-08-07.
 */
public class TestWarezDatebaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "TestWarezDB.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Category, Integer> categoryDao = null;
    private Dao<Event, Integer> eventDao = null;
    private Dao<Speaker, Integer> speakerDao = null;
    private Dao<EventDescription, Integer> eventDescriptionsDao = null;
    private Dao<CategoryDescription, Integer> categoryDescriptionsDao = null;
    private Dao<SpeakerDescription, Integer> speakerDescriptionsDao = null;
    private Dao<EventToSpeaker, Integer> eventToSpeakersDao = null;
    private Dao<Favorite, Integer> favoriteDao = null;
    private Dao<Organizers, Integer> organizersDao = null;
    private Dao<Sponsors, Integer> sponsorsDao = null;
    private Dao<Partners, Integer> partnersDao = null;
    private Dao<Company, Integer> companyDao = null;
    private Dao<CompanyType, Integer> companyTypesDao = null;
    private Dao<CompanyTypeDescriptions, Integer> companyTypeDescriptionsDao = null;
    private Dao<Conference, Integer> conferenceDao = null;
    private Dao<Staff, Integer> staffDao = null;
    private Dao<EventToStaff, Integer> eventToStaffDao = null;
    private Dao<StaffDescription, Integer> staffDescriptionDao = null;
    private Dao<ConferenceDescription, Integer> conferenceDescriptionsDao = null;
    private Dao<BuildingPlan, Integer> buildingPlanDao = null;
    private Dao<BuildingDescription, Integer> buildingDescriptionDao = null;
    private Dao<Place, Integer> placeDao = null;
    private Dao<PlaceDescription, Integer> placeDescriptionsDao = null;
    private Dao<Track, Integer> trackDao = null;
    private Dao<TrackDescription, Integer> trackDescriptionDao = null;
    private Dao<Message, Integer> messageDao = null;
    private Dao<SocialMessage, Integer> socialMessageDao = null;
    private Dao<EventToTrack, Integer> eventToTrackDao = null;
    private Dao<Gallery, Integer> galleriesDao = null;
    private Dao<Image, Integer> imagesDao = null;
    private Dao<LandingPageDescription, Integer> landingPageDescriptionDao = null;
    private Dao<Video, Integer> videoDao = null;
    private Dao<Archive, Integer> archivesDao = null;
    private Dao<EventToArchives, Integer> eventToArchivesDao = null;
    private Dao<Language, Integer> languageDao = null;
    private Dao<BEFile, Integer> filesDao = null;
    private Dao<GalleryBEFile, Integer> galleryFilesDao = null;

    public TestWarezDatebaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, BEFile.class);
            TableUtils.createTable(connectionSource, Category.class);
            TableUtils.createTable(connectionSource, CategoryDescription.class);
            TableUtils.createTable(connectionSource, Event.class);
            TableUtils.createTable(connectionSource, EventDescription.class);
            TableUtils.createTable(connectionSource, Speaker.class);
            TableUtils.createTable(connectionSource, SpeakerDescription.class);
            TableUtils.createTable(connectionSource, EventToSpeaker.class);
            TableUtils.createTable(connectionSource, Favorite.class);
            TableUtils.createTable(connectionSource, Organizers.class);
            TableUtils.createTable(connectionSource, Sponsors.class);
            TableUtils.createTable(connectionSource, Partners.class);
            TableUtils.createTable(connectionSource, Company.class);
            TableUtils.createTable(connectionSource, CompanyType.class);
            TableUtils.createTable(connectionSource, CompanyTypeDescriptions.class);
            TableUtils.createTable(connectionSource, Conference.class);
            TableUtils.createTable(connectionSource, ConferenceDescription.class);
            TableUtils.createTable(connectionSource, BuildingPlan.class);
            TableUtils.createTable(connectionSource, BuildingDescription.class);
            TableUtils.createTable(connectionSource, Place.class);
            TableUtils.createTable(connectionSource, PlaceDescription.class);
            TableUtils.createTable(connectionSource, Track.class);
            TableUtils.createTable(connectionSource, TrackDescription.class);
            TableUtils.createTable(connectionSource, Staff.class);
            TableUtils.createTable(connectionSource, EventToStaff.class);
            TableUtils.createTable(connectionSource, EventToTrack.class);
            TableUtils.createTable(connectionSource, Message.class);
            TableUtils.createTable(connectionSource, SocialMessage.class);
            TableUtils.createTable(connectionSource, StaffDescription.class);
            TableUtils.createTable(connectionSource, Gallery.class);
            TableUtils.createTable(connectionSource, Image.class);
            TableUtils.createTable(connectionSource, LandingPageDescription.class);
            TableUtils.createTable(connectionSource, Video.class);
            TableUtils.createTable(connectionSource, Archive.class);
            TableUtils.createTable(connectionSource, EventToArchives.class);
            TableUtils.createTable(connectionSource, Language.class);
            TableUtils.createTable(connectionSource, GalleryBEFile.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Category.class, true);
            TableUtils.dropTable(connectionSource, CategoryDescription.class, true);
            TableUtils.dropTable(connectionSource, Event.class, true);
            TableUtils.dropTable(connectionSource, EventDescription.class, true);
            TableUtils.dropTable(connectionSource, Speaker.class, true);
            TableUtils.dropTable(connectionSource, SpeakerDescription.class, true);
            TableUtils.dropTable(connectionSource, EventToSpeaker.class, true);
            TableUtils.dropTable(connectionSource, Favorite.class, true);
            TableUtils.dropTable(connectionSource, Organizers.class, true);
            TableUtils.dropTable(connectionSource, Sponsors.class, true);
            TableUtils.dropTable(connectionSource, Partners.class, true);
            TableUtils.dropTable(connectionSource, Company.class, true);
            TableUtils.dropTable(connectionSource, CompanyType.class, true);
            TableUtils.dropTable(connectionSource, CompanyTypeDescriptions.class, true);
            TableUtils.dropTable(connectionSource, Conference.class, true);
            TableUtils.dropTable(connectionSource, ConferenceDescription.class, true);
            TableUtils.dropTable(connectionSource, BuildingPlan.class, true);
            TableUtils.dropTable(connectionSource, BuildingDescription.class, true);
            TableUtils.dropTable(connectionSource, Place.class, true);
            TableUtils.dropTable(connectionSource, PlaceDescription.class, true);
            TableUtils.dropTable(connectionSource, Track.class, true);
            TableUtils.dropTable(connectionSource, TrackDescription.class, true);
            TableUtils.dropTable(connectionSource, EventToTrack.class, true);
            TableUtils.dropTable(connectionSource, Message.class, true);
            TableUtils.dropTable(connectionSource, SocialMessage.class, true);
            TableUtils.dropTable(connectionSource, Staff.class, true);
            TableUtils.dropTable(connectionSource, EventToStaff.class, true);
            TableUtils.dropTable(connectionSource, StaffDescription.class, true);
            TableUtils.dropTable(connectionSource, Gallery.class, true);
            TableUtils.dropTable(connectionSource, Image.class, true);
            TableUtils.dropTable(connectionSource, LandingPageDescription.class, true);
            TableUtils.dropTable(connectionSource, Video.class, true);
            TableUtils.dropTable(connectionSource, Archive.class, true);
            TableUtils.dropTable(connectionSource, EventToArchives.class, true);
            TableUtils.dropTable(connectionSource, Language.class, true);
            TableUtils.dropTable(connectionSource, BEFile.class, true);
            TableUtils.dropTable(connectionSource, GalleryBEFile.class, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        onCreate(database, connectionSource);
    }

    public Dao<Category, Integer> getCategoryDao() {
        if (categoryDao == null) {
            try {
                categoryDao = getDao(Category.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return categoryDao;
    }

    public Dao<Archive, Integer> getArchivesDao() {
        if (archivesDao == null) {
            try {
                archivesDao = getDao(Archive.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }
        return archivesDao;
    }

    public Dao<Gallery, Integer> getGalleriesDao() {
        if (galleriesDao == null) {
            try {
                galleriesDao = getDao(Gallery.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return galleriesDao;
    }

    public Dao<Image, Integer> getImagesDao() {
        if (imagesDao == null) {
            try {
                imagesDao = getDao(Image.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return imagesDao;
    }

    public Dao<Event, Integer> getEventDao() {
        if (eventDao == null) {
            try {
                eventDao = getDao(Event.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return eventDao;
    }

    public Dao<Speaker, Integer> getSpeakerDao() {
        if (speakerDao == null) {
            try {
                speakerDao = getDao(Speaker.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return speakerDao;
    }

    public Dao<Staff, Integer> getStaffDao() {
        beginTransaction();
        if (staffDao == null) {
            try {
                staffDao = getDao(Staff.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }
        endTransaction();

        return staffDao;
    }

    public Dao<EventToStaff, Integer> getEventToStaffDao() {
        if (eventToStaffDao == null) {
            try {
                eventToStaffDao = getDao(EventToStaff.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return eventToStaffDao;
    }

    public Dao<EventToArchives, Integer> getEventToArchivesDao() {
        if (eventToArchivesDao == null) {
            try {
                eventToArchivesDao = getDao(EventToArchives.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return eventToArchivesDao;
    }

    public Dao<StaffDescription, Integer> getStaffDescriptionDao() {
        if (staffDescriptionDao == null) {
            try {
                staffDescriptionDao = getDao(StaffDescription.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return staffDescriptionDao;
    }

    public Dao<EventDescription, Integer> getEventDescriptionsDao() {
        if (eventDescriptionsDao == null) {
            try {
                eventDescriptionsDao = getDao(EventDescription.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return eventDescriptionsDao;
    }

    public Dao<CategoryDescription, Integer> getCategoryDescriptionsDao() {
        if (categoryDescriptionsDao == null) {
            try {
                categoryDescriptionsDao = getDao(CategoryDescription.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return categoryDescriptionsDao;
    }

    public Dao<SpeakerDescription, Integer> getSpeakerDescriptionsDao() {
        if (speakerDescriptionsDao == null) {
            try {
                speakerDescriptionsDao = getDao(SpeakerDescription.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return speakerDescriptionsDao;
    }

    public Dao<EventToSpeaker, Integer> getEventToSpeakersDao() {
        if (eventToSpeakersDao == null) {
            try {
                eventToSpeakersDao = getDao(EventToSpeaker.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return eventToSpeakersDao;
    }

    public Dao<Favorite, Integer> getFavoriteDao() {
        if (favoriteDao == null) {
            try {
                favoriteDao = getDao(Favorite.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return favoriteDao;
    }

    public Dao<Organizers, Integer> getOrganizersDao() {
        if (organizersDao == null) {
            try {
                organizersDao = getDao(Organizers.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return organizersDao;
    }

    public Dao<Sponsors, Integer> getSponsorsDao() {
        if (sponsorsDao == null) {
            try {
                sponsorsDao = getDao(Sponsors.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return sponsorsDao;
    }

    public Dao<Partners, Integer> getPartnersDao() {
        if (partnersDao == null) {
            try {
                partnersDao = getDao(Partners.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return partnersDao;
    }

    public Dao<Company, Integer> getCompanyDao() {
        if (companyDao == null) {
            try {
                companyDao = getDao(Company.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return companyDao;
    }

    public Dao<CompanyType, Integer> getCompanyTypesDao() {
        if (companyTypesDao == null) {
            try {
                companyTypesDao = getDao(CompanyType.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return companyTypesDao;
    }

    public Dao<CompanyTypeDescriptions, Integer> getCompanyTypeDescriptionsDao() {
        if (companyTypeDescriptionsDao == null) {
            try {
                companyTypeDescriptionsDao = getDao(CompanyTypeDescriptions.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return companyTypeDescriptionsDao;
    }

    public Dao<Conference, Integer> getConferenceDao() {
        if (conferenceDao == null) {
            try {
                conferenceDao = getDao(Conference.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }


        return conferenceDao;
    }

    public Dao<ConferenceDescription, Integer> getConferenceDescriptionsDao() {
        if (conferenceDescriptionsDao == null) {
            try {
                conferenceDescriptionsDao = getDao(ConferenceDescription.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return conferenceDescriptionsDao;
    }

    public Dao<BuildingPlan, Integer> getBuildingPlanDao() {
        if (buildingPlanDao == null) {
            try {
                buildingPlanDao = getDao(BuildingPlan.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return buildingPlanDao;
    }

    public Dao<BuildingDescription, Integer> getBuildingDescriptionDao() {
        if (buildingDescriptionDao == null) {
            try {
                buildingDescriptionDao = getDao(BuildingDescription.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return buildingDescriptionDao;
    }

    public Dao<Place, Integer> getPlacesDao() {
        if (placeDao == null) {
            try {
                placeDao = getDao(Place.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return placeDao;
    }

    public Dao<PlaceDescription, Integer> getPlaceDescriptionsDao() {
        if (placeDescriptionsDao == null) {
            try {
                placeDescriptionsDao = getDao(PlaceDescription.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return placeDescriptionsDao;
    }


    public Dao<Track, Integer> getTrackDao() {
        if (trackDao == null) {
            try {
                trackDao = getDao(Track.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }
        return trackDao;
    }

    public Dao<TrackDescription, Integer> getTrackDescriptionDao() {
        if (trackDescriptionDao == null) {
            try {
                trackDescriptionDao = getDao(TrackDescription.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }
        return trackDescriptionDao;
    }

    public Dao<EventToTrack, Integer> getEventToTrackDao() {
        if (eventToTrackDao == null) {
            try {
                eventToTrackDao = getDao(EventToTrack.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }
        return eventToTrackDao;
    }

    public Dao<Message, Integer> getMessageDao() {
        if (messageDao == null) {
            try {
                messageDao = getDao(Message.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }
        return messageDao;
    }

    public Dao<SocialMessage, Integer> getSocialMessageDao() {
        if (socialMessageDao == null) {
            try {
                socialMessageDao = getDao(SocialMessage.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }
        return socialMessageDao;
    }

    public Dao<LandingPageDescription, Integer> getLandingPageDescriptionDao() {
        if (landingPageDescriptionDao == null) {
            try {
                landingPageDescriptionDao = getDao(LandingPageDescription.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }
        return landingPageDescriptionDao;
    }

    public Dao<Video, Integer> getVideoDao() {
        if (videoDao == null) {
            try {
                videoDao = getDao(Video.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }
        return videoDao;
    }


    public Dao<Language, Integer> getLanguageDao() {
        if (languageDao == null) {
            try {
                languageDao = getDao(Language.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }
        return languageDao;
    }

    public Dao<BEFile, Integer> getFilesDao() {
        if (filesDao == null) {
            try {
                filesDao = getDao(BEFile.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return filesDao;
    }

    public Dao<GalleryBEFile, Integer> getGalleryFilesDao() {
        if (galleryFilesDao == null) {
            try {
                galleryFilesDao = getDao(GalleryBEFile.class);
            } catch (SQLException ex) {
                Log.d(getClass().getName(), "error creating DAO", ex);
                throw new RuntimeException(ex);
            }
        }

        return galleryFilesDao;
    }

    public void clearDatabase() {
        try {
            TableUtils.clearTable(getConnectionSource(), Category.class);
            TableUtils.clearTable(getConnectionSource(), CategoryDescription.class);
            TableUtils.clearTable(getConnectionSource(), Event.class);
            TableUtils.clearTable(getConnectionSource(), EventDescription.class);
            TableUtils.clearTable(getConnectionSource(), EventToSpeaker.class);
            TableUtils.clearTable(getConnectionSource(), Speaker.class);
            TableUtils.clearTable(getConnectionSource(), SpeakerDescription.class);
            TableUtils.clearTable(getConnectionSource(), Organizers.class);
            TableUtils.clearTable(getConnectionSource(), Sponsors.class);
            TableUtils.clearTable(getConnectionSource(), Partners.class);
            TableUtils.clearTable(getConnectionSource(), Company.class);
            TableUtils.clearTable(getConnectionSource(), CompanyType.class);
            TableUtils.clearTable(getConnectionSource(), CompanyTypeDescriptions.class);
            TableUtils.clearTable(getConnectionSource(), Conference.class);
            TableUtils.clearTable(getConnectionSource(), ConferenceDescription.class);
            TableUtils.clearTable(getConnectionSource(), BuildingPlan.class);
            TableUtils.clearTable(getConnectionSource(), BuildingDescription.class);
            TableUtils.clearTable(getConnectionSource(), Place.class);
            TableUtils.clearTable(getConnectionSource(), PlaceDescription.class);
            TableUtils.clearTable(getConnectionSource(), Track.class);
            TableUtils.clearTable(getConnectionSource(), TrackDescription.class);
            TableUtils.clearTable(getConnectionSource(), EventToTrack.class);
            TableUtils.clearTable(getConnectionSource(), Message.class);
            TableUtils.clearTable(getConnectionSource(), SocialMessage.class);
            TableUtils.clearTable(getConnectionSource(), Staff.class);
            TableUtils.clearTable(getConnectionSource(), EventToStaff.class);
            TableUtils.clearTable(getConnectionSource(), StaffDescription.class);
            TableUtils.clearTable(getConnectionSource(), Gallery.class);
            TableUtils.clearTable(getConnectionSource(), Image.class);
            TableUtils.clearTable(getConnectionSource(), LandingPageDescription.class);
            TableUtils.clearTable(getConnectionSource(), Video.class);
            TableUtils.clearTable(getConnectionSource(), Archive.class);
            TableUtils.clearTable(getConnectionSource(), EventToArchives.class);
            TableUtils.clearTable(getConnectionSource(), Language.class);
            TableUtils.clearTable(getConnectionSource(), BEFile.class);
            TableUtils.clearTable(getConnectionSource(), GalleryBEFile.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void beginTransaction() {
        getWritableDatabase().beginTransaction();
    }

    public void endTransaction() {
        getWritableDatabase().setTransactionSuccessful();
        getWritableDatabase().endTransaction();
    }

    public void clearTable(Class classToClear) {
        try {
            TableUtils.clearTable(getConnectionSource(), classToClear);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
