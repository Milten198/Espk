package com.pgssoft.testwarez.feature.agenda.all;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.ExpandableRecyclerAdapter;
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.feature.agenda.filter.SearchFilter;
import com.pgssoft.testwarez.feature.agenda.filter.UserFilter;
import com.pgssoft.testwarez.feature.event.EventActivity;
import com.pgssoft.testwarez.event.AgendaScrollEvent;
import com.pgssoft.testwarez.event.CalendarRequestPermissionEvent;
import com.pgssoft.testwarez.event.FavoriteRefreshEvent;
import com.pgssoft.testwarez.event.LoadAgendaEvent;
import com.pgssoft.testwarez.event.OpenEventEvent;
import com.pgssoft.testwarez.event.SearchEvent;
import com.pgssoft.testwarez.database.model.Category;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.util.DatabaseUtils;
import com.pgssoft.testwarez.util.Observables;
import com.pgssoft.testwarez.util.Utils;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpodolak on 21.04.16.
 */
public class AgendaAdapter extends ExpandableRecyclerAdapter<AgendaAdapter.ListItem> implements ExpandableRecyclerAdapter.ExpandItemsListener {

    private final CompositeSubscription compositeSubscription = new CompositeSubscription();
    private boolean expandAll;

    Func1<List<Category>, Observable<Category>> dayFlatMap = categories -> {
        List<Integer> day = new ArrayList<>();
        return Observable.from(categories)
                .filter(category -> {
                    DateTime currentStartAt = category.getStartAt();
                    return !day.contains(new Integer(currentStartAt.getDayOfYear()));
                }).doOnNext(category -> day.add(category.getStartAt().getDayOfYear()));
    };


    private Observable<ListItem> allObjectsObservable;
    private List<Integer> daysOfYearList = new ArrayList<>();

    private Event clickedFavoriteEvent;
    private ImageView clickedFavoriteImageView;

    boolean isSearch = false;
    boolean restore = true;

    Context context;

    public AgendaAdapter(Context context) {
        this(context, null, false);
    }

    public AgendaAdapter(Context context, Func1<ListItem, Boolean> filter, boolean expandAll) {
        super(context);

        setExpandItemsListener(this);

        ApplicationController.getBus().register(this);

        this.context = context;
        this.expandAll = expandAll;

        initAllObservable();

        showList(filter);
    }

    private void initAllObservable() {
        Observable<ListItem> dayObservable = Observable.defer(() -> Observable.just(ApplicationController.getActiveConference()))
                .flatMap(conference ->
                        Observables.getInstance().getCategoryObservable()
                                .toList()
                                .flatMap(dayFlatMap)
                                .map(category -> {
                                    daysOfYearList.add(category.getStartAt().getDayOfYear());
                                    int displayDay = category.getStartAt().dayOfYear().get() - conference.getStartAt().dayOfYear().get() + 1;
                                    String title = context.getResources().getString(R.string.day, displayDay, category.getStartAt().toString("EEEE d MMMM", mContext.getResources().getConfiguration().locale));
                                    return new ListItem(title);
                                }));

        allObjectsObservable = Observable.zip(dayObservable, Observables.getInstance().getAgendaElements(), (listItem, itemsList) -> {
            List<ListItem> list = new ArrayList();
            list.add(listItem);
            list.addAll(itemsList);
            return list;
        }).flatMap(Observable::from)
                .filter(listItems -> listItems.getCategory() != null ? !listItems.getCategory().isHidden() : true)
                .replay()
                .autoConnect();
    }

    public void showList(Func1<ListItem, Boolean> filter) {

        Observable<ListItem> presenterObservable;

        if (filter == null && shouldUserFilter()) {
            filter = new UserFilter(compositeSubscription, Observables.getInstance().getCategoryObservable());
        }

        if (filter != null) {
            if (isSearch) {
                Func1<ListItem, Boolean> filter2 = new UserFilter(compositeSubscription, Observables.getInstance().getCategoryObservable());
                presenterObservable = allObjectsObservable.filter(filter2).filter(filter);
            } else {
                presenterObservable = allObjectsObservable.filter(filter);
            }
            presenterObservable = addCategoryFilter(presenterObservable);
        } else {
            presenterObservable = allObjectsObservable;
        }

        final Func1<ListItem, Boolean> finalFilter = filter;

        Subscription subscription = presenterObservable
                .doOnError(Throwable::printStackTrace)
                .toList()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listItems -> {
                    if (finalFilter != null) {
                        daysOfYearList.clear();

                        for (ListItem listItem : listItems) {
                            if (listItem.ItemType == TYPE_ITEM_EVENT) {
                                if (!daysOfYearList.contains(listItem.getEvent().getStartAt().getDayOfYear())) {
                                    daysOfYearList.add(listItem.getEvent().getStartAt().getDayOfYear());
                                }
                            }
                        }
                    }

                    setItems(listItems);

                    if (finalFilter != null || isSearch) {
                        expandAll();
                    } else {
                        expandDay(daysOfYearList.indexOf(DateTime.now().getDayOfYear()));
                    }

                    isSearch = false;
                    restoreHeadersState();
                    scrollToCurrentEvent();
                    ApplicationController.getBus().post(new LoadAgendaEvent());
                });

