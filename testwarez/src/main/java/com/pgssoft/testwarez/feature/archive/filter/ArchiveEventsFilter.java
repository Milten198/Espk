package com.pgssoft.testwarez.feature.archive.filter;

import com.pgssoft.testwarez.feature.archive.event.ExpandableArchiveEventAdapter;
import com.pgssoft.testwarez.database.model.Archive;
import com.pgssoft.testwarez.util.Utils;

import java.util.List;

import rx.functions.Func1;

public class ArchiveEventsFilter implements Func1<ExpandableArchiveEventAdapter.ListItem, Boolean> {

        List<Integer> filterConferencesIdList;
        List<Integer> fileList;

        public ArchiveEventsFilter() {
            filterConferencesIdList = Utils.getConferenceFilter();
            fileList = Utils.getArchiveFileTypesFilter();
        }

        @Override
        public Boolean call(ExpandableArchiveEventAdapter.ListItem listItem) {
            boolean conferenceFilter = filterConference(listItem);
            boolean fileFilter = filterFile(listItem);

            return conferenceFilter && fileFilter;
        }


    private boolean filterFile(ExpandableArchiveEventAdapter.ListItem listItem) {
        if (fileList.isEmpty() || listItem.getConference() != null){
            return true;
        }else if (listItem.getEvent() != null){
            boolean filter = false;

            for (Archive e : listItem.getEvent().getAllArchives()) {
                if (fileList.contains(e.getIntType())){
                    filter = true;
                }
            }
            return filter;
        }

        return false;
    }

    private boolean filterConference(ExpandableArchiveEventAdapter.ListItem listItem) {
        if(listItem.getConference() != null && !filterConferencesIdList.isEmpty()) {
            return filterConferencesIdList.contains(listItem.getConference().getId());
        } else if(filterConferencesIdList.isEmpty()) {
            return true;
        } else if(listItem.event != null) {
            return filterConferencesIdList.contains(listItem.event.getConferenceId());
        }
        return false;
    }
}