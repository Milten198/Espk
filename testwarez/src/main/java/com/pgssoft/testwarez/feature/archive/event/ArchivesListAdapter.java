package com.pgssoft.testwarez.feature.archive.event;

import android.content.Context;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.ClickOnFileEvent;
import com.pgssoft.testwarez.database.model.Archive;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by brosol on 2016-05-10.
 */
public class ArchivesListAdapter extends BaseAdapter {
    private Context context;
    private List<Archive> data;

    public ArchivesListAdapter(Context context, List<Archive> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Archive getItem(int i) {
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
            view = inflater.inflate(R.layout.archive_list_item, viewGroup, false);

            vh = new ViewHolder();
            vh.icon = ButterKnife.findById(view, R.id.archive_icon);
            vh.name = ButterKnife.findById(view, R.id.archive_name);

            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }

        final Archive item = getItem(i);

        vh.name.setText(Html.fromHtml(item.getName()));

        switch(item.getType()) {
            case Archive.TYPE_PDF:
                vh.icon.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.archive_pdf, null));
                break;
            case Archive.TYPE_DOCUMENT:
                vh.icon.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.archive_doc, null));
                break;
            case Archive.TYPE_PRESENTATION:
                vh.icon.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.archive_graph, null));
                break;
            case Archive.TYPE_LINK:
                vh.icon.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.archive_www, null));
                break;
            case Archive.TYPE_VIDEO:
                vh.icon.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.archive_video, null));
                break;
            case Archive.TYPE_UNDEFINED:
                vh.icon.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.archive, null));
                break;
            default:
                vh.icon.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.archive, null));
                break;
        }

        view.setOnClickListener(view1 -> {
            ApplicationController.getBus().post(new ClickOnFileEvent(item));
        });

        return view;
    }

    private class ViewHolder {
        public ImageView icon;
        public TextView name;
    }
}
