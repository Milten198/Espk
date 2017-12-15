package com.pgssoft.testwarez.feature.agenda.filter;

import com.pgssoft.testwarez.feature.agenda.all.AgendaAdapter;
import com.pgssoft.testwarez.database.model.Event;

import rx.functions.Func1;

public class PlaceFilter implements Func1<AgendaAdapter.ListItem, Boolean> {

        private int placeId;

        public PlaceFilter(int placeId) {
            this.placeId = placeId;
        }

        @Override
        public Boolean call(AgendaAdapter.ListItem listItem) {

            if (listItem.getEvent() != null) {
                Event event = listItem.getEvent();
                if (event.getPlace() == null) {
                    return false;
                }
                return event.getPlace().getId() == placeId;
            } else if (listItem.getCategory() != null) {
                return false;
            }

            return true;
        }
    }