package com.pgssoft.testwarez.event;

import com.pgssoft.testwarez.database.model.Video;

import java.util.List;

/**
 * Created by dpodolak on 19.04.16.
 */
public class ArchiveVideoClickEvent {

    Video video;

    int position;

    List<Video> videoList;

    public ArchiveVideoClickEvent(Video video) {
        this.video = video;
    }

    public ArchiveVideoClickEvent(List<Video> videoList, int position) {
        this.position = position;
        this.videoList = videoList;
    }

    public int getPosition() {
        return position;
    }

    public List<Video> getVideoList() {
        return videoList;
    }

    public Video getVideo() {
        return video != null ? video : videoList.get(position);
    }
}
