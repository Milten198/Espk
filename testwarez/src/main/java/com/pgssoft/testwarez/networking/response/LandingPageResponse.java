package com.pgssoft.testwarez.networking.response;

import com.pgssoft.testwarez.database.model.LandingPageDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dpodolak on 23.03.16.
 */
public class LandingPageResponse {

    private int id;

    private List<LandingPageDescription> descriptions = new ArrayList<>();

    public List<LandingPageDescription> getDescriptions() {

        for (LandingPageDescription lpd: descriptions){
            lpd.setLandingPageId(id);
        }
        return descriptions;
    }

    public int getId() {
        return id;
    }
}
