package com.pgssoft.testwarez.feature.archive.video;

import android.content.Context;
import android.support.annotation.IntDef;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.ExpandableRecyclerAdapter;
import com.pgssoft.testwarez.event.ArchiveVideoClickEvent;
import com.pgssoft.testwarez.event.ShowVideosRecyclerEvent;
import com.pgssoft.testwarez.feature.archive.filter.VideoSearchFilter;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.database.model.Video;
import com.pgssoft.testwarez.util.Observables;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dpodolak on 19.04.16.
 */
public class ArchiveVideoAdapter extends ExpandableRecyclerAdapter<ArchiveVideoAdapter.ListItem> {

    CompositeSubscription compositeSubscription = new CompositeSubscription();
    public static final int TYPE_VIDEO = 1001;

    Observable<ListItem> allItemObservable;

    public ArchiveVideoAdapter(Context context) {
        super(context);

        allItemObservable = Observables.getInstance().getArchiveVideoAgenda();

        loadItems(null);
    }

    private void loadItems(Func1<ListItem, Boolean> filter) {

        Observable<List<ListItem>> presentationItemObservable;

        if (filter != null) {
            presentationItemObservable = allItemObservable
                    .filter(filter)
                    .toList()
                    .flatMap(listItems -> {
                        List <ListItem> newListItems = new ArrayList<>();
                        for(int i=0; i< listItems.size(); i++){
                            if (i<(listItems.size()-1) && listItems.get(i).getConference()!= null && listItems.get(i+1).getConference()== null){
                                newListItems.add(listItems.get(i));
                            }else if (listItems.get(i).getVideo() != null){
                                newListItems.add(listItems.get(i));
                            }
                        }
                        return Observable.just(newListItems);
                    });
        } else {
            presentationItemObservable = allItemObservable.toList();
        }

        compositeSubscription.add(
                presentationItemObservable
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(listItems -> {
                                    visibleItems.clear();
                                    setItems(listItems);
                                    if(visibleItems.size() != 0) {
                                        ApplicationController.getBus().post(new ShowVideosRecyclerEvent(true));
                                    } else {
                                        ApplicationController.getBus().post(new ShowVideosRecyclerEvent(false));
                                    }
                                    notifyDataSetChanged();
                                    expandAll();
                                }, Throwable::printStackTrace
                        ));
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new ConferenceViewHolder(inflate(R.layout.item_archive_conference_header, parent));
            case TYPE_VIDEO:
                return new VideoViewHolder(inflate(R.layout.item_archive_video, parent));

        }
        return null;
    }

    @Override
    public void onBindViewHolder(ExpandableRecyclerAdapter.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                ((ConferenceViewHolder) holder).bind(position);
                break;
            case TYPE_VIDEO:
                ((VideoViewHolder) holder).bind(position);
                break;
        }
    }

    public void onDestroy() {
        compositeSubscription.unsubscribe();
        compositeSubscription.clear();
    }

    public void update() {
        allItemObservable = Observables.getInstance().refreshArchiveVideoAgenda();
        loadItems(null);
    }

    public void search(String query) {
        loadItems(query.isEmpty() ? null : new VideoSearchFilter(query));
    }

    public static class ListItem extends ExpandableRecyclerAdapter.ListItem {

        private Conference conference;
        private Video video;

        @IntDef({TYPE_HEADER, TYPE_VIDEO})
        @Retention(RetentionPolicy.SOURCE)
        public @interface Type {
        }

        public ListItem(Conference conference) {
            super(TYPE_HEADER);
            this.conference = conference;
        }

        public ListItem(Video video) {
            super(TYPE_VIDEO);
            this.video = video;
        }

        public Conference getConference() {
            return conference;
        }

        public Video getVideo() {
            return video;
        }

        @Type
        public int getType() {
            return ItemType;
        }

        @Override
        public String toString() {
            if (video != null) {
                return "video: " + video.getName();
            }

            if (conference != null) {
                return "conference: " + conference.getName();
            }

            return "empty";
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
            title.setText(Html.fromHtml(visibleItems.get(position).conference.getName()));
        }
    }

    public class VideoViewHolder extends ExpandableRecyclerAdapter.ItemViewHolder {

        @Bind(R.id.tvItemArchiveVideoTitle)
        TextView title;


        public VideoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(int position) {
            Video video = visibleItems.get(position).video;

            title.setText(Html.fromHtml(video.getName()));
            itemView.setOnClickListener(v -> {
                compositeSubscription.add(
                        allItemObservable
                                .doOnError(Throwable::printStackTrace)
                                .filter(listItem -> listItem.getType() == TYPE_VIDEO)
                                .map(listItem -> listItem.video)
                                .toList()
                                .subscribe(videoList -> ApplicationController.getBus().post(new ArchiveVideoClickEvent(videoList, videoList.indexOf(video)))));

            });

        }
    }
}
