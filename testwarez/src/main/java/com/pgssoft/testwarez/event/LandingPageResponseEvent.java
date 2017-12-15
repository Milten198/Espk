package com.pgssoft.testwarez.event;

import com.pgssoft.testwarez.networking.response.LandingPageResponse;

/**
 * Created by dpodolak on 23.03.16.
 */
public class LandingPageResponseEvent {

    LandingPageResponse response;

    public LandingPageResponseEvent(LandingPageResponse response) {
        this.response = response;
    }

    public LandingPageResponse getResponse() {
        return response;
    }
}
