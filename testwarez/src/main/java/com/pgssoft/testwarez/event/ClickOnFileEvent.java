package com.pgssoft.testwarez.event;

import com.pgssoft.testwarez.database.model.Archive;

/**
 * Created by brosol on 2016-05-12.
 */
public class ClickOnFileEvent {

    private Archive archive;

    public ClickOnFileEvent(Archive archive) {
        this.archive = archive;
    }

    public Archive getArchive() {
        return archive;
    }
}
