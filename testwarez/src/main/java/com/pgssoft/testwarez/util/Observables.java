package com.pgssoft.testwarez.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.j256.ormlite.stmt.QueryBuilder;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.feature.agenda.all.AgendaAdapter;
import com.pgssoft.testwarez.feature.archive.video.ArchiveVideoAdapter;
import com.pgssoft.testwarez.core.BaseFilterAdapter;
import com.pgssoft.testwarez.feature.archive.event.ExpandableArchiveEventAdapter;
import com.pgssoft.testwarez.database.model.Archive;
import com.pgssoft.testwarez.database.model.Category;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Favorite;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.database.model.Track;
import com.pgssoft.testwarez.database.model.Video;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by dpodolak on 22.04.16.
 */
public class Observables {

    private static Observables instance;

    public Observables() {
        eventObservables = new EventObservables();
        conferenceObservables = new ConferenceObservables();
        speakerObservable = new SpeakerObservable();
        videoObservable = new VideoObservable();
    }

    public synchronized static Observables getInstance() {
        if (instance == null) {
            instance = new Observables();
        }

        return instance;
    }

    private final ConferenceObservables conferenceObservables;
    private final EventObservables eventObservables;
    private final SpeakerObservable speakerObservable;
    private final VideoObservable videoObservable;

    private Observable<ArchiveVideoAdapter.ListItem> archiveVideoObservable;

    private Observable<Event> favoriteEventObservable = Observable.defer(() -> Observable.from(getFavorites()))
            .map(favorite -> favorite.getEvent())
            .toSortedList((event, event1) -> new Long(event.getStartAt().getMillis()).compareTo(event1.getStartAt().getMillis()))
            .flatMap(Observable::from);

    private Observable<Category> categoryObservable;
    private Observable<Track> trackObservable = Observable.defer(() -> Observable.from(loadTrackList())).replay().autoConnect();

    private Observable<BaseFilterAdapter.Filter> categoryFilterObservable;


    Func1<List<Category>, Observable<Category>> dayFlatMap = categories -> {
        List<Integer> day = new ArrayList<>();
        return Observable.from(categories)
                .filter(category -> {
                    DateTime currentStartAt = category.getStartAt();
                    return !day.contains(new Integer(currentStartAt.getDayOfYear()));
                }).doOnNext(category -> day.add(category.getStartAt().getDayOfYear()));
    };

    private Observable<List<AgendaAdapter.ListItem>> agendaElementsObservable;

    /**
     * Observable with days filter available for active conference
     */
    private Observable<Category> dayCategoryObservable;

    public Observable<List<AgendaAdapter.ListItem>> refreshAgendaElements(Context context) {

        return Observable.zip(speakerObservable.refreshSpeakers(),
                refreshCategoryObservable(),
                refreshDayCategoryObservable(),
                getDayFilterObservable(context).toList(), (speaker, category, category2, filter) -> 1)
                .flatMap(integer -> {
                    agendaElementsObservable = null;
                    return getAgendaElements();
                });
    }

    public Observable<List<AgendaAdapter.ListItem>> getAgendaElements() {

        if (agendaElementsObservable == null) {
            agendaElementsObservable = getCategoryObservable()
                    .toList()
                    .flatMap(dayFlatMap)
                    .map(category -> new AgendaAdapter.DayWrapper(category.getStartAt().getDayOfYear()))
                    .flatMap(dayWrapper ->
                            getCategoryObservable()
                                    .filter(category1 -> {
                                        int categoryDay = category1.getStartAt().getDayOfYear();
                                        int currentDay = dayWrapper.getDay();
                                        return currentDay == categoryDay;
                                    })
                                    .filter(category1 -> category1.getTitle() != null && !category1.getTitle().isEmpty())
                                    .toList()
                                    .map(categories -> {
                                        dayWrapper.setCategories(categories);
                                        return dayWrapper;
                                    }))
                    .flatMap(dayWrapper -> {
                        List<AgendaAdapter.ListItem> dayElements = new ArrayList<>();
                        return Observable.from(dayWrapper.getCategories())
                                .doOnNext(category -> dayElements.add(new AgendaAdapter.ListItem(category)))
                                .flatMap(category1 -> {

                                    if (ApplicationController.getActiveConference().allowTracks()) {
                                        return Observable.from(category1.getEvents())
                                                .toSortedList((Event event1, Event event2) -> {
                                                    return sortEventList(event1, event2);

                                                    /*int timeSort = new Long(event1.getStartAt().getMillis()).compareTo(event2.getStartAt().getMillis());
                                                    int trackList1Size = event1.getTrackList().size();
                                                    int trackList2Size = event2.getTrackList().size();
                                                    if (timeSort != 0 || trackList1Size == 0 || trackList2Size == 0) {
                                                        return timeSort;
                                                    } else {
                                                        for (int i = 0; i < event1.getTrackList().size(); i++) {
                                                            if (event2.getTrackList().size() == i) {
                                                                break;
                                                            }

                                                            int result = new Integer(event1.getTrackList().get(i).getId()).compareTo(event2.getTrackList().get(i).getId());

                                                            if (result != 0) {
                                                                return result;
                                                            }
                                                        }


                                                    }*/
                                                })
                                                .flatMap(Observable::from);
                                    } else {
                                        return Observable.from(category1.getEvents())
                                                .toSortedList((event, event2) -> {
                                                    int result = new Long(event.getStartAt().getMillis()).compareTo(event2.getStartAt().getMillis());

                                                    if (result == 0) {
                                                        result = new Integer(event.getPosition()).compareTo(event2.getPosition());
                                                    }

                                                    return result;
                                                })
                                                .flatMap(Observable::from);
                                    }
                                })
                                .doOnNext(event -> dayElements.add(new AgendaAdapter.ListItem(event)))
                                .map(event1 -> dayElements)
                                .toList()
                                .map(list -> dayElements);
                    }).replay().autoConnect();
        }

        return agendaElementsObservable;
    }

