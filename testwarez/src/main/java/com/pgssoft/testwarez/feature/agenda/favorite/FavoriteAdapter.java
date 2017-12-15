package com.pgssoft.testwarez.feature.agenda.favorite;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
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
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.feature.event.EventActivity;
import com.pgssoft.testwarez.event.CalendarRequestPermissionEvent;
import com.pgssoft.testwarez.event.FavoriteEmptyListEvent;
import com.pgssoft.testwarez.event.FavoriteRefreshEvent;
import com.pgssoft.testwarez.event.OpenEventEvent;
import com.pgssoft.testwarez.event.SearchEvent;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpodolak on 26.04.16.
 */
public class FavoriteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private Context context;

    private List<FavoriteItem> presentationList = new ArrayList<>();
    private List<Integer> daysList = new ArrayList<>();

    private ImageView clickedFavoriteImageView;
    private Event clickedFavoriteEvent;
    private DisplayImageOptions options;

    private LayoutInflater inflater;

    public FavoriteAdapter(Context context) {
        this.context = context;

        inflater = LayoutInflater.from(context);

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.user_placeholder)
                .showImageOnFail(R.drawable.user_placeholder)
                .showImageOnLoading(R.drawable.user_placeholder)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        loadFavorites(null);
        ApplicationController.getBus().register(this);
    }

    private void loadFavorites(Func1<Event, Boolean> filter) {

        final Observable<Event> eventObservable;

        if (filter != null) {
            eventObservable = Observables.getInstance().getFavoriteEventObservable().filter(filter);
        } else {
            eventObservable = Observables.getInstance().getFavoriteEventObservable();
        }

        daysList.clear();

        compositeSubscription.add(
                eventObservable
                        .filter(event -> {
                            if (!daysList.contains(event.getStartAt().getDayOfYear())) {
                                daysList.add(event.getStartAt().getDayOfYear());
                                return true;
                            }
                            return false;
                        }).map(event -> new FavoriteItem(event, FavoriteItem.HEADER_TYPE))
                        .toSortedList((favoriteItem, favoriteItem2) -> new Long(favoriteItem.event.getStartAt().getMillis()).compareTo(favoriteItem2.event.getStartAt().getMillis()))
                        .flatMap(Observable::from)
                        .flatMap(favoriteItem1 -> {
                            Observable<FavoriteItem> dayEventObservable = eventObservable
                                    .filter(event -> event.getStartAt().getDayOfYear() == favoriteItem1.event.getStartAt().getDayOfYear())
                                    .map(FavoriteItem::new);

                            return Observable.merge(Observable.just(favoriteItem1), dayEventObservable);
                        })
                        .toList()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(favorites -> {

                            presentationList.clear();
                            presentationList.addAll(favorites);
                            ApplicationController.getBus().post(new FavoriteEmptyListEvent(favorites.isEmpty()));

                        }, Throwable::printStackTrace, this::notifyDataSetChanged));
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case FavoriteItem.HEADER_TYPE:
                return new HeaderViewHolder(inflater.inflate(R.layout.item_favorite_header, parent, false));
            case FavoriteItem.EVENT_TYPE:
                return new FavoriteViewHolder(inflater.inflate(R.layout.item_agenda_event, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case FavoriteItem.HEADER_TYPE:

                ((HeaderViewHolder) holder).bind(context, presentationList.get(position).event);
                break;
            case FavoriteItem.EVENT_TYPE:
                ((FavoriteViewHolder) holder).bind(context, presentationList.get(position).event);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return presentationList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return presentationList.get(position).type;
    }

    public void onDestroy() {
        ApplicationController.getBus().unregister(this);
        if (compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }

    @Subscribe
    public void calendarPermissionGranted(CalendarRequestPermissionEvent event) {
        changeEventSignState(clickedFavoriteEvent, clickedFavoriteImageView);
    }

    @Subscribe
    public void onFavoriteRefreshEvent(FavoriteRefreshEvent event) {
        loadFavorites(null);
    }

    @Subscribe
    public void onSearchEvent(SearchEvent event) {
        if (event.type != SearchEvent.SEARCH_TYPE.FAVORITE) {
            return;
        }

        loadFavorites(event.query.isEmpty() ? null : new SearchFilter(event.query));
    }

    private void changeEventSignState(Event item, ImageView favorite) {

        if (Utils.isCalendarSync() && !((BaseActivity) context).calendarPermissionGranted(true)) {
            clickedFavoriteImageView = favorite;
            clickedFavoriteEvent = item;
            return;
        }

        if (item != null) {
            Utils.signInOutFavorite(favorite.getContext(), item, favorite, compositeSubscription, false);
        }
    }

    public void updateView() {
        onFavoriteRefreshEvent(null);
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tvItemFavoriteHeaderTitle)
        protected TextView title;

        public HeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(Context context, Event category) {
            title.setText(category.getStartAt().toString("EEEE d MMMM", context.getResources().getConfiguration().locale).toUpperCase());
        }
    }

    public class FavoriteViewHolder extends RecyclerView.ViewHolder {

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
        protected ImageView favoriteButton;

        @Bind(R.id.tvEventListItemPlace)
        protected TextView placeTextView;

        @Bind(R.id.tvItemAgendaEventContinous)
        protected TextView continousTextView;

        private final LayoutInflater inflater;

        private int speakerOffset;

        public FavoriteViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            speakerOffset = (int) view.getContext().getResources().getDimension(R.dimen.small_offset);
        }

        public void bind(Context context, Event event) {


            if (event != null) {
                //set track indicator for event
                trackIndicator.removeAllViews();
                Utils.divideIndicatorByTrack(context, event, trackIndicator);

                startTime.setText(event.getStartAt().toString("HH:mm"));
                endTime.setText("- " + event.getEndAt().toString("HH:mm"));
                title.setText(event.getDescriptions().size() > 0 ? Html.fromHtml(event.getDescriptions().get(0).getTitle()) : "");

                DateTime current = new DateTime();
                if (current.isAfter(event.getStartAt()) && current.isBefore(event.getEndAt())) {
                    continousTextView.setVisibility(View.VISIBLE);
                } else {
                    continousTextView.setVisibility(View.GONE);
                }
                favoriteButton.setOnClickListener(v -> changeEventSignState(event, favoriteButton));

                itemView.setOnClickListener(v -> {
                    OpenEventEvent openEvent = new OpenEventEvent(event);
                    openEvent.setTrack(trackIndicator);
                    openEvent.setTitle(title);

                    EventActivity.open((Activity) context, openEvent);
                    ApplicationController.getBus().post(event);
                });

                if (event.getPlace() != null && !event.getPlace().getName().isEmpty()) {
                    placeTextView.setVisibility(View.VISIBLE);
                    Drawable leftDrawable = VectorDrawableCompat
                            .create(context.getResources(), R.drawable.ic_place_black_24dp, null);
                    placeTextView.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null);
                    placeTextView.setText(Html.fromHtml(event.getPlace().getName()));
                } else {
                    placeTextView.setVisibility(View.GONE);
                }

                title.setText(Html.fromHtml(event.getTitle()));
                title.setTextColor(Color.parseColor("#000000"));

                speakerContainer.removeAllViews();

                List<Speaker> speakerList = event.getAllSpeakers();

                for (int i = 0; i < speakerList.size(); i++) {

                    View view = inflater.inflate(R.layout.item_agenda_event_list_speaker, null);


                    TextView speaker = ButterKnife.findById(view, R.id.tvItemAgendaEventList);
                    CircleImageView speakerPhoto = ButterKnife.findById(view, R.id.civItemAgendaEventList);
                    speaker.setText(Html.fromHtml(speakerList.get(i).getFullName()));

                    int id = -1;

                    if (speakerList.get(i).getPhoto() != null) {
                        id = speakerList.get(i).getPhoto().getFileId();
                    }

                    ImageLoader.getInstance().displayImage(context.getResources().getString(R.string.endpoint) +
                            context.getResources().getString(R.string.images_url_default, id), speakerPhoto, options);

                    speakerContainer.addView(view);

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
                    if (i > 0) {
                        params.topMargin = speakerOffset;
                    }
                    params.rightMargin = speakerOffset;

                    view.setLayoutParams(params);
                }

                compositeSubscription.add(Observable.defer(() -> Observable.just(DatabaseUtils.isFavorite(event)))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(isFavorite -> {
                            if (isFavorite) {
                                favoriteButton.setImageDrawable(Utils.getDrawableFilter(context, R.drawable.calendar, R.color.primary));
                            } else {
                                favoriteButton.setImageDrawable(Utils.getDrawable(context, R.drawable.calendar_grey));
                            }
                        }, Throwable::printStackTrace));
            }
        }

    }

    private class SearchFilter implements Func1<Event, Boolean> {

        private String query;

        public SearchFilter(String query) {
            this.query = query;
        }

        @Override
        public Boolean call(Event event) {
            return event.getTitle().toLowerCase().contains(query.toLowerCase());
        }
    }

    private class FavoriteItem {

        public static final int EVENT_TYPE = 300;
        public static final int HEADER_TYPE = 301;

        private Event event;

        private int type;

        public FavoriteItem(Event event) {
            this.event = event;
            type = EVENT_TYPE;
        }

        public FavoriteItem(Event event, int type) {
            this.event = event;
            this.type = type;
        }

    }
}
