package com.pgssoft.testwarez.feature.archive.event;

import android.content.Context;
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
import com.pgssoft.testwarez.event.OpenArchiveEventEvent;
import com.pgssoft.testwarez.event.ShowAgendaPlaceHolder;
import com.pgssoft.testwarez.feature.archive.filter.ArchiveEventsFilter;
import com.pgssoft.testwarez.feature.archive.filter.EventSearchFilter;
import com.pgssoft.testwarez.database.model.Archive;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.util.Observables;

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

/**
 * Created by dawidpodolak on 13.04.16.
 */
public class ExpandableArchiveEventAdapter extends ExpandableRecyclerAdapter<ExpandableArchiveEventAdapter.ListItem> {

    private static final int TYPE_ITEM = 1001;

    private Observable<ExpandableArchiveEventAdapter.ListItem> itemListObservable;

    private Subscription conferenceSubscription;


    public ExpandableArchiveEventAdapter(Context context) {
        super(context);

        itemListObservable = Observables.getInstance().getArchiveAgendaObservable();

        loadItems(new ArchiveEventsFilter());
    }


    private void loadItems(Func1<ListItem, Boolean> filter) {
        Observable<List<ListItem>> presenterObservable;

        if (filter != null && filter instanceof EventSearchFilter) {
            presenterObservable = itemListObservable.filter(filter)
                    .toList()
                    .flatMap(listItems -> {
                        List<ListItem> listItemList = new ArrayList<>();

                        //This logic prevent before display conferences without videos
                        for (int i = 0; i < listItems.size(); i++) {
                            if (listItems.get(i).getConference() != null && i < (listItems.size() - 1) && listItems.get(i + 1).getEvent() != null) {
                                listItemList.add(listItems.get(i));

                            } else if (listItems.get(i).getEvent() != null) {
                                listItemList.add(listItems.get(i));
                            }
                        }

                        return Observable.just(listItemList);
                    });
        } else if (filter != null) {
            presenterObservable = itemListObservable.filter(filter).toList();
        } else {
            presenterObservable = itemListObservable.toList();
        }

        conferenceSubscription = presenterObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listItems -> {
                    setItems(listItems);
                    ApplicationController.getBus().post(new ShowAgendaPlaceHolder(listItems.size() == 0));
                }, Throwable::printStackTrace, this::expandAll);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case TYPE_HEADER:
                return new ConferenceViewHolder(inflate(R.layout.item_archive_conference_header, parent));
            default:
                return new EventViewHolder(inflate(R.layout.archive_event_item, parent));
        }
    }


    @Override
    public void onBindViewHolder(ExpandableRecyclerAdapter.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                ((ConferenceViewHolder) holder).bind(position);
                break;
            default:
                ((EventViewHolder) holder).bind(position);
                ((EventViewHolder) holder).itemView.setOnClickListener(v -> {
                    EventViewHolder evh = (EventViewHolder) holder;
                    ApplicationController.getBus().post(new OpenArchiveEventEvent(visibleItems.get(position).event, evh.archiveEventTitle));
                });
                break;
        }
    }

    public void setFilter() {
        visibleItems.clear();

        itemListObservable = Observables.getInstance().getArchiveAgendaObservable();
        loadItems(new ArchiveEventsFilter());
    }

    public void unSubscribe() {
        conferenceSubscription.unsubscribe();
    }

    public void search(String query) {
        visibleItems.clear();
        loadItems(query.isEmpty() ? null : new EventSearchFilter(query));
    }


    public static class ListItem extends ExpandableRecyclerAdapter.ListItem {

        private Conference conference;

        public Event event;

        public ListItem(Conference conference) {
            super(TYPE_HEADER);

            this.conference = conference;
        }

        public ListItem(Event event) {
            super(TYPE_ITEM);
            this.event = event;
        }


        @Override
        public String toString() {
            if (conference != null) {
                return "conference: " + conference.getName();
            } else if (event != null) {
                return "event: " + event.getTitle();
            }

            return null;
        }

        public Conference getConference() {
            return conference;
        }

        public Event getEvent() {
            return event;
        }
    }

    public class ConferenceViewHolder extends ExpandableRecyclerAdapter.HeaderViewHolder {

        @Bind(R.id.tvItemArchiveConferenceHeaderTitle)
        protected TextView title;

        public ConferenceViewHolder(View view) {
            super(view, ButterKnife.findById(view, R.id.ivItemArchiveConferenceHeaderArrow));
            ButterKnife.bind(this, view);
        }

        @Override
        public void bind(int position) {
            super.bind(position);
            int[] screenLoc = new int[2];
            arrow.getLocationOnScreen(screenLoc);
            title.setText(Html.fromHtml(visibleItems.get(position).conference.getName()));
        }
    }

    public class EventViewHolder extends ExpandableRecyclerAdapter.ViewHolder {

        @Bind(R.id.archive_event_title)
        TextView archiveEventTitle;

        @Bind(R.id.archive_event_file_icon_1)
        protected ImageView archiveEventFileIconOne;

        @Bind(R.id.archive_event_file_icon_2)
        protected ImageView archiveEventFileIconTwo;

        @Bind(R.id.archive_event_file_icon_3)
        protected ImageView archiveEventFileIconThree;

        @Bind(R.id.archive_event_file_icon_4)
        protected ImageView archiveEventFileIconFour;


        @Bind(R.id.llArchiveEventItemSpeakers)
        protected LinearLayout speakerContainer;

        private LayoutInflater inflater;

        private float dp;

        private DisplayImageOptions options;

        public EventViewHolder(View view) {
            super(view);
            inflater = LayoutInflater.from(view.getContext());
            dp = view.getContext().getResources().getDisplayMetrics().density;
            ButterKnife.bind(this, view);

            options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .showImageForEmptyUri(R.drawable.user_placeholder)
                    .showImageOnFail(R.drawable.user_placeholder)
                    .showImageOnLoading(R.drawable.user_placeholder)
                    .build();

        }

        private void bind(int position) {
            Event event = visibleItems.get(position).event;

            archiveEventFileIconOne.setImageDrawable(null);
            archiveEventFileIconTwo.setImageDrawable(null);
            archiveEventFileIconThree.setImageDrawable(null);
            archiveEventFileIconFour.setImageDrawable(null);

            ArrayList<Speaker> speakers = event.getAllSpeakers();

            speakerContainer.removeAllViews();

            if (!speakers.isEmpty()){
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                for (Speaker s : speakers){
                    View view = inflater.inflate(R.layout.item_agenda_event_list_speaker, null);
                    view.setLayoutParams(params);

                    if (params.topMargin == 0){
                        params.topMargin = (int) (4*dp);
                    }

                    TextView speaker = ButterKnife.findById(view, R.id.tvItemAgendaEventList);
                    CircleImageView speakerPhoto = ButterKnife.findById(view, R.id.civItemAgendaEventList);

                    if (s != null) {
                        speaker.setText(Html.fromHtml(s.getFullName()));
                    }

                    speakerPhoto.setVisibility(View.VISIBLE);
                    int id = -1;

                    if (s.getPhoto() != null) {
                        id = s.getPhoto().getFileId();
                    }

                    ImageLoader.getInstance().displayImage(mContext.getResources().getString(R.string.endpoint) +
                            mContext.getResources().getString(R.string.images_url_default, id), speakerPhoto, options);
                    speakerContainer.addView(view);
                }
            }

            archiveEventTitle.setText(Html.fromHtml(event.getTitle()));

            boolean isPdf = false;
            boolean isPresentation = false;
            boolean isVideo = false;
            boolean isLink = false;
            boolean isDocument = false;
            boolean isOther = false;
            int counter = 0;

            List<ImageView> imageViews = new ArrayList<>();
            imageViews.add(archiveEventFileIconOne);
            imageViews.add(archiveEventFileIconTwo);
            imageViews.add(archiveEventFileIconThree);
            imageViews.add(archiveEventFileIconFour);

            List<Archive> archives = event.getAllArchives();

            for (Archive archive : archives) {
                if (archive != null) {
                    if (counter < 4) {
                        switch (archive.getType()) {
                            case Archive.TYPE_PDF:
                                if (!isPdf) {
                                    isPdf = true;
                                    imageViews.get(counter).setImageDrawable(VectorDrawableCompat.create(mContext.getResources(), R.drawable.archive_pdf, null));
                                    counter++;
                                }
                                break;
                            case Archive.TYPE_DOCUMENT:
                                if (!isDocument) {
                                    isDocument = true;
                                    imageViews.get(counter).setImageDrawable(VectorDrawableCompat.create(mContext.getResources(), R.drawable.archive_doc, null));
                                    counter++;
                                }
                                break;
                            case Archive.TYPE_PRESENTATION:
                                if (!isPresentation) {
                                    isPresentation = true;
                                    imageViews.get(counter).setImageDrawable(VectorDrawableCompat.create(mContext.getResources(), R.drawable.archive_graph, null));
                                    counter++;
                                }
                                break;
                            case Archive.TYPE_LINK:
                                if (!isLink) {
                                    isLink = true;
                                    imageViews.get(counter).setImageDrawable(VectorDrawableCompat.create(mContext.getResources(), R.drawable.archive_www, null));
                                    counter++;
                                }
                                break;
                            case Archive.TYPE_VIDEO:
                                if (!isVideo) {
                                    isVideo = true;
                                    imageViews.get(counter).setImageDrawable(VectorDrawableCompat.create(mContext.getResources(), R.drawable.archive_video, null));
                                    counter++;
                                }
                                break;
                            case Archive.TYPE_UNDEFINED:
                                if (!isOther) {
                                    imageViews.get(counter).setImageDrawable(VectorDrawableCompat.create(mContext.getResources(), R.drawable.archive, null));
                                    counter++;
                                }
                                break;
                            default:
                                if (!isOther) {
                                    imageViews.get(counter).setImageDrawable(VectorDrawableCompat.create(mContext.getResources(), R.drawable.archive, null));
                                    counter++;
                                }
                                break;
                        }
                    }
                }
            }

        }
    }
}
