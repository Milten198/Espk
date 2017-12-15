package com.pgssoft.testwarez.event;

import com.pgssoft.testwarez.database.model.GalleryBEFile;

import java.util.List;

/**
 * Created by dpodolak on 31.05.16.
 */
public class OpenGalleryImageEvent {

    private List<GalleryBEFile> imageList;
    private int position;

    public OpenGalleryImageEvent(List<GalleryBEFile> imageList, int position) {
        this.imageList = imageList;
        this.position = position;
    }

    public List<GalleryBEFile> getImageList() {
        return imageList;
    }

    public int getPosition() {
        return position;
    }
}