        compositeSubscription.add(subscription);
    }

    private Observable<ListItem> addCategoryFilter(Observable<ListItem> presenterObservable) {
        presenterObservable = presenterObservable.toList()
                .flatMap(listItems ->
                        Observable.from(listItems)
                                .filter(listItem -> {
                                    int indexOf = listItems.indexOf(listItem);
                                    boolean listItemCategory = listItem.getCategory() != null;
                                    boolean listItemDay = listItem.getDay() != null;

                                    if (listItemDay && indexOf == listItems.size() - 1) {
                                        return false;
                                    }

                                    if ((indexOf + 1) < listItems.size()) {
                                        ListItem nextListItem = listItems.get(indexOf + 1);

                                        boolean nextItemEvent = nextListItem.getEvent() != null;
                                        boolean nextItemCategory = nextListItem.getCategory() != null;
                                        boolean nextItemDay = nextListItem.getDay() != null;

                                        if (listItemDay && nextItemDay) {
                                            return false;
                                        } else if (listItemDay) {
                                            if (nextItemCategory || nextItemEvent) {
                                                return true;
                                            }
                                        } else if (listItemCategory && !nextItemEvent) {
                                            return false;
                                        }
                                    } else if (listItemCategory && (indexOf + 1) == listItems.size()) {
                                        return false;
                                    }

                                    return true;
                                })
                );
        return presenterObservable;
    }

    public void resetAdapter() {
        showList(null);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(inflate(R.layout.item_archive_conference_header, parent));
            case TYPE_ITEM_CATEGORY:
                return new CategoryViewHolder(inflate(R.layout.event_list_category_header, parent));
            case TYPE_ITEM_EVENT:
                return new EventViewHolder(inflate(R.layout.item_agenda_event, parent));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ExpandableRecyclerAdapter.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER: {
                ((HeaderViewHolder) holder).bind(position);
            }
            break;
            case TYPE_ITEM_CATEGORY:
                ((CategoryViewHolder) holder).bind(position);
                break;
            case TYPE_ITEM_EVENT:
                ((EventViewHolder) holder).bind(position);
                break;
        }
    }

    public void onDestroy() {
        ApplicationController.getBus().unregister(this);
        compositeSubscription.clear();
    }

    private boolean shouldUserFilter() {
        boolean dayFilter = !Utils.getAgendaDayFilter().isEmpty();
        boolean trackFilter = !Utils.getAgendaTrackFilter().isEmpty();
        return dayFilter || trackFilter;
    }

    public void scrollToCurrentEvent() {
        for (int i = 0; i < visibleItems.size(); i++) {
            if (this.getItemViewType(i) == TYPE_ITEM_EVENT) {
                if (visibleItems.get(i).getEvent().getStartAt().isBefore(DateTime.now()) &&
                        visibleItems.get(i).getEvent().getEndAt().isAfter(DateTime.now())) {
                    ApplicationController.getBus().post(new AgendaScrollEvent(i));
                    break;
                }
            }

            if (this.getItemViewType(i) == TYPE_ITEM_CATEGORY) {
                if (visibleItems.get(i).getCategory().getStartAt().isBefore(DateTime.now()) &&
                        visibleItems.get(i).getCategory().getEndAt().isAfter(DateTime.now())) {
                    ApplicationController.getBus().post(new AgendaScrollEvent(i));
                    break;
                }
            }
        }
    }

    public void restoreHeadersState() {
        List<Boolean> headersStatus = Utils.getHeadersStatus();

        if (headersStatus.isEmpty()) {
            expandAll();
            return;
        }

        int day = headersStatus.size();

        for (int i = this.visibleItems.size() - 1; i >= 0; --i) {
            if (this.getItemViewType(i) == TYPE_HEADER) {
                if (--day >= 0 && day < headersStatus.size() && headersStatus.get(day))
                    if (!this.isExpanded(i)) {
                        this.expandItems(i, true);
                    }
            }
        }

        if (restore) {
            restoreScrollState();
            restore = false;
        }
    }

    public void restoreScrollState() {
        int position = Utils.getAgendaScrollPosition();

        if (position != 0) {
            new Handler().post(() -> ApplicationController.getBus().post(new AgendaScrollEvent(position)));
        }
    }

    @Override
    public void onExpandOrCollapse() {
        List<Boolean> headersStatus = getHeadersStatus();
        StringBuilder result = new StringBuilder();

        for (Boolean status : headersStatus) {
            result.append(String.valueOf(status));
            result.append(",");
        }

        Utils.saveHeadersStatus(result.toString());
    }

    public static class ListItem extends ExpandableRecyclerAdapter.ListItem {

        private String day;

        private Category category;

        private Event event;

        public ListItem(String day) {
            super(TYPE_HEADER);
            this.day = day;
        }

        public ListItem(Category category) {
            super(TYPE_ITEM_CATEGORY);
            this.category = category;
        }

        public ListItem(Event event) {
            super(TYPE_ITEM_EVENT);
            this.event = event;
        }

        public String getDay() {
            return day;
        }

        public Category getCategory() {
            return category;
        }

        public Event getEvent() {
            return event;
        }

        @Override
        public String toString() {
            if (day != null) {
                return " day: " + day + " ";
            } else if (category != null) {
                return " category: " + category.getTitle() + "";
            } else if (event != null) {
                return " event: " + event.getTitle() + "";
            } else {
                return "empty item";
            }

        }
    }

    public class HeaderViewHolder extends ExpandableRecyclerAdapter.HeaderViewHolder {

        @Bind(R.id.tvItemArchiveConferenceHeaderTitle)
        protected TextView title;

        public HeaderViewHolder(View view) {
            super(view, ButterKnife.findById(view, R.id.ivItemArchiveConferenceHeaderArrow));
            ButterKnife.bind(this, view);
        }

        @Override
        public void bind(int position) {
            super.bind(position);
            title.setText(visibleItems.get(position).day);
        }
    }

    public class CategoryViewHolder extends ExpandableRecyclerAdapter.ItemViewHolder {

        @Bind(R.id.title)
        protected TextView title;

        public CategoryViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(int position) {
            if (visibleItems.get(position).getCategory() != null) {

                Category item = visibleItems.get(position).getCategory();
                String categoryTitleString = String.format("%s %s - %s",
                        item.getTitle(),
                        item.getStartAt().toString("HH:mm"),
                        item.getEndAt().toString("HH:mm"));

                title.setText(categoryTitleString);
            }
        }
    }

    public class EventViewHolder extends ExpandableRecyclerAdapter.ItemViewHolder {

        @Bind(R.id.start_time)
        protected TextView startTime;

        @Bind(R.id.end_time)
        protected TextView endTime;

        @Bind(R.id.title)
        protected TextView title;

        @Bind(R.id.llEventListItemSpeakers)
        protected LinearLayout speakerContainer;

        @Bind(R.id.vEventListItemTrackIndicator)
        protected LinearLayout trackIndicator;

        @Bind(R.id.favorite)
        protected ImageView favorite;

        @Bind(R.id.tvEventListItemPlace)
        protected TextView placeTextView;

        @Bind(R.id.tvItemAgendaEventContinous)
        protected TextView continousTextView;

        private final LayoutInflater inflater;

        private int offset;

        private DisplayImageOptions options;

        public EventViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            offset = (int) view.getContext().getResources().getDimension(R.dimen.small_offset);

            options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .showImageForEmptyUri(R.drawable.user_placeholder)
                    .showImageOnFail(R.drawable.user_placeholder)
                    .showImageOnLoading(R.drawable.user_placeholder)
                    .build();

        }

        public void bind(int position) {
            Event event = visibleItems.get(position).getEvent();

            if (event != null) {
                //set track indicator for event
                trackIndicator.removeAllViews();
                Utils.divideIndicatorByTrack(mContext, event, trackIndicator);

                startTime.setText(event.getStartAt().toString("HH:mm"));
                endTime.setText("- " + event.getEndAt().toString("HH:mm"));

                DateTime current = new DateTime();
                if (current.isAfter(event.getStartAt()) && current.isBefore(event.getEndAt())) {
                    continousTextView.setVisibility(View.VISIBLE);
                } else {
                    continousTextView.setVisibility(View.GONE);
                }
                favorite.setOnClickListener(v -> changeEventSignState(event, favorite));

                itemView.setOnClickListener(v -> {
                    OpenEventEvent openEvent = new OpenEventEvent(event);
                    openEvent.setTrack(trackIndicator);
                    openEvent.setTitle(title);

                    EventActivity.open((Activity) mContext, openEvent);
                    ApplicationController.getBus().post(event);
                });


                title.setText(Html.fromHtml(event.getTitle()));
                title.setTextColor(Color.parseColor("#000000"));

                speakerContainer.removeAllViews();

                List<Speaker> speakerList = event.getAllSpeakers();

                for (int i = 0; i < speakerList.size(); i++) {

                    View view = inflater.inflate(R.layout.item_agenda_event_list_speaker, null);

                    TextView speaker = ButterKnife.findById(view, R.id.tvItemAgendaEventList);
                    CircleImageView speakerPhoto = ButterKnife.findById(view, R.id.civItemAgendaEventList);
                    speaker.setText(Html.fromHtml(speakerList.get(i).getFullName()));
                    speakerPhoto.setVisibility(View.VISIBLE);

                    int id = -1;

                    if (speakerList.get(i).getPhoto() != null) {
                        id = speakerList.get(i).getPhoto().getFileId();
                    }

                    ImageLoader.getInstance().displayImage(mContext.getResources().getString(R.string.endpoint) +
                            mContext.getResources().getString(R.string.images_url_default, id), speakerPhoto, options);

                    speakerContainer.addView(view);

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
                    if (i > 0) {
                        params.topMargin = offset;
                    }
                    params.rightMargin = offset;

                    view.setLayoutParams(params);
                }

                if (event.getPlace() != null && !event.getPlace().getName().isEmpty()) {
                    placeTextView.setVisibility(View.VISIBLE);
                    placeTextView.setText(Html.fromHtml(event.getPlace().getName()));
                    Drawable leftDrawable = VectorDrawableCompat
                            .create(context.getResources(), R.drawable.ic_place_black_24dp, null);
                    placeTextView.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null);
                } else {
                    placeTextView.setVisibility(View.GONE);
                }

                compositeSubscription.add(Observable.defer(() -> Observable.just(DatabaseUtils.isFavorite(event)))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(isFavorite -> {
                            if (isFavorite) {
                                favorite.setImageDrawable(Utils.getDrawableFilter(mContext, R.drawable.calendar, R.color.primary));
                            } else {
                                favorite.setImageDrawable(Utils.getDrawable(mContext, R.drawable.calendar_grey));
                            }
                        }, Throwable::printStackTrace));

            }
        }

    }

    private void changeEventSignState(Event item, ImageView favorite) {

        if (Utils.isCalendarSync() && !((BaseActivity) mContext).calendarPermissionGranted(true)) {
            clickedFavoriteImageView = favorite;
            clickedFavoriteEvent = item;
            return;
        }

        if (item != null) {
            Utils.signInOutFavorite(mContext, item, favorite, compositeSubscription, false);


        }
    }

    public static class DayWrapper {
        int day;

        List<Category> categories;

        public DayWrapper(int day) {
            this.day = day;
            this.categories = new ArrayList<>();
        }

        public void setCategories(List<Category> categories) {
            this.categories = categories;
        }

        @Override
        public String toString() {
            return "day: " + day + ", count: " + String.valueOf(categories);
        }

        public int getDay() {
            return day;
        }

        public List<Category> getCategories() {
            return categories;
        }
    }

    @Subscribe
    public void onFavoriteRefreshEvent(FavoriteRefreshEvent event) {

        compositeSubscription.add(Observable.from(allItems)
                .flatMap(listItem -> Observable.just(listItem)
                        .filter(li -> li.getEvent() != null && li.getEvent().getId() == event.getEvent().getId())
                        .map(ev -> allItems.indexOf(listItem)))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(position -> notifyItemChanged(position, false), Throwable::printStackTrace));
    }

    public void setUserFilter() {
        allObjectsObservable = null;
        initAllObservable();
        notifyDataSetChanged();
        showList(null);
    }

    @Subscribe
    public void calendarPermissionGranted(CalendarRequestPermissionEvent event) {
        changeEventSignState(clickedFavoriteEvent, clickedFavoriteImageView);
        notifyDataSetChanged();
    }

    @Subscribe
    public void search(SearchEvent event) {
        if (event.type == SearchEvent.SEARCH_TYPE.ALL) {
            visibleItems.clear();
            notifyDataSetChanged();
            isSearch = true;
            showList(event.query.isEmpty() ? null : new SearchFilter(event.query));
        }
    }
}
