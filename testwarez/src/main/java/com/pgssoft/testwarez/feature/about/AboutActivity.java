package com.pgssoft.testwarez.feature.about;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.View;

import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseNavigationDrawerActivity;
import com.pgssoft.testwarez.event.SearchEvent;
import com.pgssoft.testwarez.widget.AboutView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by dpodolak on 24.02.16.
 */
public class AboutActivity extends BaseNavigationDrawerActivity {
    @Bind(R.id.avAboutView)
    AboutView aboutView;
    private AboutFragmentPagerAdapter aboutFragmentPagerAdapter;


    @Override
    public void updateView() {
        aboutFragmentPagerAdapter.updateView();
    }

    @Override
    public SearchEvent.SEARCH_TYPE getSerachType() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        aboutFragmentPagerAdapter = new AboutFragmentPagerAdapter(this, getSupportFragmentManager());
        aboutView.setAdapter(aboutFragmentPagerAdapter);

        setTabLayoutVisibility(View.VISIBLE);
        setTabLayoutPager(aboutView);
        setTabLayoutMode(TabLayout.MODE_SCROLLABLE);
    }
}
