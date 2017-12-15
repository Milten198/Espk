package com.pgssoft.testwarez.feature.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseNavigationDrawerActivity;
import com.pgssoft.testwarez.core.FilterActivity;
import com.pgssoft.testwarez.feature.event.EventActivity;
import com.pgssoft.testwarez.event.FavoriteRefreshEvent;
import com.pgssoft.testwarez.event.LoadAgendaEvent;
import com.pgssoft.testwarez.event.OpenEventEvent;
import com.pgssoft.testwarez.event.SearchEvent;
import com.pgssoft.testwarez.util.Observables;
import com.pgssoft.testwarez.util.Utils;
import com.pgssoft.testwarez.util.helper.PageChangeHelper;
import com.pgssoft.testwarez.widget.AgendaViewPager;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.schedulers.Schedulers;

/**
 * Created by dpodolak on 24.02.16.
 */
public class AgendaActivity extends BaseNavigationDrawerActivity {

    public static final int OPEN_EVENT_REQUEST = 12;

    public static final int FILTER_REQUEST = 101;

    private AgendaFragmentPagerAdapter agendaPagerAdapter;

    @Bind(R.id.avAgenda)
    AgendaViewPager agendaViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);
        ButterKnife.bind(this);


        agendaPagerAdapter = new AgendaFragmentPagerAdapter(this, getSupportFragmentManager());
        agendaViewPager.setAdapter(agendaPagerAdapter);

        updateView();

        setMenu(R.menu.filter_main_menu);

        setTabLayoutVisibility(View.VISIBLE);
        setTabLayoutPager(agendaViewPager);
        setTabLayoutMode(TabLayout.MODE_FIXED);

        setCurrentItem(R.id.agenda_menu_item);

        agendaViewPager.addOnPageChangeListener(new PageChangeHelper() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == 0) {
                    disableSearchBar();
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                disableSearchBar();

                if (getMenu() != null) {
                    if (position == 1) {
                        // workaround
                        new Handler().postDelayed(() -> getMenu().findItem(R.id.main_menu_filter).setVisible(false), 500);
                    } else {
                        getMenu().findItem(R.id.main_menu_filter).setVisible(true);
                        if (agendaPagerAdapter.allFragment != null) {
                            agendaPagerAdapter.allFragment.resetSearchFilter();
                        }
                    }
                }
            }
        });

        compositeSubscription.add(Observables.getInstance().getDayFilterObservable(this).toList().subscribeOn(Schedulers.newThread()).subscribe(filters -> {
        }, Throwable::printStackTrace));

        if (getIntent().getBooleanExtra("init_snackbar", false)) {
            showNoInternetConnectionSnackbar(agendaViewPager, false);
        }

        if (!Observables.getInstance().getEventObservables().isEventsObservableLoaded()) {
            Observables.getInstance().getEventObservables().refreshEventsObservable();
        }
    }

    @Override
    public void updateView() {
        agendaPagerAdapter.updateView();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public SearchEvent.SEARCH_TYPE getSerachType() {
        if (agendaViewPager.getCurrentItem() == 0) {
            return SearchEvent.SEARCH_TYPE.ALL;
        } else {
            return SearchEvent.SEARCH_TYPE.FAVORITE;
        }
    }

    @Subscribe
    public void onLoadAgendaEvent(LoadAgendaEvent event) {
        if (agendaPagerAdapter.allFragment != null) {
            agendaPagerAdapter.allFragment.hideProgressBar();
        }
    }

    @Subscribe
    public void onOpenEventEvent(OpenEventEvent openEventEvent) {
        EventActivity.open(this, openEventEvent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setFilterIcon();
        return super.onPrepareOptionsMenu(menu);
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

        switch (requestCode) {
            case FILTER_REQUEST:
                updateView();
                setFilterIcon();
                break;
            case OPEN_EVENT_REQUEST:
                if (resultCode == EventActivity.REFRESH_NEEDED) {
                    int id = data.getExtras().getInt(EventActivity.EVENT_ID);
                    compositeSubscription.add(Observables.getInstance().getEventObservables().getEventsObservable()
                            .doOnError(Throwable::printStackTrace)
                            .filter(event -> event.getId() == id)
                            .subscribeOn(Schedulers.newThread())
                            .subscribe(event1 -> ApplicationController.getBus().post(new FavoriteRefreshEvent(event1))));
                }
                break;
        }
    }

    private void setFilterIcon() {
        Menu menu = getMenu();
        if (menu != null) {
            MenuItem filterItem = menu.findItem(R.id.main_menu_filter);
            boolean dayFilter = !Utils.getAgendaDayFilter().isEmpty();
            boolean trackFilter = !Utils.getAgendaTrackFilter().isEmpty();
            filterItem.setIcon(dayFilter || trackFilter ? R.drawable.filter_icon_active : R.drawable.filter_icon);
        }
    }
}
