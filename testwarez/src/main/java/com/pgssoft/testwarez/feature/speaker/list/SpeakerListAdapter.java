package com.pgssoft.testwarez.feature.speaker.list;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.OpenPersonEvent;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.database.model.SpeakerDescription;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by rtulaza on 2015-08-05.
 */
public class SpeakerListAdapter extends BaseAdapter {
    private Context context;
    private List<Speaker> data;
    private boolean isArchive;

    private DisplayImageOptions options;

    public SpeakerListAdapter(Context context, List<Speaker> data, boolean isArchive) {
        this.context = context;
        this.data = data;
        this.isArchive = isArchive;

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.user_placeholder)
                .showImageOnFail(R.drawable.user_placeholder)
                .showImageOnLoading(R.drawable.user_placeholder)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Speaker getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder vh;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.speaker_list_item, viewGroup, false);

            vh = new ViewHolder();
            vh.photo = ButterKnife.findById(view, R.id.speaker_photo);
            vh.name = ButterKnife.findById(view, R.id.speaker_name);
            vh.company = ButterKnife.findById(view, R.id.speaker_company);
            vh.separator = ButterKnife.findById(view, R.id.vSpeakerListItemSeparator);

            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }

        final Speaker item = getItem(position);

        if (item != null) {
            int id = -1;

            if (item.getPhoto() != null) {
                id = item.getPhoto().getFileId();
            }

            ImageLoader.getInstance().displayImage(context.getResources().getString(R.string.endpoint) +
                    context.getResources().getString(R.string.images_url_default, id), vh.photo, options);

            vh.name.setText(Html.fromHtml(item.getFullName()));

            vh.company.setText("");
            SpeakerDescription speakerDescription = item.getDescription();
            if (speakerDescription != null) {
                boolean isJobNotEmpty = speakerDescription.getJob() != null && !speakerDescription.getJob().isEmpty();
                boolean isCompanyNotEmpty = speakerDescription.getCompany() != null && !speakerDescription.getCompany().isEmpty();

                if (isJobNotEmpty || isCompanyNotEmpty) {
                    vh.company.setVisibility(View.VISIBLE);
                } else {
                    vh.company.setVisibility(View.GONE);
                }

                if (isJobNotEmpty) {
                    vh.company.setText(Html.fromHtml(speakerDescription.getJob()));
                }
                if (isJobNotEmpty && isCompanyNotEmpty) {
                    vh.company.append(", ");
                }
                if (isCompanyNotEmpty) {
                    vh.company.append(speakerDescription.getCompany());
                }
            } else {
                vh.company.setVisibility(View.GONE);
            }

            view.setOnClickListener(view1 -> {
                OpenPersonEvent event = new OpenPersonEvent(item);
                event.setPhoto(vh.photo);
                event.setName(vh.name);
                event.setIsArchive(isArchive);
                ApplicationController.getBus().post(event);
            });
        }

        return view;
    }

    private static class ViewHolder {
        public ImageView photo;
        public TextView name;
        public TextView company;
        public View separator;
    }
}