    public Observable<Category> refreshCategoryObservable() {
        categoryObservable = null;
        return getCategoryObservable();
    }

    public synchronized Observable<Category> getCategoryObservable() {

        if (categoryObservable == null) {
            categoryObservable = Observable.defer(() -> Observable.from(loadCategories())).replay().autoConnect();
        }
        return categoryObservable;
    }

    public Observable<Track> getTrackObservable() {
        return trackObservable;
    }

    public Observable<BaseFilterAdapter.Filter> getDayFilterObservable(Context context) {

        if (categoryFilterObservable == null) {
            categoryFilterObservable = getDayCategoryObservable()
                    .toList()
                    .flatMap(categories -> {
                        //convert categories to filter
                        final int[] dayCount = {1};
                        return Observable.from(categories)
                                .map(category -> buildFilter(context, dayCount[0], category));
                    })
                    .replay().autoConnect();
        }

        return categoryFilterObservable;
    }

    private Observable<ExpandableArchiveEventAdapter.ListItem> archiveAgendaObservable;

    @NonNull
    private BaseFilterAdapter.Filter buildFilter(Context context, int i, Category category) {
        BaseFilterAdapter.Filter filter = new BaseFilterAdapter.Filter(BaseFilterAdapter.Type.DAY, category.getId());

        BaseFilterAdapter.DayCategoryWrapper dayCategoryWrapper = new BaseFilterAdapter.DayCategoryWrapper(category);
        filter.setTag(dayCategoryWrapper);
        String partTitle = category.getStartAt().toString("EEEE d MMMM", context.getResources().getConfiguration().locale);
        String title = String.format("%1$d DZIEŃ - %2$s", i, partTitle);
        filter.setTitle(title);
        i++;

        filter.setTag(dayCategoryWrapper);

        return filter;
    }


