package com.pgssoft.testwarez.feature.agenda.filter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseFilterAdapter;
import com.pgssoft.testwarez.database.model.Track;
import com.pgssoft.testwarez.util.Observables;
import com.pgssoft.testwarez.util.Utils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpodolak on 08.03.16.
 */
public class AgendaFilterAdapter extends BaseFilterAdapter {

    private Context context;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private List<Object> objectList = new ArrayList<>();

    private boolean allowTrack;

    /**
     * Store days of year of conference
     */
    private List<Integer> dayCategoryList = new ArrayList<>();

    /**
     * Store id of category which start day of conference and should be display
     */
    private ArrayList<Integer> selectedDayList = (ArrayList<Integer>) Utils.getAgendaDayFilter();

    /**
     * Store categories which start day of conference
     */
    private List<DayCategoryWrapper> categoryList = new ArrayList<>();

    /**
     * Store id of tracks of conference which should display
     */
    private ArrayList<Integer> selectedTrackList = (ArrayList<Integer>) Utils.getAgendaTrackFilter();

    /**
     * Store all track of active conference
     */
    private List<Track> trackList = new ArrayList<>();

    /**
     * Position of track title on objectList
     */
    private int objectDayTitleId = 0;
    private int objectTrackTitleId;


    /**
     * Observable with available tracks filter for active conference
     */
    private final Observable<Filter> trackObservable = Observables.getInstance().getTrackObservable()
            .map(t -> {
                Filter filter = new Filter(t.getTitle(), Type.TRACK, t.getId());
                filter.trackColor = t.getColor();
                filter.tag = t;
                trackList.add(t);
                return filter;
            }).toList()
            .map(filters -> {
                if (selectedTrackList.isEmpty()) {
                    for (Filter f : filters) {
                        selectedTrackList.add(f.id);
                    }
                } else {
                    for (Filter f : filters) {
                        f.isSelected = selectedTrackList.contains(new Integer(f.id));
                    }
                }
                return filters;
            })
            .flatMap(Observable::from);


    /**
     * Observable with all titles and filters. All item in this observable are adequate in appropriate queue
     */
    private Observable<Object> listObjectObservable;


    public AgendaFilterAdapter(Context context) {
        super(context);
        this.context = context;

        compositeSubscription.add(
                Observable.defer(() -> Observable.just(ApplicationController.getActiveConference()))
                        .flatMap(conference -> {
                            allowTrack = conference.allowTracks();
                            return Observables.getInstance().getDayFilterObservable(context);
                        })
                        .doOnNext(filter -> dayCategoryList.add(filter.id))
                        .toList()
                        .map(filters -> {
                            if (selectedDayList.isEmpty()) {
                                for (Filter f : filters) {
                                    selectedDayList.add(f.id);
                                }
                            } else {
                                for (Filter f : filters) {
                                    f.isSelected = selectedDayList.contains(new Integer(f.id));
                                }
                            }
                            return filters;
                        })

                        .flatMap(Observable::from)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(Schedulers.newThread())
                        .subscribe(filter -> categoryList.add((DayCategoryWrapper) filter.getTag()), Throwable::printStackTrace, () -> showFilters(context)));


    }

