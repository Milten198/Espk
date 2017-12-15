package com.pgssoft.testwarez.feature.agenda.favorite;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.FavoriteEmptyListEvent;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;


public class FavoriteFragment extends Fragment {

    @Bind(R.id.tvFragmentFavorite)
    TextView lackOfElementsTextView;

    private FavoriteAdapter favoriteAdapter;

    @Bind(R.id.rvFragmentFavorite)
    RecyclerView favoriteRecyclerView;


    public static FavoriteFragment newInstance() {
        FavoriteFragment fragment = new FavoriteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public FavoriteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        favoriteRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoriteAdapter = new FavoriteAdapter(getContext());
        favoriteRecyclerView.setAdapter(favoriteAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        ApplicationController.getBus().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ApplicationController.getBus().unregister(this);
    }

    @Subscribe
    public void onEmptyList(FavoriteEmptyListEvent event){
        lackOfElementsTextView.setVisibility(event.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroy() {
        favoriteAdapter.onDestroy();
        super.onDestroy();
    }

    public void updateView() {
        favoriteAdapter.updateView();
    }
}