    private List<Track> loadTrackList() {
        List<Track> filterTracks = new ArrayList<>();

        Conference activeConference = ApplicationController.getActiveConference();
        try {
            QueryBuilder<Track, Integer> trackQB = ApplicationController.getDatabaseHelper().getTrackDao().queryBuilder();
            trackQB.where().eq(Track.CONFERENCE_ID, activeConference.getId());
            filterTracks = trackQB.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return filterTracks;
    }

    public Observable<Event> getFavoriteEventObservable() {
        return favoriteEventObservable;
    }

    public Observable<Category> refreshDayCategoryObservable() {
        dayCategoryObservable = null;
        return getDayCategoryObservable();
    }

    public Observable<Category> getDayCategoryObservable() {

        if (dayCategoryObservable == null) {
            dayCategoryObservable = getCategoryObservable()
                    .toList()
                    .flatMap(categories -> {
                        List<Integer> dayCategoryList = new ArrayList<>();
                        return Observable.from(categories)
                                .filter(category -> {
                                    int currentDay = category.getStartAt().getDayOfYear();

                                    if (!dayCategoryList.contains(currentDay)) {
                                        dayCategoryList.add(currentDay);
                                        return true;
                                    } else {
                                        return false;
                                    }
                                });
                    }).replay().autoConnect();
        }

        return dayCategoryObservable;
    }

    /**
     * Load all categories for active conference
     *
     * @return
     */
    private List<Category> loadCategories() {
        List<Category> categories = new ArrayList<>();

        Conference activeConference = ApplicationController.getActiveConference();

        try {
            // load all categories and sort by date
            QueryBuilder<Category, Integer> categoryQB = ApplicationController.getDatabaseHelper().getCategoryDao().queryBuilder();
            categoryQB.orderBy(Category.START_AT_COLUMN_NAME, true);
            categoryQB.where().eq(Category.CONFERENCE_ID, activeConference.getId());
            categories = categoryQB.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    private List<Favorite> getFavorites() {
        List<Favorite> favoriteList = new ArrayList<>();
        Conference activeConference = ApplicationController.getActiveConference();
        try {
            favoriteList.addAll(ApplicationController.getDatabaseHelper().getFavoriteDao().queryForEq(Favorite.CONFERENCE_ID_COLUMN, activeConference.getId()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoriteList;
    }


    public Observable<ArchiveVideoAdapter.ListItem> refreshArchiveVideoAgenda() {
        archiveVideoObservable = null;
        return getArchiveVideoAgenda();
    }

    public Observable<ArchiveVideoAdapter.ListItem> getArchiveVideoAgenda() {
        if (archiveVideoObservable == null) {

            archiveVideoObservable = conferenceObservables.getArchiveConferences()
                    .flatMap(conference3 -> {

                        Observable<Video> archiveObservable = Observable.from(conference3.getArchiveCollection())
                                .filter(archive -> archive.getType().equals(Archive.TYPE_VIDEO))
                                .map(archive -> new Video(archive));

                        Observable<Video> videoObservable = Observable.from(conference3.getVideoCollection())
                                .concatWith(archiveObservable)
                                .filter(Video::isValid)
                                .toSortedList((video, video2) -> video.getName().toLowerCase().compareTo(video2.getName().toLowerCase()))
                                .flatMap(Observable::from);

                        Observable<ArchiveVideoAdapter.ListItem> listItemObservable =
                                Observable.concat(Observable.just(new ArchiveVideoAdapter.ListItem(conference3)),
                                        videoObservable
                                                .map(video -> new ArchiveVideoAdapter.ListItem(video)));

                        return videoObservable
                                .count()
                                .filter(count -> count > 0)
                                .flatMap(count -> listItemObservable);
                    })
                    .replay().autoConnect()
            ;
        }

        return archiveVideoObservable;
    }


    public Observable<ExpandableArchiveEventAdapter.ListItem> refreshArchiveAgendaObservable() {
        archiveAgendaObservable = null;
        return getArchiveAgendaObservable();
    }

    public Observable<ExpandableArchiveEventAdapter.ListItem> getArchiveAgendaObservable() {

        if (archiveAgendaObservable == null) {
            archiveAgendaObservable = conferenceObservables.getArchiveConferences()
                    .map(conference -> new ExpandableArchiveEventAdapter.ListItem(conference))
                    .flatMap(listItem -> {
                        Observable<ExpandableArchiveEventAdapter.ListItem> eventOB = eventObservables.getEventsObservable()
                                .filter(event -> event.getConferenceId() == listItem.getConference().getId())
                                .filter(event -> !event.isTechnical())
                                .filter(event -> !event.getAllArchives().isEmpty())
                                .filter(Event::isArchival)
                                .map(event -> new ExpandableArchiveEventAdapter.ListItem(event));

                        Observable<ExpandableArchiveEventAdapter.ListItem> confOB = Observable.just(listItem);


                        return eventOB.count().filter(integer -> integer > 0).flatMap(integer -> Observable.merge(confOB, eventOB));
                    })
                    .replay().autoConnect();

        }
        return archiveAgendaObservable;
    }

    public ConferenceObservables getConferenceObservables() {
        return conferenceObservables;
    }

    public EventObservables getEventObservables() {
        return eventObservables;
    }

    public SpeakerObservable getSpeakerObservable() {
        return speakerObservable;
    }

    int sortEventList(Event event1, Event event2) {
        int startTimeSort = Long.valueOf(event1.getStartAt().getMillis()).compareTo(event2.getStartAt().getMillis());
        int trackSort = 0;

        for (int i = 0; i < event1.getTrackList().size(); i++) {
            if (event2.getTrackList().size() == i) {
                break;
            }

            trackSort = new Integer(event1.getTrackList().get(i).getId()).compareTo(event2.getTrackList().get(i).getId());

            if (trackSort != 0) {
                break;
            }
        }

        int positionSort = event1.getPosition() == event2.getPosition() ? 0 : event1.getPosition() > event2.getPosition() ? 1 : -1;
        int endTimeSort = Long.valueOf(event1.getEndAt().getMillis()).compareTo(event2.getEndAt().getMillis());
        int nameSort = event1.getTitle().compareTo(event2.getTitle());

        return startTimeSort != 0 ? startTimeSort :
                trackSort != 0 ? trackSort :
                        positionSort != 0 ? positionSort :
                                endTimeSort != 0 ? endTimeSort :
                                        nameSort;
    }

    public class ConferenceObservables {

        private Observable<Conference> archiveConferencesObservable;

        public Observable<Conference> refreshArchiveConferences() {
            archiveConferencesObservable = null;
            return getArchiveConferences();

        }

        public Observable<Conference> getArchiveConferences() {

            if (archiveConferencesObservable == null) {
                archiveConferencesObservable = Observable.defer(() -> Observable.from(loadArchiveConferenceList()))
                        .toSortedList((conference, conference2) -> new Long(conference2.getStartAt().getMillis()).compareTo(conference.getStartAt().getMillis()))
                        .flatMap(Observable::from)
                        .replay().autoConnect();
            }
            return archiveConferencesObservable;
        }
    }

    public List<Conference> loadArchiveConferenceList() {
        List<Conference> conferenceList = new ArrayList<>();

        try {
            QueryBuilder<Conference, Integer> trackQB = ApplicationController.getDatabaseHelper().getConferenceDao().queryBuilder();
            trackQB.where().eq(Conference.STATUS_COLUMN, Conference.CONFERENCE_ARCHIVE);

            conferenceList.addAll(trackQB.query());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conferenceList;
    }

    public class EventObservables {

        private Observable<Event> eventsObservable;

        public boolean isEventsObservableLoaded() {
            return eventsObservable != null;
        }

        public Observable<Event> refreshEventsObservable() {
            eventsObservable = null;
            return getEventsObservable();
        }

        public Observable<Event> getEventsObservable() {

            if (eventsObservable == null) {
                eventsObservable = Observable.defer(() -> Observable.from(loadEvents()))
                        .toSortedList((event, event2) -> event.getTitle().compareTo(event2.getTitle()))
                        .flatMap(Observable::from).replay().autoConnect();
            }

            return eventsObservable;
        }

        public Observable<Event> getActiveEventsObservable() {

            Conference activeConference = ApplicationController.getActiveConference();

            return getEventsObservable()
                    .filter(event -> event.getConferenceId() == activeConference.getId()).replay().autoConnect();
        }

        private List<Event> loadEvents() {
            List<Event> events = new ArrayList<>();

            try {
                events.addAll(ApplicationController.getDatabaseHelper().getEventDao().queryForAll());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return events;
        }
    }

    public class SpeakerObservable {

        private Observable<Speaker> listObservable;

        public Observable<Speaker> refreshSpeakers() {
            listObservable = null;
            return getSpeakers();
        }

        public Observable<Speaker> getSpeakers() {
            if (listObservable == null) {

                listObservable =
                        Observable.defer(() -> Observable.just(ApplicationController.getActiveConference()))
                                .flatMap(activeConference -> Observable.from(activeConference.getEventCollection())
                                        .flatMap(event -> Observable.from(event.getAllSpeakers()))
                                        .distinct(Speaker::getId)
                                        .toSortedList((speaker1, speaker2) -> Collator.getInstance(new Locale("pl", "PL")).compare(speaker1.getFullName(), speaker2.getFullName()))
                                        .flatMap(Observable::from).replay().autoConnect());
            }
            return listObservable;
        }
    }

    public static class VideoObservable {

        private Observable<Video> localVideoObservable = Observable.defer(() -> Observable.from(loadVideoList()));
        private Observable<Video> localArchiveObservable = Observable.defer(() -> Observable.from(loadArchivesList())).map(achive -> new Video(achive));

        private Observable<Video> videosObservable;

        public Observable<Video> getVideosObservable() {

            if (videosObservable == null) {
                videosObservable = Observable.merge(localVideoObservable, localArchiveObservable).replay().autoConnect();
            }

            return videosObservable;
        }

        private List<Video> loadVideoList() {
            List<Video> videoList = new ArrayList<>();

            try {
                videoList.addAll(ApplicationController.getDatabaseHelper().getVideoDao().queryForAll());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return videoList;
        }

        private List<Archive> loadArchivesList() {
            List<Archive> videoList = new ArrayList<>();

            try {
                QueryBuilder<Archive, Integer> archiveQB = ApplicationController.getDatabaseHelper().getArchivesDao().queryBuilder();
                archiveQB.where().eq(Archive.COLUMN_TYPE, Archive.TYPE_VIDEO);
                videoList.addAll(archiveQB.query());

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return videoList;
        }


    }
}
