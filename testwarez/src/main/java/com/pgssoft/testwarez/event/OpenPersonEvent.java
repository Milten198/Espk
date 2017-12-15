package com.pgssoft.testwarez.event;

import android.widget.ImageView;
import android.widget.TextView;

import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.database.model.Staff;

/**
 * Created by rtulaza on 2015-08-17.
 */
public class OpenPersonEvent {
    public Speaker speaker = null;
    public Staff staff = null;
    private ImageView photo;
    private TextView name;
    private boolean isArchive;

    public OpenPersonEvent(Speaker speaker) {
        this.speaker = speaker;
    }

    public OpenPersonEvent(Staff staff) {
        this.staff = staff;
    }

    public ImageView getPhoto() {
        return photo;
    }

    public void setPhoto(ImageView photo) {
        this.photo = photo;
    }

    public TextView getName() {
        return name;
    }

    public void setName(TextView name) {
        this.name = name;
    }

    public boolean isArchive() {
        return isArchive;
    }

    public void setIsArchive(boolean isArchive) {
        this.isArchive = isArchive;
    }
}
