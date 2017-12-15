package com.pgssoft.testwarez.notification;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.RemoteViews;

import com.pgssoft.testwarez.R;

import org.joda.time.DateTime;

/**
 * Created by dpodolak on 22.06.16.
 *
 * BuilderPattern is used in this class
 */
@SuppressLint("ParcelCreator")
public class ExpandedRMNotification extends RemoteViews{

    public ExpandedRMNotification(String packageName) {
        super(packageName, R.layout.notification_expanded_layout);
        setTextViewText(R.id.tvNotificationTime, new DateTime().toString("HH:mm"));
    }

    private void setContentText(String contentText) {
        setTextViewText(android.R.id.content, contentText);
    }

    private void setSummary(String summary) {
        setViewVisibility(R.id.vNotificationSeparator, View.VISIBLE);
        setViewVisibility(android.R.id.summary, View.VISIBLE);
        setTextViewText(android.R.id.summary, summary);
    }

    public static class Builder{

        private StringBuilder contentText = new StringBuilder();
        private String summaryText;
        private String packageName;

        public Builder(String packageName) {
            this.packageName = packageName;
        }

        public Builder addContentLine(String line) {
            contentText.append(line).append("\n");
            return this;
        }

        public Builder setSummary(String summaryText) {
            this.summaryText = summaryText;
            return this;
        }

        public ExpandedRMNotification build(){
            ExpandedRMNotification expandedRMNotification = new ExpandedRMNotification(packageName);
            expandedRMNotification.setContentText(contentText.toString());

            if(summaryText != null){
                expandedRMNotification.setSummary(summaryText);
            }
            return expandedRMNotification;
        }
    }
}
