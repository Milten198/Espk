package com.pgssoft.testwarez.feature.messages.filter;

import com.pgssoft.testwarez.database.model.Message;

import rx.functions.Func1;

public class SearchFilter implements Func1<Message, Boolean> {

        private String query;

        public SearchFilter(String searchFilter) {
            this.query = searchFilter;
        }

        @Override
        public Boolean call(Message message) {

            if (message.getDescriptions().toLowerCase().contains(query.toLowerCase())) {
                return true;
            }

            return false;
        }
    }
