package com.pgssoft.testwarez.feature.archive.filter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseFilterAdapter;
import com.pgssoft.testwarez.event.FilterArchiveConferencesEvent;
import com.pgssoft.testwarez.database.model.Archive;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.util.Observables;
import com.pgssoft.testwarez.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by brosol on 2016-04-14.
 */
public class ArchiveAgendaFilterAdapter extends BaseFilterAdapter {

    private Context context;

    private static final int FILE_TYPES_COUNT = 5;
    CompositeSubscription compositeSubscription = new CompositeSubscription();
    private List<Object> objectList = new ArrayList<>();
    private List<Integer> selectedConferencesList = new ArrayList<>();
    private List<Integer> selectedFileTypesList = new ArrayList<>();

    private Title conferenceTitle;
    private Title fileTypeTitle;

    private int conferenceSize;
    private int fileTypesSize;

    Observable<Object> allObjectObservable;

    public ArchiveAgendaFilterAdapter(Context context) {
        super(context);
        this.context = context;

        conferenceTitle = new Title(context.getString(R.string.conferences), Type.CONFERENCE);
        fileTypeTitle = new Title(context.getString(R.string.file_types), Type.FILE_TYPE);
        conferenceTitle.setSelected(true);
        fileTypeTitle.setSelected(true);


        selectedConferencesList = Utils.getConferenceFilter();
        selectedFileTypesList = Utils.getArchiveFileTypesFilter();

        Observable<Conference> conferenceWithArchives = Observables.getInstance().getConferenceObservables().getArchiveConferences()
                .filter(Conference::hasArchiveEventsWithArchives)
                .replay().autoConnect();

        fileTypesSize = 0;

        allObjectObservable = Observable.concat(
                Observable.just(conferenceTitle),
                conferenceWithArchives
                        .map(this::mapConferenceOnFilter),
                Observable.just(fileTypeTitle),
                conferenceWithArchives.flatMap(conference -> Observable.from(conference.getArchiveCollection()))
                        .distinct(Archive::getType)
                        .map(this::mapArchiveOnFilter)
                        .doOnNext(filter -> {

                            if (!selectedFileTypesList.isEmpty() && !selectedFileTypesList.contains(filter.id)) {
                                filter.isSelected = false;
                            } else {
                                filter.isSelected = true;
                            }

                            fileTypesSize++;
                        }))
                .subscribeOn(Schedulers.newThread()).replay().autoConnect();


        compositeSubscription.add(
                allObjectObservable
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> {
                                    if (o instanceof Filter) {
                                        Filter f = ((Filter) o);
                                        if (f.type == Type.CONFERENCE && !f.isSelected) {
                                            conferenceTitle.setSelected(false);
                                        } else if (f.type == Type.FILE_TYPE && !f.isSelected) {
                                            fileTypeTitle.setSelected(false);
                                        }
                                    }
                                    objectList.add(o);
                                }, Throwable::printStackTrace, () -> {

                                    if (selectedConferencesList.isEmpty()) {
                                        for (Conference c : getSelectedConferences()) {
                                            selectedConferencesList.add(c.getId());
                                        }
                                    }
                                    if (selectedFileTypesList.isEmpty()) {
                                        for (Integer i : getFilterFileTypesId()) {
                                            selectedFileTypesList.add(i);
                                        }
                                    }

                                    greyFileFilters();
                                }
                        ));

    }

    public Filter mapConferenceOnFilter(Conference conference) {
        Filter filter = new Filter(conference.getName(), Type.CONFERENCE, conference.getId());

        filter.tag = conference;

        if (!selectedConferencesList.isEmpty() && !selectedConferencesList.contains(conference.getId())) {
            filter.isSelected = false;
        }

        conferenceSize++;
        return filter;
    }

    private Filter mapArchiveOnFilter(Archive archive) {
        switch (archive.getType()) {
            case Archive.TYPE_PDF:
                Filter pdfFilter = new Filter(context.getResources().getString(R.string.file_type_pdf), Type.FILE_TYPE, 0);
                pdfFilter.medium = Archive.TYPE_PDF;
                return pdfFilter;

            case Archive.TYPE_DOCUMENT:
                Filter docFilter = new Filter(context.getResources().getString(R.string.file_type_doc), Type.FILE_TYPE, 1);
                docFilter.medium = Archive.TYPE_DOCUMENT;
                return docFilter;

            case Archive.TYPE_PRESENTATION:
                Filter presentationFilter = new Filter(context.getResources().getString(R.string.file_type_presentation), Type.FILE_TYPE, 2);
                presentationFilter.medium = Archive.TYPE_PRESENTATION;
                return presentationFilter;

            case Archive.TYPE_VIDEO:
                Filter videoFilter = new Filter(context.getResources().getString(R.string.file_type_video), Type.FILE_TYPE, 3);
                videoFilter.medium = Archive.TYPE_VIDEO;
                return videoFilter;

            default:
                Filter wwwFilter = new Filter(context.getResources().getString(R.string.file_type_link), Type.FILE_TYPE, 4);
                wwwFilter.medium = Archive.TYPE_LINK;
                return wwwFilter;

        }
    }


    @Override
    public void afterBind(RecyclerView.ViewHolder holder, Object item, int position) {

        if (holder instanceof TitleViewHolder) {
            Title title = (Title) item;
            holder.itemView.setOnClickListener(v -> {
                if (!title.isSelected) {
                    title.isSelected = !title.isSelected;
                    ((TitleViewHolder) holder).marker.setVisibility(title.isSelected ? View.VISIBLE : View.GONE);
                    changeAllToCheck(title);
                }
            });
        } else if (holder instanceof FilterConferenceViewHolder) {
            Filter filter = (Filter) item;
            FilterConferenceViewHolder fcvh = (FilterConferenceViewHolder) holder;

            if (!filter.enabled) {
                holder.itemView.setBackgroundColor(Color.parseColor("#cecece"));
                fcvh.title.setTextColor(Color.parseColor("#666666"));
            } else {
                holder.itemView.setBackground(context.getResources().getDrawable(R.drawable.simple_button_bg));
                fcvh.title.setTextColor(Color.parseColor("#000000"));
            }

            holder.itemView.setOnClickListener(v -> {
                if (!filter.enabled) {
                    return;
                }

                if (selectedConferencesList.size() > 1 || !filter.isSelected) {
                    filter.isSelected = !filter.isSelected;
                    ((FilterConferenceViewHolder) holder).setChecked(filter.isSelected);
                } else {
                    Toast.makeText(context, R.string.atleast_one_item_should_be_selected, Toast.LENGTH_SHORT).show();
                }

                checkAllItem(filter);
            });
        } else if (holder instanceof FilterFileTypeViewHolder) {
            Filter filter = (Filter) item;
            FilterFileTypeViewHolder fftvh = (FilterFileTypeViewHolder) holder;

            if (!filter.enabled) {
                holder.itemView.setBackgroundColor(Color.parseColor("#cecece"));
                fftvh.title.setTextColor(Color.parseColor("#666666"));
            } else {
                holder.itemView.setBackground(context.getResources().getDrawable(R.drawable.simple_button_bg));
                fftvh.title.setTextColor(Color.parseColor("#000000"));
            }

            fftvh.itemView.setOnClickListener(v -> {
                if (!filter.enabled) {
                    return;
                }

                if ((selectedFileTypesList.size() > 1 && getFilterFileTypes().size() > 1) || !filter.isSelected) {
                    filter.isSelected = !filter.isSelected;
                    fftvh.setChecked(filter.isSelected);
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

        if (filter.type == Type.CONFERENCE) {
            if (selectedConferencesList.contains(filter.id) && !filter.isSelected) {
                selectedConferencesList.remove(new Integer(filter.id));
            } else if (!selectedConferencesList.contains(filter.id) && filter.isSelected) {
                selectedConferencesList.add(filter.id);
            }

            conferenceTitle.setSelected(selectedConferencesList.size() == conferenceSize);

            greyFileFilters();
        } else if (filter.type == Type.FILE_TYPE) {
            if (selectedFileTypesList.contains(filter.id) && !filter.isSelected) {
                selectedFileTypesList.remove(new Integer(filter.id));
            } else if (!selectedFileTypesList.contains(filter.id) && filter.isSelected) {
                selectedFileTypesList.add(filter.id);
            }

            fileTypeTitle.setSelected(selectedFileTypesList.size() == fileTypesSize);

            greyConferenceFilters();
        }
    }

    private void greyFileFilters() {

        Observable<Filter> fileTypesObservable = allObjectObservable
                .filter(o -> o instanceof Filter && ((Filter) o).type == Type.FILE_TYPE)
                .map(o2 -> (Filter) o2);

        compositeSubscription.add(
                allObjectObservable
                        .filter(o -> o instanceof Filter && ((Filter) o).type == Type.CONFERENCE && ((Filter) o).isSelected)
                        .map(o1 -> (Conference) ((Filter) o1).tag)
                        .flatMap(conference ->
                                Observable.from(conference.getArchiveCollection()))
                        .distinct(Archive::getType)
                        .map(Archive::getIntType)
                        .filter(integer -> integer > -1)
                        .toList()
                        .flatMap(integers1 -> fileTypesObservable
                                .map(f -> {

                                    f.enabled = integers1.contains(f.id);

                                    if (!f.enabled) {
                                        f.isSelected = f.enabled;
                                    }
                                    return f;
                                }))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(filter -> {
                        }, Throwable::printStackTrace, () -> {

                            List<Filter> filters = getFilterFileTypes();
                            for (Filter filter : filters) {
                                if (selectedFileTypesList.contains(filter.id) && !filter.isSelected) {
                                    selectedFileTypesList.remove(new Integer(filter.id));
                                } else if (!selectedFileTypesList.contains(filter.id) && filter.isSelected) {
                                    selectedFileTypesList.add(filter.id);
                                }
                            }

                            if (selectedFileTypesList.isEmpty()) {
                                Collections.reverse(filters);
                                for (Filter filter : filters) {
                                    if (filter.enabled) {
                                        filter.setSelected(true);
                                        break;
                                    }
                                }

                                greyConferenceFilters();
                            }

                            fileTypeTitle.setSelected(selectedFileTypesList.size() == fileTypesSize);

                            notifyDataSetChanged();

                            ApplicationController.getBus().post(new FilterArchiveConferencesEvent());
                        }));

    }

    private void greyConferenceFilters() {
        Observable<Filter> conferenceFilterObservable = allObjectObservable
                .filter(o -> o instanceof Filter && ((Filter) o).type == Type.CONFERENCE)
                .map(o1 -> (Filter) o1).replay().autoConnect();


        compositeSubscription.add(
                allObjectObservable
                        .filter(o -> o instanceof Filter && ((Filter) o).type == Type.FILE_TYPE && ((Filter) o).isSelected)
                        .map(o2 -> ((Filter) o2).medium)
                        .toList()
                        .flatMap(filter -> conferenceFilterObservable
                                .flatMap(conferenceFilter -> Observable.from(((Conference) conferenceFilter.tag).getArchiveCollection())
                                        .distinct(archive -> archive.getType())
                                        .map(archive1 -> archive1.getType())
                                        .toList() //list of file types in conferenceFilter
                                        .doOnNext(s -> {
                                            List<String> retainList = new ArrayList<>(filter);
                                            retainList.retainAll(s);

                                            if (!retainList.isEmpty() && s.isEmpty()) {
                                                conferenceFilter.enabled = true;
                                            } else {
                                                conferenceFilter.enabled = !retainList.isEmpty();
                                            }

                                            if (!conferenceFilter.enabled) {
                                                conferenceFilter.isSelected = conferenceFilter.enabled;
                                            }
                                        })
                                        .map(strings -> conferenceFilter)))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(filter1 -> {
                        }, Throwable::printStackTrace, () -> {

                            for (Filter filter : getFilterConferences()) {
                                if (selectedConferencesList.contains(filter.id) && !filter.isSelected) {
                                    selectedConferencesList.remove(new Integer(filter.id));
                                } else if (!selectedConferencesList.contains(filter.id) && filter.isSelected) {
                                    selectedConferencesList.add(filter.id);
                                }

                                conferenceTitle.setSelected(selectedConferencesList.size() == conferenceSize);
                            }
                            notifyDataSetChanged();

                            ApplicationController.getBus().post(new FilterArchiveConferencesEvent());
                        }));

    }

    private void changeAllToCheck(Title title) {

        if (title.type == Type.CONFERENCE) {
            for (Filter filter : getFilterConferences()) {
                if (filter.enabled) {
                    filter.setSelected(true);
                    if (!selectedConferencesList.contains(filter.id)) {
                        selectedConferencesList.add(filter.id);
                    }
                }
            }

            greyFileFilters();
        } else if (title.type == Type.FILE_TYPE) {
            for (Filter filter : getFilterFileTypes()) {
                if (filter.enabled) {
                    filter.setSelected(true);
                    if (!selectedFileTypesList.contains(filter.id)) {
                        selectedFileTypesList.add(filter.id);
                    }
                }
            }

            greyConferenceFilters();
        }
    }

    public List<Filter> getFilters() {
        List<Filter> filterList = new ArrayList<>();
        for (Object o : objectList) {
            if (o instanceof Filter) {
                filterList.add((Filter) o);
            }
        }

        return filterList;
    }

    /**
     * Call when all items from dayNumber section should mark or not
     *
     * @param title
     */
    private void selectItems(Title title) {
        for (Filter filter : getFilters()) {
            if (filter.type == title.type) {
                filter.isSelected = title.isSelected;
                if (filter.type == Type.CONFERENCE && !selectedConferencesList.contains(filter.id)) {
                    selectedConferencesList.add(filter.id);
                } else if (filter.type == Type.FILE_TYPE && !selectedFileTypesList.contains(filter.id)) {
                    selectedFileTypesList.add(filter.id);
                }
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Set all items from all sections
     */
    @Override
    public void removeFilter() {
        for (Object o : objectList) {

            if (o instanceof Filter) {
                ((Filter) o).enabled = true;
            }

            if (o instanceof Title) {
                ((Title) o).isSelected = true;

                selectItems((Title) o);
            }
        }
    }

    /**
     * Get selected days
     *
     * @return
     */
    public List<Conference> getSelectedConferences() {

        List<Conference> conferenceList = new ArrayList<>();
        for (Object o : objectList) {
            if (o instanceof Filter && ((Filter) o).type == Type.CONFERENCE && ((Filter) o).isSelected) {
                conferenceList.add((Conference) ((Filter) o).tag);
            }
        }
        return conferenceList;
    }

    /**
     * Get selected days
     *
     * @return
     */
    public List<Filter> getFilterConferences() {

        List<Filter> conferenceList = new ArrayList<>();
        for (Object o : objectList) {
            if (o instanceof Filter && ((Filter) o).type == Type.CONFERENCE) {
                conferenceList.add((Filter) o);
            }
        }
        return conferenceList;
    }

    public List<Filter> getFilterFileTypes() {

        List<Filter> filterFileTypes = new ArrayList<>();
        for (Object o : objectList) {
            if (o instanceof Filter && ((Filter) o).type == Type.FILE_TYPE) {
                filterFileTypes.add((Filter) o);
            }
        }
        return filterFileTypes;
    }

    public List<Integer> getFilterFileTypesId() {

        List<Integer> filterFileTypes = new ArrayList<>();
        for (Object o : objectList) {
            if (o instanceof Filter && ((Filter) o).type == Type.FILE_TYPE && ((Filter) o).isSelected) {
                filterFileTypes.add(((Filter) o).id);
            }
        }
        return filterFileTypes;
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
        } else if (((Filter) objectList.get(position)).type == Type.CONFERENCE) {
            return FILTER_CONFERENCE_HOLDER;
        } else {
            return FILTER_FILE_TYPE_HOLDER;
        }
    }

    @Override
    public void saveFilter() {

        if (selectedConferencesList.isEmpty() || selectedConferencesList.size() == conferenceSize) {
            Utils.clearConferencesFilter();
        } else {
            Utils.saveConferencesFilter((ArrayList<Integer>) selectedConferencesList);
        }

        if (selectedFileTypesList.isEmpty() || selectedFileTypesList.size() == fileTypesSize) {
            Utils.clearArchiveFileTypesFilter();
        } else {
            Utils.saveArchiveFileTypesFilter((ArrayList<Integer>) selectedFileTypesList);
        }

        compositeSubscription.clear();
        compositeSubscription.unsubscribe();
    }

    @Override
    public Object getItem(int position) {
        return objectList.get(position);
    }

}
