package com.pgssoft.testwarez.core;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.stmt.QueryBuilder;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.database.model.Archive;
import com.pgssoft.testwarez.database.model.Category;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Message;
import com.pgssoft.testwarez.database.model.Track;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by dpodolak on 08.03.16.
 */
public abstract class BaseFilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public static final int TITLE_HOLDER = 1;
    public static final int FILTER_DAY_HOLDER = 2;
    public static final int FILTER_TRACK_HOLDER = 3;
    public static final int FILTER_SOCIAL_HOLDER = 4;
    public static final int FILTER_CONFERENCE_HOLDER = 5;
    public static final int FILTER_FILE_TYPE_HOLDER = 6;

    private Context context;

    private LayoutInflater inflater;

    public enum Type {DAY, TRACK, SOCIAL, CONFERENCE, FILE_TYPE}

    public BaseFilterAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TITLE_HOLDER:
                return new TitleViewHolder(inflater.inflate(R.layout.item_filter_title, parent, false));
            case FILTER_DAY_HOLDER:
                return new FilterDayViewHolder(inflater.inflate(R.layout.item_filter_day, parent, false));
            case FILTER_TRACK_HOLDER:
                return new FilterTrackViewHolder(inflater.inflate(R.layout.item_filter_track, parent, false));
            case FILTER_SOCIAL_HOLDER:
                return new FilterSocialHolder(inflater.inflate(R.layout.item_filter_social, parent, false));
            case FILTER_CONFERENCE_HOLDER:
                return new FilterConferenceViewHolder(inflater.inflate(R.layout.item_filter_conference, parent, false));
            case FILTER_FILE_TYPE_HOLDER:
                return new FilterFileTypeViewHolder(inflater.inflate(R.layout.item_filter_file_type, parent, false));
        }
        return null;
    }

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TitleViewHolder) {
            Title title = (Title) getItem(position);
            ((TitleViewHolder) holder).title.setText(Html.fromHtml(title.title));
            ((TitleViewHolder) holder).marker.setVisibility(title.isSelected ? View.VISIBLE : View.GONE);
            afterBind(holder, title, position);
        } else if (holder instanceof FilterDayViewHolder) {
            Filter filter = (Filter) getItem(position);
            FilterDayViewHolder fdvh = (FilterDayViewHolder) holder;

            int dayNum = ((DayCategoryWrapper) filter.tag).category.getStartAt().dayOfYear().get() - ApplicationController.getActiveConference().getStartAt().dayOfYear().get() + 1;

            fdvh.dayNumber.setText(String.valueOf(dayNum));

            String date = ((DayCategoryWrapper) filter.tag).category.getStartAt().toString("EEEE, dd.MM.yyyy");
            //polish days have a small first letter, so below code fix this
            date = date.substring(0, 1).toUpperCase() + date.substring(1);
            fdvh.date.setText(date);
            fdvh.setChecked(filter.isSelected);
            afterBind(holder, filter, position);
        } else if (holder instanceof FilterTrackViewHolder) {
            Filter filter = (Filter) getItem(position);
            FilterTrackViewHolder ftvh = (FilterTrackViewHolder) holder;

            if (filter.title != null && !filter.title.isEmpty()) {
                ftvh.title.setText(Html.fromHtml(filter.title));
            } else {
                ftvh.title.setText(filter.title);
            }

            ftvh.trackIndicator.setVisibility(View.VISIBLE);
            ftvh.setChecked(filter.isSelected);
            ftvh.trackIndicator.setBackgroundColor(Color.parseColor(filter.trackColor));

            afterBind(holder, filter, position);

        } else if (holder instanceof FilterSocialHolder){
            Filter filter = (Filter) getItem(position);
            FilterSocialHolder fsh = (FilterSocialHolder) holder;

            fsh.title.setText(Html.fromHtml(filter.title));

            @DrawableRes int drawableIcon = 0;

            switch (filter.medium){
                case Message.FACEBOOK:
                    drawableIcon = R.drawable.facebook;
                    break;
                case Message.TWITTER:
                    drawableIcon = R.drawable.twitter;
                    break;
                case Message.ORGANIZER:
                    drawableIcon= R.drawable.organizer;
                    break;
            }

            fsh.socialIcon.setImageDrawable(VectorDrawableCompat.create(context.getResources(), drawableIcon, null));
            fsh.setChecked(filter.isSelected);
            afterBind(holder, filter, position);
        } else if (holder instanceof  FilterConferenceViewHolder) {
            Filter filter = (Filter) getItem(position);
            FilterConferenceViewHolder fcvh = (FilterConferenceViewHolder) holder;
            fcvh.title.setText(Html.fromHtml(filter.title));
            fcvh.setChecked(filter.isSelected);
            afterBind(holder, filter, position);
        } else if(holder instanceof FilterFileTypeViewHolder) {
            Filter filter = (Filter) getItem(position);
            FilterFileTypeViewHolder fftvh = (FilterFileTypeViewHolder) holder;
            fftvh.title.setText(Html.fromHtml(filter.title));
            fftvh.setChecked(filter.isSelected);

            @DrawableRes int drawableIcon = 0;

            switch (filter.medium) {
                case Archive.TYPE_PDF:
                    drawableIcon = R.drawable.archive_pdf;
                    break;
                case Archive.TYPE_DOCUMENT:
                    drawableIcon = R.drawable.archive_pdf;
                    break;
                case Archive.TYPE_PRESENTATION:
                    drawableIcon = R.drawable.archive_graph;
                    break;
                case Archive.TYPE_VIDEO:
                    drawableIcon = R.drawable.archive_video;
                    break;
                case Archive.TYPE_LINK:
                    drawableIcon = R.drawable.archive_www;
                    break;
                case Archive.TYPE_UNDEFINED:
                    drawableIcon = R.drawable.archive;
                    break;
            }

            fftvh.fileTypeIcon.setImageDrawable(VectorDrawableCompat.create(context.getResources(), drawableIcon, null));

            afterBind(holder, filter, position);
        }
    }

    public abstract void removeFilter();

    public abstract boolean isFilterOn();

    public abstract void saveFilter();

    protected abstract Object getItem(int position);

    public abstract void afterBind(RecyclerView.ViewHolder holder, Object item, int position);

    public static class TitleViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tvItemFilterTitle)
        public TextView title;

        @Bind(R.id.ivItemFilterMarker)
        public ImageView marker;

        public TitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class FilterDayViewHolder extends RecyclerView.ViewHolder {


        @Bind(R.id.tvItemFilterDayNumber)
        public TextView dayNumber;

        @Bind(R.id.tvItemFilterDate)
        public TextView date;

        @Bind(R.id.ivItemFilterMarker)
        public ImageView marker;

        @Bind(R.id.vItemFilterDaySeparator)
        public View separator;

        public FilterDayViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setChecked(boolean checked) {
            if (checked) {
                marker.setImageResource(R.drawable.ic_check_circle_black_24dp);
            } else {
                marker.setImageResource(R.drawable.ic_panorama_fish_eye_black_24dp);
            }
        }
    }

    public static class FilterTrackViewHolder extends RecyclerView.ViewHolder {


        @Bind(R.id.tvItemFilterTitle)
        public TextView title;

        @Bind(R.id.ivItemFilterMarker)
        public ImageView marker;

        @Bind(R.id.vItemFilterTrackIndicator)
        public View trackIndicator;

        public FilterTrackViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setChecked(boolean checked) {
            if (checked) {
                marker.setImageResource(R.drawable.ic_check_circle_black_24dp);
            } else {
                marker.setImageResource(R.drawable.ic_panorama_fish_eye_black_24dp);
            }
        }
    }

    public static class FilterSocialHolder extends RecyclerView.ViewHolder{

        @Bind(R.id.ivItemFilterSocialIndicator)
        public ImageView socialIcon;

        @Bind(R.id.tvItemFilterSocial)
        public TextView title;

        @Bind(R.id.ivItemFilterMarker)
        public ImageView marker;

        public FilterSocialHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setChecked(boolean checked) {
            if (checked) {
                marker.setImageResource(R.drawable.ic_check_circle_black_24dp);
            } else {
                marker.setImageResource(R.drawable.ic_panorama_fish_eye_black_24dp);
            }
        }
    }

    public static class FilterConferenceViewHolder extends RecyclerView.ViewHolder{

        @Bind(R.id.ivItemFilterConferenceIndicator)
        public ImageView conferenceIcon;

        @Bind(R.id.tvItemFilterConference)
        public TextView title;

        @Bind(R.id.ivItemFilterMarker)
        public ImageView marker;

        public FilterConferenceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setChecked(boolean checked) {
            if (checked) {
                marker.setImageResource(R.drawable.ic_check_circle_black_24dp);
            } else {
                marker.setImageResource(R.drawable.ic_panorama_fish_eye_black_24dp);
            }
        }
    }

    public static class FilterFileTypeViewHolder extends RecyclerView.ViewHolder{

        @Bind(R.id.ivItemFilterFileTypeIcon)
        public ImageView fileTypeIcon;

        @Bind(R.id.tvItemFilterFileType)
        public TextView title;

        @Bind(R.id.ivItemFilterMarker)
        public ImageView marker;

        public FilterFileTypeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setChecked(boolean checked) {
            if (checked) {
                marker.setImageResource(R.drawable.ic_check_circle_black_24dp);
            } else {
                marker.setImageResource(R.drawable.ic_panorama_fish_eye_black_24dp);
            }
        }
    }

    public final static class Title {
        public String title;
        public boolean isSelected;

        public Type type;

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public Title(String s, Type type) {
            title = s;
            this.type = type;
        }
    }

    public final static class Filter {
        public String title;
        public Type type;
        public int id;
        public boolean isSelected = true;
        public boolean enabled = true;
        public String trackColor;

        // Medium used in messages filter
        public String medium;

        public Object tag;

        public Filter(String title, Type type, int id) {
            this.title = title;
            this.type = type;
            this.id = id;
        }
        public Filter(Type type, int id) {
            this.type = type;
            this.id = id;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public Object getTag() {
            return tag;
        }

        public void setTag(Object tag) {
            this.tag = tag;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return medium;
        }
    }

    public static class DayCategoryWrapper{

        public List<Integer> trackId = new ArrayList<>();
        public Category category;

        public DayCategoryWrapper(Category category) {
            this.category = category;
            loadTrackId();

        }

        private void loadTrackId(){
            for (Event e : getEvents()) {
                if (e.getStartAt().getDayOfYear() == category.getStartAt().getDayOfYear()){
                    for (Track t : e.getTrackList()) {
                        if (!trackId.contains(t.getId())){
                            trackId.add(t.getId());
                        }
                    }
                }
            }

            Timber.d("DayCategoryWrapper:loadTrackId: " + String.valueOf(trackId));
        }

        public List<Event> getEvents(){
            List<Event> events = new ArrayList<>();
            Conference activeConference = ApplicationController.getActiveConference();

            try{
                QueryBuilder<Event, Integer> qb = ApplicationController.getDatabaseHelper().getEventDao().queryBuilder();
                qb.where().eq(Event.CONFERENCE_ID, activeConference.getId());
                events.addAll(qb.query());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return events;
        }

        public List<Integer> getAvailableTracksId(){
            return trackId;
        }

        @Override
        public String toString() {
            return "" + category.getId();
        }
    }
}
