package com.pgssoft.testwarez.database.model;

import android.content.Context;
import android.support.annotation.StringDef;
import android.webkit.MimeTypeMap;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.pgssoft.testwarez.R;

import org.joda.time.DateTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by brosol on 2016-05-10.
 */
@DatabaseTable(tableName = "archives")
public class Archive {

    public static final String TYPE_PDF = "pdf";
    public static final String TYPE_DOCUMENT = "document";
    public static final String TYPE_PRESENTATION = "presentation";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_LINK = "www";
    public static final String TYPE_UNDEFINED = "undefined";

    public static final String COLUMN_TYPE = "type";
    public static final String CONFERENCE_ID = "conferenceId";

    @StringDef({TYPE_PDF, TYPE_DOCUMENT, TYPE_PRESENTATION, TYPE_VIDEO, TYPE_LINK, TYPE_UNDEFINED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    @DatabaseField(id = true)
    private int id;

    @DatabaseField(foreign = true, columnName = CONFERENCE_ID)
    private Conference conference;

    @DatabaseField
    private String name;

    @DatabaseField
    private String lang;

    @DatabaseField(columnName = COLUMN_TYPE)
    private String type;

    @DatabaseField(foreign = true)
    private BEFile file;

    @DatabaseField
    private String url;

    @SerializedName("created_at")
    @DatabaseField
    private DateTime createdAt;

    @SerializedName("updated_at")
    @DatabaseField
    private DateTime updatedAt;

    @ForeignCollectionField(eager = false)
    private Collection<EventToArchives> eventToArchives = new ArrayList<>();

    public Archive() {
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLang() {
        return lang;
    }

    @Type
    public String getType() {
        return type;
    }

    public BEFile getFile() {
        return file;
    }

    public String getFileName() {
        if (name == null) {
            return "";
        }

        return name.substring(name.lastIndexOf(".") + 1);
    }

    public String getFileNameWithExtension(Context context) {
        String result;

        switch (type) {
            case Archive.TYPE_PDF:
                result = getFileName() + "." + getExtension();
                break;
            case Archive.TYPE_DOCUMENT:
                result = getFileName() + "." + getExtension();
                break;
            case Archive.TYPE_PRESENTATION:
                result = getFileName() + "." + getExtension();
                break;
            case Archive.TYPE_LINK:
                result = getFileName();
                break;
            case Archive.TYPE_VIDEO:
                result = getFileName() + "." + getExtension();
                break;
            default:
                result = getFileName();
                break;
        }

        return result;
    }

    public String getExtension() {
        if (file == null) {
            if (getUrl() != null && !getUrl().isEmpty()) {
                return MimeTypeMap.getFileExtensionFromUrl(getUrl());
            } else {
                return "";
            }
        }

        return MimeTypeMap.getFileExtensionFromUrl(file.getName());
    }

    public String getMimeType() {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension().toLowerCase());
    }

    public String getUrl() {
        return url;
    }

    public String getFullUrl(Context context) {
        if (getFile() != null) {
            return context.getResources().getString(R.string.endpoint) + "api/v2/attachments/" + getFile().getFileId();
        } else if (getUrl() != null && !getUrl().toLowerCase().startsWith("http")) {
            return "http://" + getUrl().replaceAll(" ", "%20");
        } else if (getUrl() != null) {
            return getUrl().replaceAll(" ", "%20");
        } else {
            return "";
        }
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public Conference getConference() {
        return conference;
    }

    public void setConference(Conference conference) {
        this.conference = conference;
    }

    public List<EventToArchives> getEventToArchives() {
        return new ArrayList<>(eventToArchives);
    }

    public ArrayList<Event> getAllEvents() {
        ArrayList<Event> result = new ArrayList<>();
        for (EventToArchives eta : eventToArchives) {
            result.add(eta.getEvent());
        }
        return result;
    }

    public int getIntType() {
        switch (type) {
            case TYPE_PDF:
                return 0;
            case TYPE_DOCUMENT:
                return 1;
            case TYPE_PRESENTATION:
                return 2;
            case TYPE_VIDEO:
                return 3;
            case TYPE_LINK:
                return 4;
            default:
                return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Archive archive = (Archive) o;

        return id == archive.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
