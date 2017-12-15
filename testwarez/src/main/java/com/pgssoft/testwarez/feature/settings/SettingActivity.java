package com.pgssoft.testwarez.feature.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseNavigationDrawerActivity;
import com.pgssoft.testwarez.event.ChangeLanguageEvent;
import com.pgssoft.testwarez.event.SearchEvent;
import com.pgssoft.testwarez.util.Utils;
import com.pgssoft.testwarez.widget.SettingsView;
import com.squareup.otto.Subscribe;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by dpodolak on 24.02.16.
 */
public class SettingActivity extends BaseNavigationDrawerActivity {


    @Bind(R.id.svSettingsActivity)
    SettingsView settingsView;

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
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSION_CALENDAR:{
                if (grantResults.length > 0){
                    settingsView.onCalendarPermissionGranted(grantResults[0] == PackageManager.PERMISSION_GRANTED);
                }
            }break;
        }

    }

    @Subscribe
    public void onLangChange(ChangeLanguageEvent event){

        if (Locale.getDefault().toString().equals(event.getLocale().toString())){
            return;
        }

        Utils.saveAsDefaultLanguage(this, event.getLocale().toString());
        Intent refreshIntent = new Intent(SettingActivity.this, SettingActivity.class);
        refreshIntent.putExtra(CURRENT_MENU_ID, currentMenuId);
        refreshIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(refreshIntent);
        overridePendingTransition(0, 0);
    }

}
