package com.pgssoft.testwarez.feature.archive.filter;

import com.pgssoft.testwarez.feature.archive.event.ExpandableArchiveEventAdapter;
import com.pgssoft.testwarez.database.model.Event;

import rx.functions.Func1;

public class EventSearchFilter implements Func1<ExpandableArchiveEventAdapter.ListItem, Boolean> {

        String query;

        public EventSearchFilter(String query) {
            this.query = query;
        }

        @Override
        public Boolean call(ExpandableArchiveEventAdapter.ListItem listItem) {
            if (listItem.event != null){
                Event event = listItem.event;
                if (event.getTitle().toLowerCase().contains(query.toLowerCase())){
                    return true;
                }else{
                    return false;
                }
            }
            return true;
        }
    }

