package com.pgssoft.testwarez.feature.archive;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.feature.archive.event.ArchiveEventsFragment;
import com.pgssoft.testwarez.feature.archive.gallery.ArchiveGalleryFragment;
import com.pgssoft.testwarez.feature.archive.video.ArchiveVideoFragment;

/**
 * Created by dpodolak on 08.03.16.
 */
public class ArchiveFragmentAdapter extends FragmentPagerAdapter {

    public static final String TYPE_EXTRA = "type_extra";

    public void update() {
        if (eventsFragment != null){
            eventsFragment.updateView();
        }

        if (galleryFragment != null){
            galleryFragment.updateView();
        }

        if (videoFragment != null){
            videoFragment.updateView();
        }
    }

    ArchiveEventsFragment eventsFragment;
    ArchiveGalleryFragment galleryFragment;
    ArchiveVideoFragment videoFragment;

    public enum TAB_TITLE {
        EVENTS, GALLERY, VIDEO
    }

    private final Context context;

    public ArchiveFragmentAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        TAB_TITLE tabTitle = TAB_TITLE.values()[position];

        switch (tabTitle){
            case EVENTS:
                eventsFragment = ArchiveEventsFragment.newInstance();
                return eventsFragment;
            case GALLERY:
                galleryFragment = ArchiveGalleryFragment.newInstance();
                return galleryFragment;
            case VIDEO:
                videoFragment = ArchiveVideoFragment.newInstance();
            return videoFragment;
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        TAB_TITLE tabTitle = TAB_TITLE.values()[position];

        switch (tabTitle){
            case EVENTS:
                return context.getString(R.string.archive_tab_events);
            case GALLERY:
                return context.getString(R.string.archive_tab_gallery);
            case VIDEO:
                return context.getString(R.string.archive_tab_video);
        }

        return null;
    }

    @Override
    public int getCount() {
        return TAB_TITLE.values().length;
    }
}
