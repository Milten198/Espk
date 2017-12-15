package com.pgssoft.testwarez.feature.archive.video;

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
import com.pgssoft.testwarez.event.ShowVideosRecyclerEvent;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by dpodolak on 08.03.16.
 */
public class ArchiveVideoFragment extends Fragment {


    @Bind(R.id.rvFragmentArchiveVideo)
    RecyclerView expandableRecyclerView;
    @Bind(R.id.no_videos_layout)
    RelativeLayout noVideosLayout;

    private ArchiveVideoAdapter archiveVideoAdapter;

    public static ArchiveVideoFragment newInstance() {
        ArchiveVideoFragment fragmentInstance;
        fragmentInstance = new ArchiveVideoFragment();
        Bundle args = new Bundle();
        fragmentInstance.setArguments(args);

        return fragmentInstance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View videoView = inflater.inflate(R.layout.fragment_archive_video, container, false);
        ButterKnife.bind(this, videoView);

        expandableRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        archiveVideoAdapter = new ArchiveVideoAdapter(getContext());
        expandableRecyclerView.setAdapter(archiveVideoAdapter);

        return videoView;
    }


    public void updateView() {
        archiveVideoAdapter.update();
    }

    @Override
    public void onResume() {
        super.onResume();
        ApplicationController.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ApplicationController.getBus().unregister(this);
    }

    @Subscribe
    public void onSearchEvent(SearchEvent event) {
        if (event.type != SearchEvent.SEARCH_TYPE.ARCHIVE_VIDEOS) {
            return;
        }

        archiveVideoAdapter.search(event.query);
    }

    @Subscribe
    public void onShowVideosRecyclerEvent(ShowVideosRecyclerEvent event) {
        if(event.isShow()) {
            noVideosLayout.setVisibility(View.GONE);
        } else {
            noVideosLayout.setVisibility(View.VISIBLE);
        }
    }
}
