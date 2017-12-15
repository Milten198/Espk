package com.pgssoft.testwarez.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.feature.agenda.AgendaActivity;
import com.pgssoft.testwarez.feature.agenda.filter.AgendaFilterAdapter;
import com.pgssoft.testwarez.feature.messages.MessagesActivity;
import com.pgssoft.testwarez.feature.archive.filter.ArchiveAgendaFilterAdapter;
import com.pgssoft.testwarez.feature.messages.filter.MessageFilterAdapter;
import com.pgssoft.testwarez.event.FilterArchiveConferencesEvent;
import com.pgssoft.testwarez.feature.archive.ArchiveActivity;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dpodolak on 08.03.16.
 */
public class FilterActivity extends BaseActivity {

    public static final String REQUEST_CODE = "request_code";
    public static final int FILTER_RESULT_CODE = 200;
    @Bind(R.id.rvActivityFilters)
    RecyclerView filterRecyclerView;

    @Bind(R.id.filterToolbar)
    Toolbar toolbar;

    private BaseFilterAdapter filterAdapter;

    private int filterRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.filter_title);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        filterRequest = getIntent().getExtras().getInt(REQUEST_CODE);
        toolbar.setNavigationOnClickListener(v -> FilterActivity.this.finish());

        LinearLayoutManager manager = new LinearLayoutManager(this);

        filterRecyclerView.setLayoutManager(manager);

        if (filterRequest == AgendaActivity.FILTER_REQUEST) {
            filterAdapter = new AgendaFilterAdapter(this);
        } else if (filterRequest == MessagesActivity.FILTER_REQUEST) {
            filterAdapter = new MessageFilterAdapter(this);
        } else if (filterRequest == ArchiveActivity.FILTER_REQUEST) {
            filterAdapter = new ArchiveAgendaFilterAdapter(this);
        }

        filterRecyclerView.setAdapter(filterAdapter);
    }

    @OnClick(R.id.bActivityFilterApply)
    public void applyFilters() {
        filterAdapter.saveFilter();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.filter_menu, menu);
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.bottom_translate_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.filter_menu_accept) {
            filterAdapter.removeFilter();
        }

        return super.onOptionsItemSelected(item);
    }


    @Subscribe
    public void onFilterArchiveConferencesEvent(FilterArchiveConferencesEvent filterArchiveConferencesEvent) {
        setResult(FILTER_RESULT_CODE);
    }
}

