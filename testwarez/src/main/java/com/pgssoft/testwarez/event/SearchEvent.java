package com.pgssoft.testwarez.event;

/**
 * Created by rtulaza on 2015-08-14.
 */
public class SearchEvent {
    public enum SEARCH_TYPE {
        ALL, FAVORITE, SPEAKERS, ORGANIZER_MESSAGE, ARCHIVE_EVENTS, ARCHIVE_VIDEOS
    }

    public String query;
    public SEARCH_TYPE type;

    public SearchEvent(String query, SEARCH_TYPE type) {
        this.query = query;
        this.type = type;
    }
}
