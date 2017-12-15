package com.pgssoft.testwarez.feature.archive.filter;

import com.pgssoft.testwarez.feature.archive.video.ArchiveVideoAdapter;

import rx.functions.Func1;

/**
 * Created by dpodolak on 16.05.16.
 */
public class VideoSearchFilter implements Func1<ArchiveVideoAdapter.ListItem, Boolean> {

    private String query;

    public VideoSearchFilter(String query) {
        this.query = query;
    }

    @Override
    public Boolean call(ArchiveVideoAdapter.ListItem video) {

        if (video != null && video.getVideo()!= null){
            return video.getVideo().getName().toLowerCase().contains(query.toLowerCase());
        }

        return true;
    }
}
