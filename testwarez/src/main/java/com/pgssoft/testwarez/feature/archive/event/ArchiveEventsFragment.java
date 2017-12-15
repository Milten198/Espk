package com.pgssoft.testwarez.feature.archive.event;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.SearchEvent;
import com.pgssoft.testwarez.event.ShowAgendaPlaceHolder;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by dpodolak on 08.03.16.
 */
public class ArchiveEventsFragment extends Fragment {


    private ExpandableArchiveEventAdapter expandableRecyclerAdapter;

    private static final String REFRESH_KEY = "refresh_key";

    @Bind(R.id.rvFragmentArchiveEvents)
    protected RecyclerView recyclerView;
    @Bind(R.id.no_archive_events_layout)
    protected RelativeLayout noArchiveEventsLayout;

    public static ArchiveEventsFragment newInstance() {
        ArchiveEventsFragment fragmentInstance;
        fragmentInstance = new ArchiveEventsFragment();
        Bundle args = new Bundle();
        fragmentInstance.setArguments(args);

        return fragmentInstance;
    }

    private boolean refresh = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationController.getBus().register(this);
        setRetainInstance(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archive_events, container, false);
        ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expandableRecyclerAdapter = new ExpandableArchiveEventAdapter(getContext());
        recyclerView.setAdapter(expandableRecyclerAdapter);

        if(savedInstanceState != null) {
            refresh = savedInstanceState.getBoolean(REFRESH_KEY, false);
        }

        if(refresh) {
            expandableRecyclerAdapter.notifyDataSetChanged();
            refresh = false;
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(REFRESH_KEY, true);
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Subscribe
    public void onSearchEvent(SearchEvent event) {
        if (event.type != SearchEvent.SEARCH_TYPE.ARCHIVE_EVENTS) {
            return;
        }

        expandableRecyclerAdapter.search(event.query);
    }

    @Subscribe
    public void onShowAgendaRecyclerEvent(ShowAgendaPlaceHolder event) {
        if(event.isShow()) {
            noArchiveEventsLayout.setVisibility(View.VISIBLE);
        } else {
            noArchiveEventsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (expandableRecyclerAdapter != null) {
            expandableRecyclerAdapter.unSubscribe();
        }
        ApplicationController.getBus().unregister(this);
    }

    public void updateView() {
        if (recyclerView != null){
            expandableRecyclerAdapter.setFilter();
        }else{
            refresh = true;
        }
    }
}
