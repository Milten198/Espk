package com.pgssoft.testwarez.feature.speaker.list;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.event.OpenEventEvent;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.util.DatabaseUtils;
import com.pgssoft.testwarez.util.Utils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by rtulaza on 2015-08-12.
 */
public class SpeakerAgendaListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Event> events;
    private ArrayList<ArrayList<Speaker>> speakers;
    private ArrayList<Event> savedEvents;
    private boolean isFavorite = false;

    private Event clickedFavoriteEvent;
    private ImageView clickedFavoriteImageView;

    private CompositeSubscription compositeSubscription;

    public SpeakerAgendaListAdapter(Context context, ArrayList<Event> events, ArrayList<ArrayList<Speaker>> speakers, boolean isFavorite) {
        this.context = context;
        this.events = events;
        this.speakers = speakers;
        this.isFavorite = isFavorite;

        this.savedEvents = new ArrayList<>(events);
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Event getItem(int i) {
        return events.get(i);
    }

    public ArrayList<Speaker> getItemSpeakers(int i) {
        return speakers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder vh;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_speaker_agenda_event, viewGroup, false);

            vh = new ViewHolder();
            vh.startTime = ButterKnife.findById(view, R.id.start_time);
            vh.endTime = ButterKnife.findById(view, R.id.end_time);
            vh.title = ButterKnife.findById(view, R.id.title);
            vh.favorite = ButterKnife.findById(view, R.id.favorite);
            vh.trackIndicator = ButterKnife.findById(view, R.id.vEventListItemTrackIndicator);
            vh.place = ButterKnife.findById(view, R.id.tvEventListItemPlace);
            vh.day = ButterKnife.findById(view, R.id.tvSELItemDay);

            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }

        final Event item = getItem(i);

        Drawable leftDrawable = VectorDrawableCompat.
                create(context.getResources(), R.drawable.ic_place_black_24dp, null);
        vh.place.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null);
        vh.startTime.setText(item.getStartAt().toString("HH:mm"));
        vh.endTime.setText(String.format("- %s", item.getEndAt().toString("HH:mm")));
        vh.title.setText(Html.fromHtml(item.getTitle()));

        vh.day.setText(item.getStartAt().toString("EEEE d MMMM", context.getResources().getConfiguration().locale));

        Utils.divideIndicatorByTrack(context, item, vh.trackIndicator);

        compositeSubscription.add(Observable.defer(() -> Observable.just(DatabaseUtils.isFavorite(item)))
                .doOnError(Throwable::printStackTrace)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isFavorite -> {
                    if (isFavorite) {
                        vh.favorite.setImageDrawable(Utils.getDrawableFilter(context, R.drawable.calendar, R.color.primary));
                    } else {
                        vh.favorite.setImageDrawable(Utils.getDrawable(context, R.drawable.calendar_grey));
                    }
                }));

        final ImageView tempFavorite = vh.favorite;
        vh.favorite.setOnClickListener(v -> changeEventSignState(item, tempFavorite));

        if (item.getPlace() != null) {
            vh.place.setVisibility(View.VISIBLE);
            vh.place.setText(Html.fromHtml(item.getPlace().getName()));
        } else {
            vh.place.setVisibility(View.GONE);
        }

        view.setOnClickListener(view1 -> {
            OpenEventEvent event = new OpenEventEvent(item);
            event.setTitle(vh.title);
            event.setTrack(vh.trackIndicator);
            ApplicationController.getBus().post(event);
        });

        return view;
    }

    private void changeEventSignState(Event event, ImageView favorite) {

        if (Utils.isCalendarSync() && !((BaseActivity) context).calendarPermissionGranted(true)) {
            clickedFavoriteImageView = favorite;
            clickedFavoriteEvent = event;
            return;
        }

        if (event != null) {
            Utils.signInOutFavorite(context, event, favorite, compositeSubscription, false);
        }
    }

    public void onCalendarPermissionGranted() {
        changeEventSignState(clickedFavoriteEvent, clickedFavoriteImageView);
    }


    public void setCompositeSubscription(CompositeSubscription compositeSubscription) {
        this.compositeSubscription = compositeSubscription;
    }

    class ViewHolder {
        TextView startTime;
        TextView endTime;
        TextView title;
        TextView speaker;
        ImageView favorite;
        LinearLayout trackIndicator;
        TextView place;
        TextView day;
    }

}
