package com.pgssoft.testwarez.feature.messages;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.stmt.QueryBuilder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.EmptyListEvent;
import com.pgssoft.testwarez.event.NotificationMessageEvent;
import com.pgssoft.testwarez.feature.messages.filter.MediaFilter;
import com.pgssoft.testwarez.database.model.Message;
import com.pgssoft.testwarez.database.model.SocialMessage;
import com.squareup.otto.Subscribe;

import java.sql.SQLException;
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
 * Created by dpodolak on 08.04.16.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    CompositeSubscription compositeSubscription = new CompositeSubscription();


    private LayoutInflater layoutInflater;

    private List<Message> presentMessageList = new ArrayList<>();

    /**
     * Store social message and message from organizers
     */
    private final Observable<List<Message>> localObservable = Observable.defer(() -> Observable.just(ApplicationController.getActiveConference()))
            .observeOn(Schedulers.io())
            .flatMap(conference -> Observable.merge(Observable.from(getMessages(conference.getId())), Observable.from(getSocialMessages(conference.getId()))))
            .toSortedList((message1, message2) ->
                    message1 instanceof SocialMessage ?
                            (message2 instanceof SocialMessage ?
                                    ((SocialMessage) message2).getPublishedAt().compareTo(((SocialMessage) message1).getPublishedAt()) :
                                    message2.getUpdatedAt().compareTo(((SocialMessage) message1).getPublishedAt())) :
                            (message2 instanceof SocialMessage ?
                                    ((SocialMessage) message2).getPublishedAt().compareTo(message1.getUpdatedAt()) :
                                    message2.getUpdatedAt().compareTo(message1.getUpdatedAt()))
            );

    private Context context;

    private final Observable<? extends List<? extends Message>> networkObservable = Observable.defer(() -> Observable.just(ApplicationController.getActiveConference()))
            .flatMap(conference -> Observable.merge(
                    ApplicationController.getNetworkInterface().loadSocialMessages(conference.getId())
                            .onErrorResumeNext(throwable -> Observable.empty()),
                    ApplicationController.getNetworkInterface().loadMessages(conference.getId())
                            .onErrorResumeNext(throwable -> Observable.empty())))
            .flatMap(messages -> messages != null ? Observable.from(messages) : Observable.empty())
            .doOnError(Throwable::printStackTrace)
            .doOnNext(message -> {
                try {
                    message.setConferenceId(ApplicationController.getActiveConference().getId());
                    if (message instanceof SocialMessage) {
                        ApplicationController.getDatabaseHelper().getSocialMessageDao().createOrUpdate((SocialMessage) message);
                    } else {
                        ApplicationController.getDatabaseHelper().getMessageDao().createOrUpdate(message);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .toList()
            .subscribeOn(Schedulers.newThread());

    private Observable<Message> messageObservable = getMessageObservable();


    public MessageAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        ApplicationController.getBus().register(this);

        loadMessages(new MediaFilter());
    }

    public Observable<Message> getMessageObservable() {
        return localObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(messages -> {
                    presentMessageList.clear();
                    presentMessageList.addAll(messages);
                    notifyDataSetChanged();
                    ApplicationController.getBus().post(new EmptyListEvent(presentMessageList.isEmpty()));
                })
                .observeOn(Schedulers.newThread())
                .flatMap(messages1 -> networkObservable.flatMap(messages2 -> {
                    if (messages2 != null && messages1.size() != messages2.size()) {
                        return localObservable
                                .flatMap(Observable::from);
                    } else {
                        return Observable.from(messages1);
                    }
                }))
                .replay().autoConnect();
    }

    public void onDestroy() {
        if (compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }

        ApplicationController.getBus().unregister(this);
    }


    @Subscribe
    public void onNewMessage(NotificationMessageEvent event) {
        messageObservable = getMessageObservable();
        loadMessages(new MediaFilter());
    }

    public void loadMessages(Func1<Message, Boolean> filter) {

        compositeSubscription.add(messageObservable
                .filter(filter)
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((List<Message> messages) -> {
                    if (messages != null) {
                        presentMessageList.clear();
                        presentMessageList.addAll(messages);
                        notifyDataSetChanged();
                        ApplicationController.getBus().post(new EmptyListEvent(presentMessageList.isEmpty()));
                    }

                }, Throwable::printStackTrace));
    }

    public List<Message> getMessages(int conferenceId) {

        List<Message> messages = new ArrayList<>();

        try {
            QueryBuilder<Message, Integer> qb = ApplicationController.getDatabaseHelper().getMessageDao().queryBuilder();
            qb.where().eq(Message.CONFERENCE_ID, conferenceId);
            messages.addAll(qb.query());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

    public List<SocialMessage> getSocialMessages(int conferenceId) {

        List<SocialMessage> messages = new ArrayList<>();

        try {
            QueryBuilder<SocialMessage, Integer> qb = ApplicationController.getDatabaseHelper().getSocialMessageDao().queryBuilder();
            qb.where().eq(Message.CONFERENCE_ID, conferenceId);
            messages.addAll(qb.query());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageViewHolder(layoutInflater.inflate(R.layout.item_messages_social, parent, false));
    }

    @Override
    public void onBindViewHolder(MessageViewHolder vh, int position) {
        Message item = presentMessageList.get(position);


        int uri = 0;
        switch (item.getMedium()) {
            case Message.FACEBOOK:
                uri += R.drawable.facebook;
                break;
            case Message.TWITTER:
                uri += R.drawable.twitter;
                break;
            case Message.ORGANIZER:
                uri += R.drawable.organizer;
                break;
        }

        vh.socialAvatar.setImageResource(uri);

        try {
            vh.time.setText(item instanceof SocialMessage ? ((SocialMessage) item).getPublishedAt().toString("HH:mm, dd MMMM yyyy", context.getResources().getConfiguration().locale) : item.getCreatedAt().toString("HH:mm, dd MMMM yyyy", context.getResources().getConfiguration().locale));
        } catch (Exception e) {
            e.printStackTrace();
        }

        vh.text.setText(Html.fromHtml(item.getDescriptions()));
        vh.text.setMovementMethod(LinkMovementMethod.getInstance());
        vh.text.setLinksClickable(true);

        if (item instanceof SocialMessage && ((SocialMessage) item).getLink() != null && !((SocialMessage) item).getLink().isEmpty()) {
            vh.link.setVisibility(View.VISIBLE);
            vh.link.setText(((SocialMessage) item).getLink());
            vh.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(((SocialMessage) item).getLink()));
                context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.messages_chooser_title)));
            });
        } else {
            vh.link.setVisibility(View.GONE);
            vh.itemView.setOnClickListener(null);
        }

        if (item instanceof SocialMessage && ((SocialMessage) item).getFoto() != null && !((SocialMessage) item).getFoto().isEmpty()) {
            vh.photo.setVisibility(View.VISIBLE);

            ImageLoader.getInstance().displayImage(((SocialMessage) item).getFoto(), vh.photo);
        } else {
            vh.photo.setVisibility(View.GONE);

        }
    }

    @Override
    public int getItemCount() {
        return presentMessageList.size();
    }

    public void updateWithFilter() {
        loadMessages(new MediaFilter());
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.social_avatar)
        ImageView socialAvatar;

        @Bind(R.id.time)
        TextView time;

        @Bind(R.id.text)
        TextView text;

        @Bind(R.id.link)
        TextView link;

        @Bind(R.id.photo)
        ImageView photo;

        public MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
