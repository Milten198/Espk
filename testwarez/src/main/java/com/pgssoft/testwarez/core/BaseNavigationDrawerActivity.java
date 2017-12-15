package com.pgssoft.testwarez.core;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.SearchEvent;
import com.pgssoft.testwarez.feature.about.AboutActivity;
import com.pgssoft.testwarez.feature.agenda.AgendaActivity;
import com.pgssoft.testwarez.feature.archive.ArchiveActivity;
import com.pgssoft.testwarez.feature.map.MapActivity;
import com.pgssoft.testwarez.feature.messages.MessagesActivity;
import com.pgssoft.testwarez.feature.plan.BuildingPlanActivity;
import com.pgssoft.testwarez.feature.settings.SettingActivity;
import com.pgssoft.testwarez.feature.speaker.list.SpeakerListActivity;
import com.pgssoft.testwarez.util.Utils;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by dpodolak on 24.02.16.
 */
public abstract class BaseNavigationDrawerActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String CURRENT_MENU_ID = "current_menu_id";

    public static final String ACTION_UPDATE = "com.pgssoft.testwarez.ACTION_UPDATE";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    private Toolbar toolbar;
    private RelativeLayout appBarContainer;
    private FrameLayout mContainerLayout;
    private TabLayout tabLayout;

    public int currentMenuId;

    private MenuItem currentMenuItem;

    private Menu menu;

    private int menuId = -1;

    private SearchView searchView;

    private BroadcastReceiver updateViewBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateView();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);


        drawerLayout = ButterKnife.findById(this, R.id.drawer_layout);
        toolbar = ButterKnife.findById(this, R.id.toolbar);
        navigationView = ButterKnife.findById(this, R.id.navigation_view);
        appBarContainer = ButterKnife.findById(this, R.id.app_bar_container);
        mContainerLayout = ButterKnife.findById(this, R.id.flABaseContainer);
        tabLayout = ButterKnife.findById(this, R.id.tab_layout);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setItemBackgroundResource(R.drawable.drawer_item_selector);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer_action, R.string.close_drawer_action) {
            @Override
            public void onDrawerOpened(View drawerView) {
                Utils.hideKeyboard(BaseNavigationDrawerActivity.this);

                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        currentMenuId = getIntent().getIntExtra(CURRENT_MENU_ID, -1);

        setCurrentItem(currentMenuId);

        navigationView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                navigationView.getViewTreeObserver().removeOnPreDrawListener(this);

                drawerLayout.closeDrawers();
                return true;
            }
        });

        IntentFilter intentFilter = new IntentFilter(ACTION_UPDATE);
        registerReceiver(updateViewBroadcast, intentFilter);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        savedInstanceState.clear();
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(updateViewBroadcast);
        super.onDestroy();
    }

    public void setCurrentItem(int currentMenuId) {
        currentMenuItem = navigationView.getMenu().findItem(currentMenuId);

        /**
         * Open drawer and close drawer in order to nice pass through beetwen the activities
         */
        if (currentMenuItem != null) {
            currentMenuItem.setChecked(true);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(currentMenuItem.getTitle());
            }
            if (getIntent().getBooleanExtra("openedDrawer", false)) {
                getIntent().removeExtra("openedDrawer");
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        }
    }


    public void setTabLayoutVisibility(int visibility) {
        tabLayout.setVisibility(visibility);
    }

    public void setTabLayoutMode(int mode) {
        tabLayout.setTabMode(mode);
    }

    public void setTabLayoutPager(ViewPager adapter) {
        tabLayout.setupWithViewPager(adapter);
    }


    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        LayoutInflater inflater = LayoutInflater.from(this);
        mContainerLayout.addView(inflater.inflate(layoutResID, mContainerLayout, false));
    }

    @Override
    public void setContentView(View v) {
        mContainerLayout.addView(v);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        if (currentMenuId == menuItem.getItemId()) {
            drawerLayout.closeDrawers();
            return true;
        }

        Intent menuIntent = new Intent();
        menuIntent.putExtra(CURRENT_MENU_ID, menuItem.getItemId());

        ApplicationController.getParentManager().setMenuId(menuItem.getItemId());
        menuIntent.putExtra("openedDrawer", true);
        switch (menuItem.getItemId()) {
            case R.id.agenda_menu_item:
                menuIntent.setClass(this, AgendaActivity.class);
                break;

            case R.id.speakers_menu_item:
                menuIntent.setClass(this, SpeakerListActivity.class);
                break;
            case R.id.building_map_menu_item:
                menuIntent.setClass(this, BuildingPlanActivity.class);
                break;

            case R.id.messages_menu_item:
                menuIntent.setClass(this, MessagesActivity.class);
                break;

            case R.id.map_menu_item:
                menuIntent.setClass(this, MapActivity.class);
                break;

            case R.id.archive_menu_item:
                menuIntent.setClass(this, ArchiveActivity.class);
                break;

            case R.id.about_menu_item:
                menuIntent.setClass(this, AboutActivity.class);
                break;

            case R.id.settings_menu_item:
                menuIntent.setClass(this, SettingActivity.class);
                break;
        }

        startActivity(menuIntent);
        overridePendingTransition(0, 0);
        finish();

        return false;
    }

    public void setToolbarTitle(String toolbarTitle) {
        toolbar.setTitle(toolbarTitle);
    }

    public abstract void updateView();


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        if (menuId == -1) {
            menuId = R.menu.main_menu;
        }

        inflater.inflate(menuId, menu);
        this.menu = menu;

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView = (SearchView) menu.findItem(R.id.main_menu_search).getActionView();
        searchView.setIconifiedByDefault(true);
        searchView.setOnSearchClickListener(v -> {
            MenuItem filterItem = menu.findItem(R.id.main_menu_filter);
            if (filterItem != null) {
                filterItem.setVisible(false);
            }

            if(!Utils.isTablet(this)) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            searchView.requestFocusFromTouch();
        });
        searchView.setOnCloseListener(() -> {
            MenuItem filterItem = menu.findItem(R.id.main_menu_filter);
            if (filterItem != null && (getSerachType() != SearchEvent.SEARCH_TYPE.ARCHIVE_VIDEOS && getSerachType() != null)) {
                filterItem.setVisible(true);
            }

            if(!Utils.isTablet(this)) {
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            }
            return false;
        });
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));


        Subscription querySubscription = RxSearchView.queryTextChanges(searchView)
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> {
                    if (!searchView.isIconified()) {
                        ApplicationController.getBus().post(new SearchEvent(String.valueOf(charSequence), getSerachType()));
                    }
                }, Throwable::printStackTrace);

        compositeSubscription.add(querySubscription);

        if (getSerachType() == null) {
            menu.findItem(R.id.main_menu_search).setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.main_menu_search) {
            MenuItem filterItem = menu.findItem(R.id.main_menu_filter);
            filterItem.setVisible(item.collapseActionView());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public abstract SearchEvent.SEARCH_TYPE getSerachType();

    public void setMenu(int menu) {
        this.menuId = menu;
    }

    public Menu getMenu() {
        return menu;
    }

    public void disableNavigationDrawer() {
        navigationView.setEnabled(false);

        drawerLayout.setEnabled(false);
        drawerToggle.setDrawerIndicatorEnabled(false);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void disableSearchBar() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        }
    }

    public boolean isSearchExpanded() {
        return !searchView.isIconified();
    }


}
