package com.pgssoft.testwarez.feature.messages.filter;

import com.pgssoft.testwarez.database.model.Message;
import com.pgssoft.testwarez.util.Utils;

import java.util.List;

import rx.functions.Func1;

public class MediaFilter implements Func1<Message, Boolean> {

        private List<Integer> socialFilter;

        public MediaFilter() {
            socialFilter = Utils.getSocialFilter();
        }

        @Override
        public Boolean call(Message message) {

            if (socialFilter.isEmpty()) {
                return true;
            }
            if (message.getMedium().equals(Message.FACEBOOK)
                    && socialFilter.contains(MessageFilterAdapter.FACEBOOK_ID)) {
                return true;
            } else if (message.getMedium().equals(Message.TWITTER)
                    && socialFilter.contains(MessageFilterAdapter.TWITTER_ID)) {
                return true;
            } else if (message.getMedium().equals(Message.ORGANIZER)
                    && socialFilter.contains(MessageFilterAdapter.ORGANIZER_ID)) {
                return true;
            }

            return false;
        }
    }