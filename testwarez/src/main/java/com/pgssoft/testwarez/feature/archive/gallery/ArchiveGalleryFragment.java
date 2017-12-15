package com.pgssoft.testwarez.feature.archive.gallery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.NetworkAccessEvent;
import com.pgssoft.testwarez.event.ShowGalleryRecycler;
import com.pgssoft.testwarez.widget.AutoFitStaggeredGridLayoutManager;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by dpodolak on 08.03.16.
 */
public class ArchiveGalleryFragment extends Fragment {

    private ArchiveGalleryAdapter adapter;

    @Bind(R.id.rvFragmentArchiveGallery)
    protected RecyclerView recyclerView;
    @Bind(R.id.no_galleries_layout)
    protected RelativeLayout noGalleriesLayout;

    public static ArchiveGalleryFragment newInstance() {
        ArchiveGalleryFragment fragmentInstance;
        fragmentInstance = new ArchiveGalleryFragment();
        Bundle args = new Bundle();
        fragmentInstance.setArguments(args);

        return fragmentInstance;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archive_gallery, container, false);
        ButterKnife.bind(this, view);

        setAdapter();
        ApplicationController.getBus().register(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.unSubscribe();
        }
        ApplicationController.getBus().unregister(this);
    }

    @Subscribe
    public void onInternetConnection(NetworkAccessEvent event) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void onShowGalleryRecyclerEvent(ShowGalleryRecycler event) {
        if(event.isShow()) {
            noGalleriesLayout.setVisibility(View.GONE);
        } else {
            noGalleriesLayout.setVisibility(View.VISIBLE);
        }
    }

    public void updateView() {
        setAdapter();
    }

    private void setAdapter() {
        AutoFitStaggeredGridLayoutManager layoutManager;
        layoutManager = new AutoFitStaggeredGridLayoutManager
                (2, StaggeredGridLayoutManager.VERTICAL, getContext());

        recyclerView.setLayoutManager(layoutManager);
        adapter = new ArchiveGalleryAdapter(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setItemViewCacheSize(10);
    }
}
