package com.pgssoft.testwarez.feature.speaker.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseNavigationDrawerActivity;
import com.pgssoft.testwarez.event.OpenPersonEvent;
import com.pgssoft.testwarez.event.SearchEvent;
import com.pgssoft.testwarez.feature.speaker.detail.PersonActivity;
import com.pgssoft.testwarez.widget.SpeakerListView;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by dpodolak on 24.02.16.
 */
public class SpeakerListActivity extends BaseNavigationDrawerActivity implements AdapterView.OnItemClickListener {


    @Bind(R.id.splASpeakerListActivity)
    SpeakerListView speakerListView;

    @Override
    public void updateView() {
        speakerListView.updateView();
    }

    @Override
    public SearchEvent.SEARCH_TYPE getSerachType() {
        return SearchEvent.SEARCH_TYPE.SPEAKERS;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker_list);
        ButterKnife.bind(this);

        updateView();
        speakerListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, PersonActivity.class);
        intent.putExtra(PersonActivity.PERSON_ID, speakerListView.getSpeaker(position).getId());
        intent.putExtra(PersonActivity.PERSON_TYPE, 0);
    }

    @Subscribe
    public void onOpenSpeakerEvent(OpenPersonEvent event) {
        PersonActivity.open(this, event);
    }


}
