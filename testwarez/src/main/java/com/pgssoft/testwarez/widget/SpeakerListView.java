package com.pgssoft.testwarez.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.feature.speaker.list.SpeakerListAdapter;
import com.pgssoft.testwarez.event.SearchEvent;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.util.Observables;
import com.squareup.otto.Subscribe;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SpeakerListView extends ListView {
    private SpeakerListAdapter adapter;


    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public SpeakerListView(Context context) {
        super(context);
        init();
    }

    public SpeakerListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpeakerListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setDivider(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ApplicationController.getBus().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        ApplicationController.getBus().unregister(this);

        compositeSubscription.clear();
        compositeSubscription.unsubscribe();
        super.onDetachedFromWindow();
    }

    public void updateView() {

        compositeSubscription.add(Observables.getInstance().getSpeakerObservable().getSpeakers()
                .toList()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(speakers -> {
                    adapter = new SpeakerListAdapter(getContext(), speakers, false);
                    setAdapter(adapter);
                    setSelection(0);
                }, Throwable::printStackTrace));

    }


    @Subscribe
    public void onSearchEvent(SearchEvent event) {

        compositeSubscription.add(
                Observables.getInstance().getSpeakerObservable().getSpeakers()
                        .filter(speaker -> getVisibility() != VISIBLE || event.type != SearchEvent.SEARCH_TYPE.SPEAKERS || speaker.getFullName().toLowerCase().contains(event.query.toLowerCase()))
                        .toList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(speakers -> {
                            adapter = new SpeakerListAdapter(getContext(), speakers, false);
                            setAdapter(adapter);
//                            setSelection(0);
                        }, Throwable::printStackTrace));

    }

    public Speaker getSpeaker(int position) {
        return adapter.getItem(position);
    }
}
