package com.pgssoft.testwarez.feature.archive.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.ArchiveGalleryClickEvent;
import com.pgssoft.testwarez.database.model.GalleryBEFile;

import java.util.List;

import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by dpodolak on 12.04.16.
 */
public class GalleryAdapter extends PagerAdapter implements View.OnTouchListener {

    private Context context;
    private LayoutInflater inflater;
    private List<GalleryBEFile> imageList;

    public GalleryAdapter(Context context, List<GalleryBEFile> imageList) {
        this.context = context;
        this.imageList = imageList;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View page = inflater.inflate(R.layout.gallery_page, null);

        PhotoView photoView = ButterKnife.findById(page, R.id.gallery_photo);
        photoView.setZoomable(true);
        ProgressBar progressBar = ButterKnife.findById(page, R.id.gallery_progress_bar);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(context, R.color.primary), PorterDuff.Mode.SRC_ATOP);
        View clickHandler = ButterKnife.findById(page, R.id.click_handler);

        ImageLoader.getInstance().displayImage(
                context.getResources().getString(R.string.endpoint) +
                        context.getResources().getString(R.string.images_url_default, imageList.get(position).getImage().getFileId()),
                photoView, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        progressBar.setVisibility(View.GONE);
                    }
                });

        clickHandler.setOnTouchListener(this);
        container.addView(page);

        return page;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private RelativeLayout getRelativeLayout() {
        RelativeLayout layout = new RelativeLayout(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(params);

        return layout;
    }

    private void setProgressLayoutParams(ProgressBar progressBar) {
        RelativeLayout.LayoutParams progressParams =
                (RelativeLayout.LayoutParams) progressBar.getLayoutParams();
        progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(progressParams);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        ApplicationController.getBus().post(new ArchiveGalleryClickEvent());
        return false;
    }
}
