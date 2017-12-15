package com.pgssoft.testwarez.service;

import android.app.IntentService;
import android.content.Intent;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.database.model.Favorite;
import com.pgssoft.testwarez.util.Utils;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by dpodolak on 13.06.16.
 */
public class AlarmReschedulingIntentService extends IntentService{

    public static final String TAG = "AlarmReschedulingIntentService";

    public AlarmReschedulingIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(Utils.areRemindersEnabled()) {
            try {
                List<Favorite> allFavorites = ApplicationController.getDatabaseHelper().getFavoriteDao().queryForAll();

                if (Utils.areRemindersEnabled()) {
                    for (Favorite fav : allFavorites) {
                        Utils.cancelEventAlarm(this, fav.getEvent());
                        Utils.setEventAlarm(this, fav);
                    }
                }else if (Utils.isCalendarSync()){


                    for (Favorite fav : allFavorites) {
                        Utils.cancelCalendarEvent(this, fav.getEvent());
                        Utils.setCalendarEvent(this, fav);
                    }
                }
            }
            catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
