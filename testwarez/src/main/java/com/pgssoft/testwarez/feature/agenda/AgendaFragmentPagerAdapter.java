package com.pgssoft.testwarez.feature.agenda;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.feature.agenda.all.AllFragment;
import com.pgssoft.testwarez.feature.agenda.favorite.FavoriteFragment;

/**
 * Created by rtulaza on 2015-08-14.
 */
public class AgendaFragmentPagerAdapter extends FragmentPagerAdapter {


    private enum TAB_TITLE {
        ALL, FAVORITE
    }

    private Context context;
    public AllFragment allFragment;
    private FavoriteFragment favoriteFragment;

    public AgendaFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        this.context = context;
    }



    @Override
    public Fragment getItem(int position) {
        TAB_TITLE tab = TAB_TITLE.values()[position];
        switch (tab) {
            case ALL:
                allFragment = AllFragment.newInstance();
                return allFragment;
            case FAVORITE:
                favoriteFragment = FavoriteFragment.newInstance();
                return favoriteFragment;
        }

        return null;
    }

    @Override
    public int getCount() {
        return TAB_TITLE.values().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        TAB_TITLE tab = TAB_TITLE.values()[position];
        switch (tab) {
            case ALL:
                return context.getResources().getString(R.string.agenda_tab_all);
            case FAVORITE:
                return context.getResources().getString(R.string.agenda_tab_favorite);
        }

        return null;
    }

    public void resetSearchFilter() {
        if(allFragment != null) {
            allFragment.resetSearchFilter();
        }
    }

    public void updateView() {
        if (allFragment != null){
            allFragment.updateView();
        }

        if (favoriteFragment != null){
            favoriteFragment.updateView();
        }
    }
}
