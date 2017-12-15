package com.pgssoft.testwarez.feature.agenda.all;


import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.AgendaScrollEvent;
import com.pgssoft.testwarez.util.Utils;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;


public class AllFragment extends Fragment {
    private static final String REFRESH_KEY = "refresh_key";

    @Bind(R.id.agenda_recycler_view)
    RecyclerView agendaRecycleView;
    @Bind(R.id.agenda_progress_bar)
    ProgressBar agendaProgressBar;

    private AgendaAdapter adapter;
    private LinearLayoutManager layoutManager;

    private boolean refresh = false;

    public static AllFragment newInstance() {
        AllFragment fragment = new AllFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AllFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all, container, false);

        ButterKnife.bind(this, view);

        layoutManager = new LinearLayoutManager(getContext());
        agendaRecycleView.setLayoutManager(layoutManager);
        agendaProgressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(getContext(), R.color.primary), PorterDuff.Mode.SRC_ATOP);

        adapter = new AgendaAdapter(getContext());

        agendaRecycleView.setAdapter(adapter);
        if (savedInstanceState != null) {
            refresh = savedInstanceState.getBoolean(REFRESH_KEY, false);
        }

        if (refresh) {
            adapter.notifyDataSetChanged();
            refresh = false;
        }

        return view;
    }

    public void hideProgressBar() {
        agendaProgressBar.setVisibility(View.GONE);
    }

    public void updateView() {
        if (agendaRecycleView != null) {
            adapter.setUserFilter();
        } else {
            refresh = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ApplicationController.getBus().register(this);

        adapter.restoreHeadersState();
    }

    @Override
    public void onPause() {
        super.onPause();

        ApplicationController.getBus().unregister(this);
        Utils.saveAgendaScrollPosition(getScrollPosition());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(REFRESH_KEY, true);
    }

    public void resetSearchFilter() {
        if (adapter != null) {
            adapter.resetAdapter();
        }
    }

    public int getScrollPosition() {
        return layoutManager.findFirstVisibleItemPosition();
    }

    @Subscribe
    public void onAgendaScrollEvent(AgendaScrollEvent event) {
        agendaRecycleView.scrollToPosition(event.getPosition());
    }
}
