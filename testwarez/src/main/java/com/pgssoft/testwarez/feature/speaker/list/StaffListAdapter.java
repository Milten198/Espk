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
import com.pgssoft.testwarez.database.model.Staff;
import com.pgssoft.testwarez.database.model.StaffDescription;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by brosol on 2016-03-25.
 */
public class StaffListAdapter extends BaseAdapter {
    private Context context;
    private List<Staff> data;

    public StaffListAdapter(Context context, List<Staff> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Staff getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder vh;

        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.speaker_list_item, viewGroup, false);

            vh = new ViewHolder();
            vh.photo = ButterKnife.findById(view, R.id.speaker_photo);
            vh.name = ButterKnife.findById(view, R.id.speaker_name);
            vh.company = ButterKnife.findById(view, R.id.speaker_company);

            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }

        final Staff item = getItem(i);

        int id = -1;

        if (item.getPhoto() != null) {
            id = item.getPhoto().getFileId();
        }

        ImageLoader.getInstance().displayImage(context.getResources().getString(R.string.endpoint) +
                        context.getResources().getString(R.string.images_url_default, id), vh.photo,
                new DisplayImageOptions.Builder()
                        .showImageForEmptyUri(R.drawable.user_placeholder)
                        .showImageOnFail(R.drawable.user_placeholder)
                        .showImageOnLoading(R.drawable.user_placeholder)
                        .build());
        vh.name.setText(Html.fromHtml(item.getName()));

        vh.company.setText("");
        StaffDescription staffDescription = item.getDescription();
        if(staffDescription != null) {
            boolean isJobNotEmpty = staffDescription.getJob() != null && !staffDescription.getJob().isEmpty();
            boolean isCompanyNotEmpty = staffDescription.getCompany() != null && !staffDescription.getCompany().isEmpty();

            if(isJobNotEmpty || isCompanyNotEmpty) {
                vh.company.setVisibility(View.VISIBLE);
            } else {
                vh.company.setVisibility(View.GONE);
            }

            if (isJobNotEmpty) {
                vh.company.setText(Html.fromHtml(staffDescription.getJob()) + ", ");
            }
            if(isCompanyNotEmpty) {
                vh.company.append(staffDescription.getCompany());
            }
        } else {
            vh.company.setVisibility(View.GONE);
        }

        boolean isPhoneNotEmpty = item.getPhone() != null && !item.getPhone().isEmpty();
        boolean isFacebookNotEmpty = item.getFacebook() != null && !item.getFacebook().isEmpty();
        boolean isEmailNotEmpty = item.getEmail() != null && !item.getEmail().isEmpty();
        boolean isLinkedinNotEmpty = item.getLinkedin() != null && !item.getLinkedin().isEmpty();
        boolean isSkypeNotEmpty = item.getSkype() != null && !item.getSkype().isEmpty();
        boolean isTwitterNotEmpty = item.getTwitter() != null && !item.getTwitter().isEmpty();

        if(isPhoneNotEmpty || isFacebookNotEmpty || isEmailNotEmpty ||
                isLinkedinNotEmpty || isSkypeNotEmpty || isTwitterNotEmpty)
            view.setOnClickListener(view1 -> {
                OpenPersonEvent event = new OpenPersonEvent(item);
                event.setPhoto(vh.photo);
                event.setName(vh.name);
                ApplicationController.getBus().post(event);

            });

        return view;
    }

    private class ViewHolder {
        public ImageView photo;
        public TextView name;
        public TextView company;
    }
}
