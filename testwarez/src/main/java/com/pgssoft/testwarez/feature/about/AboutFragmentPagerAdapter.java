package com.pgssoft.testwarez.feature.about;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.pgssoft.testwarez.R;

/**
 * Created by rtulaza on 2015-08-05.
 */
public class AboutFragmentPagerAdapter extends FragmentStatePagerAdapter {

    public void updateView() {
        if (descriptionFragment != null){
            descriptionFragment.updateView();
        }

        if (organizerFragment != null){
            organizerFragment.updateView();
        }

        if (partnersFragment != null){
            partnersFragment.updateView();
        }

        if (sponsorFragment != null) {
            sponsorFragment.updateView();
        }
    }

    private DescriptionFragment descriptionFragment;
    private PhotoListFragment organizerFragment;
    private PhotoListFragment partnersFragment;
    private PhotoListFragment sponsorFragment;
    public enum TAB_TITLE {
        DESCRIPTION, ORGANIZERS, PARTNERS, SPONSORS
    }

    private Context context;

    public AboutFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        TAB_TITLE tab = TAB_TITLE.values()[position];
        switch (tab) {
            case DESCRIPTION:
                descriptionFragment = DescriptionFragment.newInstance();
                return descriptionFragment;
            case ORGANIZERS:
                organizerFragment = PhotoListFragment.newInstance(TAB_TITLE.ORGANIZERS);
                return organizerFragment;
            case PARTNERS:
                partnersFragment = PhotoListFragment.newInstance(TAB_TITLE.PARTNERS);
                return partnersFragment;
            case SPONSORS:
                sponsorFragment = PhotoListFragment.newInstance(TAB_TITLE.SPONSORS);
            return sponsorFragment;
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
            case DESCRIPTION:
                return context.getResources().getString(R.string.about_tab_description);
            case ORGANIZERS:
                return context.getResources().getString(R.string.about_tab_organizers);
            case PARTNERS:
                return context.getResources().getString(R.string.about_tab_partners);
            case SPONSORS:
                return context.getResources().getString(R.string.about_tab_sponsors);
        }

        return null;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
