package com.pgssoft.testwarez.feature.messages;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.pwittchen.reactivenetwork.library.ConnectivityStatus;
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseNavigationDrawerActivity;
import com.pgssoft.testwarez.event.EmptyListEvent;
import com.pgssoft.testwarez.event.SearchEvent;
import com.pgssoft.testwarez.core.FilterActivity;
import com.pgssoft.testwarez.feature.messages.filter.MediaFilter;
import com.pgssoft.testwarez.service.MyGcmListenerService;
import com.pgssoft.testwarez.util.Utils;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by dpodolak on 24.02.16.
 */
public class MessagesActivity extends BaseNavigationDrawerActivity {
    public static final String OPEN_ORGANIZER_MESSAGES = "open_organizer_messages";

    public static final int FILTER_REQUEST = 100;

    private MessageAdapter messageAdapter;

    @Bind(R.id.rvActivityMessage)
    RecyclerView recyclerView;

    @Bind(R.id.tvActivityMessageList)
    TextView placeholderTextView;

    @Override
    public void updateView() {

    }

    @Override
    public SearchEvent.SEARCH_TYPE getSerachType() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        ButterKnife.bind(this);

        setMenu(R.menu.filter_main_menu);

        boolean fromMessage = getIntent().getBooleanExtra(OPEN_ORGANIZER_MESSAGES, false);

        if (fromMessage) {
            setCurrentItem(R.id.messages_menu_item);
            MyGcmListenerService.clearNotification(this);
        }

        messageAdapter = new MessageAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        compositeSubscription.add(new ReactiveNetwork()
                .observeConnectivity(this)
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectivityStatus -> {
                    if (ConnectivityStatus.OFFLINE != connectivityStatus) {
                        messageAdapter.loadMessages(new MediaFilter());
                    }
                }, Throwable::printStackTrace));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setFilterIcon();

        return super.onPrepareOptionsMenu(menu);
    }

    private void setFilterIcon() {
        Menu menu = getMenu();
        if (menu != null) {
            MenuItem filterItem = menu.findItem(R.id.main_menu_filter);
            filterItem.setIcon(!Utils.getSocialFilter().isEmpty() ? R.drawable.filter_icon_active : R.drawable.filter_icon);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_menu_filter) {
            Intent filterIntent = new Intent(this, FilterActivity.class);
            filterIntent.putExtra(FilterActivity.REQUEST_CODE, FILTER_REQUEST);
            startActivityForResult(filterIntent, FILTER_REQUEST);
            overridePendingTransition(R.anim.top_translate_in, R.anim.top_translate_out);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILTER_REQUEST) {
            setFilterIcon();
            messageAdapter.updateWithFilter();
        }
    }

    @Subscribe
    public void onEmptyListEvent(EmptyListEvent event) {
        placeholderTextView.setVisibility(event.isEmpty() ? View.VISIBLE : View.GONE);
    }

}
