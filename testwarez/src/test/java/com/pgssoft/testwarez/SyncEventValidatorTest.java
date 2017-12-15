package com.pgssoft.testwarez;

import com.pgssoft.testwarez.event.SyncDataEvent;
import com.pgssoft.testwarez.utils.SyncEventValidator;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dpodolak on 04.07.16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class SyncEventValidatorTest {

    @Test
    public void mainTest(){
        List<SyncDataEvent> syncDataEvents = new ArrayList<>();
        syncDataEvents.add(new SyncDataEvent(SyncDataEvent.START_SYNCING));
        syncDataEvents.add(new SyncDataEvent(SyncDataEvent.START_SYNC_ACTIVE_CONFERENCE));
        syncDataEvents.add(new SyncDataEvent(SyncDataEvent.DOWNLOAD_ACTIVE_CONFERENCE_CONTENT));
        syncDataEvents.add(new SyncDataEvent(SyncDataEvent.DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT));
        syncDataEvents.add(new SyncDataEvent(SyncDataEvent.DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT));
        syncDataEvents.add(new SyncDataEvent(SyncDataEvent.DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT));
        syncDataEvents.add(new SyncDataEvent(SyncDataEvent.DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT));
        syncDataEvents.add(new SyncDataEvent(SyncDataEvent.DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT));
        syncDataEvents.add(new SyncDataEvent(SyncDataEvent.DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT));
        syncDataEvents.add(new SyncDataEvent(SyncDataEvent.DOWNLOAD_ARCHIVE_CONFERENCE_CONTENT));
        syncDataEvents.add(new SyncDataEvent(SyncDataEvent.SYNC_ARCHIVE_COMPLETE));

        Assert.assertTrue(SyncEventValidator.isValid(syncDataEvents, SyncEventValidator.SYNC_ALL_PATTERN));
    }
}
