package com.pgssoft.testwarez.feature.archive.event;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.feature.speaker.list.SpeakerListAdapter;
import com.pgssoft.testwarez.database.model.Archive;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.util.ReadMoreLayoutListener;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by brosol on 2016-04-07.
 */
public class ArchiveEventDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final private Event event;
    final private ArrayList<Speaker> speakers;
    final private Context context;
    final private Point screenSize;
    final private ArrayList<Archive> archives;

    private SpeakerListAdapter speakerListAdapter;
    private ArchivesListAdapter archivesListAdapter;


    public ArchiveEventDataAdapter(Context context, Event event, ArrayList<Speaker> speakers, ArrayList<Archive> archives, Point screenSize) {
        this.context = context;
        this.event = event;
        this.speakers = speakers;
        this.screenSize = screenSize;
        this.archives = archives;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v;
        switch (i) {
            case 0:
                v = inflater.inflate(R.layout.rv_event_archives, viewGroup, false);
                return new ArchivesViewHolder(v);
            case 1:
                v = inflater.inflate(R.layout.rv_event_description, viewGroup, false);
                return new DescriptionViewHolder(v);
            case 2:
                v = inflater.inflate(R.layout.rv_event_speakers, viewGroup, false);
                return new SpeakersViewHolder(v);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder s, int i) {
        switch (i) {
            case 0:
                final ArchivesViewHolder avh = (ArchivesViewHolder) s;
                if (archives != null) {
                    if (archives.size() > 0) {
                        avh.itemView.setVisibility(View.VISIBLE);
                        archivesListAdapter = new ArchivesListAdapter(context, archives);
                        avh.archivesList.removeAllViews();

                        for (int k = 0; k < archivesListAdapter.getCount(); k++) {
                            avh.archivesList.addView(archivesListAdapter.getView(k, null, avh.archivesList));
                        }
                    } else {
                        avh.title.setVisibility(View.GONE);
                    }
                } else {
                    avh.title.setVisibility(View.GONE);
                }
                return;
            case 1:
                DescriptionViewHolder dvh = (DescriptionViewHolder) s;
                if (event.getDescriptions().size() > 0) {
                    dvh.itemView.setVisibility(View.VISIBLE);
                    dvh.description.setText(Html.fromHtml(event.getDescription()));
                    dvh.description.setMovementMethod(LinkMovementMethod.getInstance());
                    dvh.description.setLinksClickable(true);
                } else {
                    dvh.itemView.setVisibility(View.GONE);
                }

                dvh.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ReadMoreLayoutListener(context, dvh.readMore, dvh.readMoreIcon, dvh.readMoreText, dvh.description, screenSize));
                dvh.readMoreIcon.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.primary), PorterDuff.Mode.MULTIPLY));
                return;
            case 2:
                final SpeakersViewHolder svh = (SpeakersViewHolder) s;

                svh.rateContainer.setVisibility(View.GONE);

                if (speakers != null) {
                    if (speakers.size() > 0) {
                        svh.itemView.setVisibility(View.VISIBLE);

                        speakerListAdapter = new SpeakerListAdapter(context, speakers, true);

                        svh.speakersList.removeAllViews();

                        for (int j = 0; j < speakerListAdapter.getCount(); j++) {
                            svh.speakersList.addView(speakerListAdapter.getView(j, null, svh.speakersList));
                        }
                    } else {
                        svh.itemView.setVisibility(View.GONE);
                    }
                } else {
                    svh.itemView.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 3;
    }


    public class DescriptionViewHolder extends RecyclerView.ViewHolder {

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
        }
    }

    public class SpeakersViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.speakers_list)
        protected LinearLayout speakersList;

        @Bind(R.id.tvRVEventSpeakersTitle)
        protected TextView title;

        @Bind(R.id.rate_container)
        protected FrameLayout rateContainer;

        public SpeakersViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }

    public class ArchivesViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.archives_list)
        protected LinearLayout archivesList;

        @Bind(R.id.tvRVEventArchivesTitle)
        protected TextView title;

        public ArchivesViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}
