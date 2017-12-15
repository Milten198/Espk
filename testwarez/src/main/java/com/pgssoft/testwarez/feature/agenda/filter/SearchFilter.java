package com.pgssoft.testwarez.feature.agenda.filter;

import com.pgssoft.testwarez.feature.agenda.all.AgendaAdapter;

import rx.functions.Func1;

public class SearchFilter implements Func1<AgendaAdapter.ListItem, Boolean> {

        String query;

        public SearchFilter(String query) {
            this.query = query;
        }

        @Override
        public Boolean call(AgendaAdapter.ListItem listItem) {
            if (listItem.getEvent() != null) {
                return listItem.getEvent().getTitle().toLowerCase().contains(query.toLowerCase());
            } else if (listItem.getCategory() != null) {
                return listItem.getCategory().getTitle().toLowerCase().contains(query.toLowerCase());
            }
            return true;
        }
    }