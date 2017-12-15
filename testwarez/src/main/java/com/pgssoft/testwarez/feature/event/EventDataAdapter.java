package com.pgssoft.testwarez.feature.event;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.feature.plan.ShowFloorActivity;
import com.pgssoft.testwarez.feature.speaker.list.SpeakerListAdapter;
import com.pgssoft.testwarez.feature.speaker.list.StaffListAdapter;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.database.model.Staff;
import com.pgssoft.testwarez.util.ReadMoreLayoutListener;
import com.pgssoft.testwarez.widget.ratingbar.BaseRatingBar;

import org.joda.time.DateTime;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by rtulaza on 2015-09-10.
 */
public class EventDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Event event;
    private ArrayList<Speaker> speakers;
    private ArrayList<Staff> staff;
    private Context context;
    private Point screenSize;

    private SpeakerListAdapter speakerListAdapter;
    private StaffListAdapter staffListAdapter;

    public EventDataAdapter(Context context, Event event, ArrayList<Speaker> speakers, ArrayList<Staff> staff, Point screenSize) {
        this.context = context;
        this.event = event;
        this.speakers = speakers;
        this.staff = staff;
        this.screenSize = screenSize;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v;
        switch (i) {
            case 0:
                v = inflater.inflate(R.layout.rv_event_date_time, viewGroup, false);
                return new DateTimeViewHolder(v);
            case 1:
                v = inflater.inflate(R.layout.rv_event_description, viewGroup, false);
                return new DescriptionViewHolder(v);
            case 2:
                v = inflater.inflate(R.layout.rv_event_speakers, viewGroup, false);
                return new SpeakersViewHolder(v);
            case 3:
                v = inflater.inflate(R.layout.rv_event_map, viewGroup, false);
                return new MapViewHolder(v);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder s, int i) {
        switch (i) {
            case 0:
                DateTimeViewHolder dtvh = (DateTimeViewHolder) s;

                Conference conference = ApplicationController.getActiveConference();
                int dayTime = event.getStartAt().dayOfYear().get() - conference.getStartAt().dayOfYear().get() + 1;

                String header = context.getResources().getString(R.string.day, dayTime, event.getCategory().getStartAt().toString("EEEE d MMMM", context.getResources().getConfiguration().locale));

                dtvh.dateTime.setText(String.format("%s %s,", header, event.getFormattedTime()));
                if (event.getPlace() != null) {
                    dtvh.place.setText(Html.fromHtml(event.getPlace().getFullName()));
                } else {
                    dtvh.place.setVisibility(View.GONE);
                    dtvh.locationImage.setVisibility(View.GONE);
                }

                return;
            case 1:
                DescriptionViewHolder dvh = (DescriptionViewHolder) s;
                if (event.getDescriptions().size() > 0) {
                    dvh.container.setVisibility(View.VISIBLE);
                    dvh.description.setText(Html.fromHtml(event.getDescription()));
                    dvh.description.setMovementMethod(LinkMovementMethod.getInstance());
                    dvh.description.setLinksClickable(true);

                    if (event.isTechnical()) {
                        dvh.title.setText(context.getString(R.string.technical_place));
                    }

                } else {
                    dvh.container.setVisibility(View.GONE);

                }

                dvh.container.getViewTreeObserver().addOnGlobalLayoutListener(new ReadMoreLayoutListener(context, dvh.readMore, dvh.readMoreIcon, dvh.readMoreText, dvh.description, screenSize));

                dvh.readMoreIcon.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.primary), PorterDuff.Mode.MULTIPLY));

                return;
            case 2:
                final SpeakersViewHolder svh = (SpeakersViewHolder) s;
                if (!event.isTechnical()) {
                    if (speakers != null) {
                        if (speakers.size() > 0) {
                            svh.container.setVisibility(View.VISIBLE);

                            speakerListAdapter = new SpeakerListAdapter(context, speakers, false);

                            svh.speakersList.removeAllViews();

                            for (int j = 0; j < speakerListAdapter.getCount(); j++) {
                                svh.speakersList.addView(speakerListAdapter.getView(j, null, svh.speakersList));
                            }
                        } else {
                            svh.container.setVisibility(View.GONE);
                            ViewGroup.LayoutParams params = svh.itemView.getLayoutParams();
                            params.height = 0;
                            svh.itemView.setLayoutParams(params);
                        }
                    } else {
                        svh.container.setVisibility(View.GONE);
                    }
                } else {
                    if (staff != null) {
                        if (staff.size() > 0) {
                            svh.title.setText(context.getString(R.string.technical_person));
                            svh.container.setVisibility(View.VISIBLE);

                            staffListAdapter = new StaffListAdapter(context, staff);

                            svh.speakersList.removeAllViews();

                            for (int j = 0; j < staffListAdapter.getCount(); j++) {
                                svh.speakersList.addView(staffListAdapter.getView(j, null, svh.speakersList));
                            }
                        } else {
                            svh.container.setVisibility(View.GONE);
                        }
                    } else {
                        svh.container.setVisibility(View.GONE);
                    }
                }

                if (event.isArchival() || event.isTechnical()) {
                    svh.rateContainer.setVisibility(View.GONE);
                    return;
                }

                DateTime current = new DateTime();

                if (current.isAfter(event.getEndAt())) {
                    svh.rateButton.setVisibility(View.VISIBLE);
                    svh.rateButtonStars.setVisibility(View.VISIBLE);
                    svh.rateMessageBefore.setVisibility(View.GONE);
                    svh.rateInfoContainer.setVisibility(View.VISIBLE);
                    svh.rateButton.setEnabled(true);
                    svh.rateButton.setBackground(ContextCompat.getDrawable(context, R.drawable.rate_button_background));
                    svh.rateButton.setTextColor(ContextCompat.getColor(context, android.R.color.white));

                    svh.ratingBar.setRating(event.getAverageRating());

                    if (event.getAverageRating() == 0.0f) {
                        svh.rateInfoText.setText(context.getResources().getString(R.string.no_rating));
                    } else {
                        float rating = round(event.getAverageRating());
                        String rate = rating % 1.0f == 0 ? String.valueOf((int) rating) : String.valueOf(rating);
                        SpannableString spannableString = new SpannableString(rate + " " + context.getResources().getString(R.string.rate));
                        spannableString.setSpan(new RelativeSizeSpan(1.5f), 0, rate.length(), 0);
                        svh.rateInfoText.setText(spannableString);
                    }

                    if (event.isRated()) {
                        svh.rateMessageAfter.setVisibility(View.VISIBLE);
                        svh.rateButtonStars.setVisibility(View.GONE);
                        svh.rateButton.setVisibility(View.GONE);
                    } else {
                        svh.rateMessageAfter.setVisibility(View.GONE);
                        svh.rateButtonStars.setVisibility(View.VISIBLE);
                        svh.rateButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    svh.rateMessageBefore.setVisibility(View.VISIBLE);
                    svh.rateInfoContainer.setVisibility(View.GONE);
                    svh.rateButton.setVisibility(View.VISIBLE);
                    svh.rateButtonStars.setVisibility(View.VISIBLE);
                    svh.rateButton.setEnabled(false);
                    svh.rateButton.setBackground(ContextCompat.getDrawable(context, R.drawable.rate_button_background_disabled));
                    svh.rateButton.setTextColor(ContextCompat.getColor(context, R.color.button_text_disabled));
                }

                return;
            case 3:
                final MapViewHolder mvh = (MapViewHolder) s;

                if (event.getPlace() != null && event.getPlace().getPlace() != null) {
                    mvh.container.setVisibility(View.VISIBLE);
                    mvh.container.setOnClickListener(v -> {
                        ShowFloorActivity.openActivity((EventActivity) context, mvh.map, event.getPlace(), event.getPlace().getFullName(), false);
                    });

                    mvh.title.setText(Html.fromHtml(event.getPlace().getName()));

                    String description = event.getPlace().getBuildingPlan().getName();
                    if (description != null && !description.isEmpty()) {
                        mvh.description.setVisibility(View.VISIBLE);
                        mvh.description.setText(Html.fromHtml(description));
                        mvh.description.setMovementMethod(LinkMovementMethod.getInstance());
                        mvh.description.setLinksClickable(true);
                    }

                    ImageLoader.getInstance().displayImage(context.getResources().getString(R.string.endpoint) +
                            context.getResources().getString(R.string.images_url_default, event.getPlace().getPlace().getFileId()), mvh.map);
                } else {
                    mvh.container.setVisibility(View.GONE);
                    mvh.container.setOnClickListener(null);
                }

                break;
        }
    }

    private float round (double value) {
        int scale = (int) Math.pow(10, 1);
        return (float) Math.round(value * scale) / scale;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    public class DateTimeViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tvRVEventDatetime)
        protected TextView dateTime;

        @Bind(R.id.tvRVEventDatetimePlace)
        protected TextView place;

        @Bind(R.id.ivRVEventDatetimePlace)
        protected ImageView locationImage;

        public DateTimeViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public class DescriptionViewHolder extends RecyclerView.ViewHolder {

        protected View container;
        @Bind(R.id.description)
        protected TextView description;
        @Bind(R.id.read_more)
        protected View readMore;
        @Bind(R.id.read_more_icon)
        protected ImageView readMoreIcon;
        @Bind(R.id.read_more_text)
        protected TextView readMoreText;
        @Bind(R.id.tvRVEventDescriptionTitle)
        protected TextView title;

        public DescriptionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
            view.findViewById(R.id.speakerDescriptionHelpView).setOnClickListener(v -> {
            });
        }
    }

    public class SpeakersViewHolder extends RecyclerView.ViewHolder {

        public View container;
        @Bind(R.id.speakers_list)
        protected LinearLayout speakersList;

        @Bind(R.id.tvRVEventSpeakersTitle)
        protected TextView title;

        @Bind(R.id.rate_container)
        protected FrameLayout rateContainer;

        @Bind(R.id.rate_button)
        protected Button rateButton;

        @Bind(R.id.rate_button_stars)
        protected LinearLayout rateButtonStars;

        @Bind(R.id.rate_message_before)
        protected TextView rateMessageBefore;

        @Bind(R.id.rate_message_after)
        protected TextView rateMessageAfter;

        @Bind(R.id.rate_info_container)
        protected FrameLayout rateInfoContainer;

        @Bind(R.id.rate_info_text)
        protected TextView rateInfoText;

        @Bind(R.id.rating_bar)
        protected BaseRatingBar ratingBar;


        public SpeakersViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
            container = view;

            ratingBar.setNumStars(5);
            ratingBar.setStarPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, view.getContext().getResources().getDisplayMetrics()));
            ratingBar.setTouchable(false);
        }

        @OnClick(R.id.rate_button)
        protected void onClick(View view) {
            ((AppCompatActivity) context).startActivityForResult(new Intent(context, EventRateActivity.class).putExtra("eventId", event.getId()), EventActivity.RATE_REQUEST);
        }
    }

    public class MapViewHolder extends RecyclerView.ViewHolder {

        public View container;

        @Bind(R.id.map)
        protected ImageView map;

        @Bind(R.id.tvRVEventMapTitle)
        protected TextView title;

        @Bind(R.id.tvRVEventMapDescription)
        protected TextView description;


        public MapViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
            container = view;
        }
    }
}
