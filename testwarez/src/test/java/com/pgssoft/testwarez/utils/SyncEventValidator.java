package com.pgssoft.testwarez.utils;

import android.support.annotation.StringDef;

import com.pgssoft.testwarez.event.SyncDataEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dpodolak on 04.07.16.
 */
public class SyncEventValidator {

    public static final String SYNC_ALL_PATTERN = "7,5,1,(2),3";
    public static final String SYNC_ALL_WITH_LANDING_PAGE_PATTERN = "7,4,(2),3";

    @StringDef({SYNC_ALL_PATTERN, SYNC_ALL_WITH_LANDING_PAGE_PATTERN})
    @Retention(RetentionPolicy.SOURCE)
    public static @interface EventPattern{}


    public static boolean isValid(List<SyncDataEvent> eventsList, @EventPattern String pattern){
        List<Integer> patternEvents = new ArrayList<>();

        int repeatingValue = SyncDataEvent.EMPTY_STATE;

        boolean isValid = true;

        for (String rv: pattern.split(",")){
            if (rv.contains("(") && rv.contains(")")){
                repeatingValue = Integer.parseInt(rv.replace("(", "").replace(")", ""));
            }else{
                patternEvents.add(Integer.parseInt(rv));
            }
        }

        int repeatingValueCount = 0;
        for (int i=0; i<eventsList.size(); i++){
            int stateOfEvent = eventsList.get(i).getSyncState();

            if (stateOfEvent != repeatingValue){
                isValid = stateOfEvent == patternEvents.get(i - repeatingValueCount);
            }else{
                repeatingValueCount++;
            }

            if (i == eventsList.size()-1){
                isValid = stateOfEvent == SyncDataEvent.SYNC_ARCHIVE_COMPLETE;
            }
        }

        return isValid;
    }

    public static boolean validConferencesToRegister(List<SyncDataEvent> events, List<Integer> conferenceIds){
        int conferencesCount = 0;

        for(SyncDataEvent event: events){
            if (event.getSyncState() == SyncDataEvent.DOWNLOAD_ACTIVE_CONFERENCE_CONTENT || event.getSyncState() == SyncDataEvent.DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT){
                conferencesCount++;
            }
        }

        return conferenceIds.size() == conferencesCount;
    }

}