    private void showFilters(Context context) {
        final Observable<Title> dayTitleObservable = Observable.just(new Title(context.getString(R.string.day_of_conferences), Type.DAY));
        final Observable<Title> trackTitleObservable = Observable.just(new Title(context.getString(R.string.tracks), Type.TRACK));

        listObjectObservable = Observable.merge(dayTitleObservable, Observables.getInstance().getDayFilterObservable(context));

        if (allowTrack) {
            listObjectObservable = listObjectObservable.concatWith(trackTitleObservable).concatWith(trackObservable);
        }

        listObjectObservable = listObjectObservable.subscribeOn(Schedulers.newThread()).replay().autoConnect();


        compositeSubscription.add(
                listObjectObservable
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> {
                                    if (o instanceof Title && ((Title) o).type == Type.TRACK) {
                                        objectTrackTitleId = objectList.size();
                                    }

                                    objectList.add(o);
                                    notifyDataSetChanged();
                                }, Throwable::printStackTrace,
                                () -> {
                                    ((Title) objectList.get(objectDayTitleId)).isSelected = selectedDayList.size() == dayCategoryList.size();
                                    if (allowTrack) {
                                        ((Title) objectList.get(objectTrackTitleId)).isSelected = selectedTrackList.size() == trackList.size();
                                        greyTrackFilter();
                                    }

                                }));
    }


    @Override
    public void afterBind(RecyclerView.ViewHolder holder, Object item, int position) {
        if (holder instanceof TitleViewHolder) {
            Title title = (Title) item;
            holder.itemView.setOnClickListener(v -> {
                if (!title.isSelected) {
                    title.isSelected = !title.isSelected;
                    ((TitleViewHolder) holder).marker.setVisibility(title.isSelected ? View.VISIBLE : View.GONE);
                    selectItems(title);
                }
            });
        } else if (holder instanceof FilterDayViewHolder) {

            Filter filter = (Filter) item;
            FilterDayViewHolder fdvh = (FilterDayViewHolder) holder;

            if (position == objectTrackTitleId - 1) {
                fdvh.separator.setVisibility(View.GONE);
            }

            if (!filter.enabled) {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.filter_item_bg_grey));
                fdvh.date.setTextColor(context.getResources().getColor(R.color.filter_item_text_grey));
                fdvh.dayNumber.setTextColor(context.getResources().getColor(R.color.filter_item_text_grey));
            } else {
                holder.itemView.setBackground(context.getResources().getDrawable(R.drawable.simple_button_bg));
                fdvh.date.setTextColor(Color.BLACK);
                fdvh.dayNumber.setTextColor(Color.BLACK);
            }

            holder.itemView.setOnClickListener(v -> {

                if (!filter.enabled) {
                    return;
                }

                if (selectedDayList.size() > 1 || !filter.isSelected) {
                    filter.isSelected = !filter.isSelected;
                    ((FilterDayViewHolder) holder).setChecked(filter.isSelected);
                } else {
                    Toast.makeText(context, R.string.atleast_one_item_should_be_selected, Toast.LENGTH_SHORT).show();
                }

                checkAllItem(filter);
            });


        } else if (holder instanceof FilterTrackViewHolder) {
            Filter filter = (Filter) item;
            FilterTrackViewHolder ftvh = (FilterTrackViewHolder) holder;

            if (!filter.enabled) {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.filter_item_bg_grey));
                ftvh.title.setTextColor(context.getResources().getColor(R.color.filter_item_text_grey));
            } else {
                holder.itemView.setBackground(context.getResources().getDrawable(R.drawable.simple_button_bg));
                ftvh.title.setTextColor(Color.BLACK);
            }
            holder.itemView.setOnClickListener(v -> {

                if (!filter.enabled) {
                    return;
                }

                if ((getFilterTracksEnabled().size() > 1 && getFilterTrackSelected().size() > 1) || !filter.isSelected) {

                    filter.isSelected = !filter.isSelected;
                    ftvh.setChecked(filter.isSelected);
                } else {
                    Toast.makeText(context, R.string.atleast_one_item_should_be_selected, Toast.LENGTH_SHORT).show();
                }

                checkAllItem(filter);
            });
        }
    }

    /**
     * Call after item click. Change state of dayNumber when all items are selected or some item isn't selected
     *
     * @param filter
     */
    private void checkAllItem(Filter filter) {

        if (filter.type == Type.DAY) {
            //selected state is state earlier
            if (selectedDayList.contains(filter.id) && !filter.isSelected) {
                selectedDayList.remove(new Integer(filter.id));
            } else if (!selectedDayList.contains(filter.id) && filter.isSelected) {
                selectedDayList.add(filter.id);
            }

            ((Title) objectList.get(objectDayTitleId)).isSelected = selectedDayList.size() == dayCategoryList.size();


            if (allowTrack) {
                greyTrackFilter();
            }

        } else if (filter.type == Type.TRACK) {
            if (selectedTrackList.contains(filter.id) && !filter.isSelected) {
                selectedTrackList.remove(new Integer(filter.id));
            } else if (!selectedTrackList.contains(filter.id) && filter.isSelected) {
                selectedTrackList.add(filter.id);
            }

            ((Title) objectList.get(objectTrackTitleId)).isSelected = selectedTrackList.size() == trackList.size();

            greyDayFilter();
        }

        notifyDataSetChanged();
    }

    private void greyDayFilter() {

        selectedDayList.clear();
        compositeSubscription.add(
                Observables.getInstance().getDayFilterObservable(context)
                        .subscribe(filter -> {
                            DayCategoryWrapper dayCategoryWrapper = (DayCategoryWrapper) filter.getTag();

                            boolean dayContainsSelectedTracks = false;
                            for (Integer trackId : selectedTrackList) {
                                if (dayCategoryWrapper.getAvailableTracksId().contains(trackId)) {
                                    dayContainsSelectedTracks = true;
                                    break;
                                }
                            }

                            filter.enabled = dayContainsSelectedTracks;
                            filter.isSelected &= dayContainsSelectedTracks;

                            if (filter.isSelected) {
                                selectedDayList.add(filter.id);
                            }
                        }, Throwable::printStackTrace, () -> {
                            ((Title) objectList.get(objectDayTitleId)).isSelected = selectedDayList.size() == dayCategoryList.size();
                            notifyDataSetChanged();
                        }));

    }

    /**
     * get trackId for all selected days and next grey tracks which are not in these days
     */
    private void greyTrackFilter() {
        List<Integer> availableTracks = new ArrayList<>();

        selectedTrackList.clear();
        compositeSubscription.add(
                Observable.from(categoryList)
                        .filter(dayCategoryWrapper -> selectedDayList.contains(new Integer(dayCategoryWrapper.category.getId())))
                        .flatMap(dayCategoryWrapper1 -> Observable.from(dayCategoryWrapper1.getAvailableTracksId()))
                        .distinct()
                        .doOnNext(availableTrack -> availableTracks.add(availableTrack))
                        .toList()
                        .flatMap(integer1 -> Observable.from(getTrackFilter()))
                        .subscribe(f -> {
                            Track track = (Track) f.tag;

                            if (!availableTracks.contains(track.getId())) {
                                //if availableTracks hasn't required track id, set this searchFilter to disable
                                f.enabled = false;
                                f.isSelected = false;
                                selectedTrackList.remove(Integer.valueOf(f.id));
                            } else {
                                f.enabled = true;
                                if (f.isSelected && !selectedTrackList.contains(f.id)) {
                                    selectedTrackList.add(f.id);
                                }
                            }
                        }, Throwable::printStackTrace, () -> {
                            if (selectedTrackList.isEmpty()) {
                                List<Filter> enableTrack = getFilterTracksEnabled();
                                Filter lastFilter = enableTrack.get(enableTrack.size() - 1);
                                lastFilter.isSelected = true;


                                ((Title) objectList.get(objectTrackTitleId)).isSelected = selectedTrackList.size() == trackList.size();
                                if (!selectedTrackList.contains(lastFilter.id)) {
                                    selectedTrackList.add(lastFilter.id);
                                }
                            }
                        }));
    }

    public List<Filter> getFilterTracksEnabled() {
        List<Filter> enableTrack = new ArrayList<>();

        compositeSubscription.add(
                Observable.from(getTrackFilter())
                        .doOnError(Throwable::printStackTrace)
                        .filter(filter1 -> filter1.enabled)
                        .subscribe(enableTrack::add));

        return enableTrack;
    }

    /**
     * Get list of filters with track
     *
     * @return
     */
    private List<Filter> getTrackFilter() {
        List<Filter> filter = new ArrayList<>();

        compositeSubscription.add(
                Observable.from(objectList)
                        .doOnError(Throwable::printStackTrace)
                        .filter(o1 -> o1 instanceof Filter && ((Filter) o1).type == Type.TRACK)
                        .subscribe(o -> filter.add((Filter) o)));

        return filter;
    }

    public List<Filter> getFilters() {
        List<Filter> filterList = new ArrayList<>();

        compositeSubscription.add(
                Observable.from(objectList)
                        .doOnError(Throwable::printStackTrace)
                        .filter(o1 -> o1 instanceof Filter)
                        .subscribe(o2 -> filterList.add((Filter) o2)));

        return filterList;
    }

    /**
     * Call when all items from dayNumber section should mark or not
     *
     * @param title
     */
    private void selectItems(Title title) {

        if (title.type == Type.DAY) {
            selectedDayList.clear();
        } else if (title.type == Type.TRACK) {
            selectedTrackList.clear();
        }

        compositeSubscription.add(
                Observable.from(getFilters())
                        .filter(filter1 -> filter1.type == title.type)
                        .toList()
                        .flatMap(Observable::from)
                        .subscribe(filter2 -> {
                                    filter2.setSelected(title.isSelected);
                                    if (filter2.type == Type.DAY) {
                                        //selected state is state earlier
                                        selectedDayList.add(filter2.id);
                                    } else if (filter2.type == Type.TRACK && filter2.enabled) {
                                        selectedTrackList.add(filter2.id);
                                    }
                                }, Throwable::printStackTrace,
                                () -> {
                                    if (allowTrack) {
                                        greyTrackFilter();
                                        ((Title) objectList.get(objectTrackTitleId)).isSelected = selectedTrackList.size() == trackList.size();

                                        greyDayFilter();
                                    }

                                    notifyDataSetChanged();
                                }));
    }

    /**
     * Set all items from all sections
     */
    @Override
    public void removeFilter() {

        selectedDayList.clear();
        selectedTrackList.clear();
        compositeSubscription.add(Observable.just(objectList.get(objectDayTitleId), objectList.get(objectTrackTitleId))
                .map(object -> (Title) object)
                .doOnNext(title1 -> title1.setSelected(true))
                .flatMap(title -> Observable.from(getFilters())
                        .filter(filter1 -> filter1.type == title.type))

                .toList()
                .flatMap(Observable::from)
                .subscribe(filter2 -> {
                            filter2.setSelected(true);
                            filter2.enabled = true;

                            if (filter2.type == Type.DAY) {
                                selectedDayList.add(filter2.id);
                            } else if (filter2.type == Type.TRACK) {
                                selectedTrackList.add(filter2.id);
                            }

                        }, Throwable::printStackTrace,
                        () -> notifyDataSetChanged()));


    }

    /**
     * Get selected tracks
     *
     * @return
     */
    public List<Track> getFilterTrackSelected() {
        List<Track> trackList = new ArrayList<>();
        for (Filter filter : getTrackFilter()) {
            if (filter.type == Type.TRACK && filter.isSelected) {
                trackList.add((Track) filter.tag);
            }
        }
        return trackList;
    }

    @Override
    public boolean isFilterOn() {
        boolean isFilterOn = false;

        for (Object o : objectList) {
            if (o instanceof Filter && !((Filter) o).isSelected) {
                isFilterOn = true;
                break;
            }
        }

        return isFilterOn;
    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (objectList.get(position) instanceof Title) {
            return TITLE_HOLDER;
        } else if (((Filter) objectList.get(position)).type == Type.DAY) {
            return FILTER_DAY_HOLDER;
        } else {
            return FILTER_TRACK_HOLDER;
        }
    }

    @Override
    public void saveFilter() {

        if (selectedDayList.size() == dayCategoryList.size()) {
            selectedDayList.clear();
        }

        if (selectedTrackList.size() == trackList.size()) {
            selectedTrackList.clear();
        }

        Utils.saveAgendaFilter(selectedDayList, selectedTrackList);
        compositeSubscription.clear();
        compositeSubscription.unsubscribe();
    }

    @Override
    public Object getItem(int position) {
        return objectList.get(position);
    }


}
