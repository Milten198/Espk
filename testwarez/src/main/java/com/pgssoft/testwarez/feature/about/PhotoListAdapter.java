package com.pgssoft.testwarez.feature.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.database.model.companies.CompanyEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by rtulaza on 2015-08-05.
 */
public class PhotoListAdapter extends BaseAdapter {
    private Context context;
    private List<CompanyEntity> companyList = new ArrayList<>();
    private DisplayImageOptions options;

    public PhotoListAdapter(Context context) {
        this.context = context;
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.partner_placeholder)
                .showImageOnFail(R.drawable.partner_placeholder)
                .showImageOnLoading(R.drawable.partner_placeholder)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
    }

    public void setCompanyList(List<CompanyEntity> companyList) {
        this.companyList.clear();
        this.companyList.addAll(companyList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return companyList.size();
    }

    @Override
    public CompanyEntity getItem(int i) {
        return companyList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.fragment_photo_list_item, viewGroup, false);
            holder = new ViewHolder();
            holder.photo = ButterKnife.findById(convertView, R.id.photo);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CompanyEntity item = getItem(i);

        if (item.getCompany() != null && item.getCompany().getLogo() != null) {
            ImageLoader.getInstance().displayImage(context.getResources().getString(R.string.endpoint) +
                    context.getResources().getString(R.string.images_url_default, item.getCompany().getLogo().getFileId()), holder.photo, options);
        }

        convertView.setOnClickListener(v -> {
            if (item.getCompany() != null && !item.getCompany().getHomepage().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getCompany().getHomepage()));
                context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.company_list_chooser_title)));
            }
        });
        return convertView;
    }

    static class ViewHolder {
        ImageView photo;
    }
}
