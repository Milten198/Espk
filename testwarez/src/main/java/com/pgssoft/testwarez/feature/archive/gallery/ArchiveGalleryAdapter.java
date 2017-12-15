package com.pgssoft.testwarez.feature.archive.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.OpenGalleryImageEvent;
import com.pgssoft.testwarez.database.model.Conference;
import com.pgssoft.testwarez.database.model.Gallery;
import com.pgssoft.testwarez.database.model.GalleryBEFile;
import com.pgssoft.testwarez.widget.AutoFitStaggeredGridLayoutManager;
import com.pgssoft.testwarez.util.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by brosol on 2016-03-29.
 */
public class ArchiveGalleryAdapter extends ExpandableStaggeredRecyclerAdapter<ArchiveGalleryAdapter.ListItem> implements View.OnClickListener {

    List<GalleryBEFile> images = new ArrayList<>();
    Context context;

    private DisplayImageOptions displayImageOptions;
    private Subscription conferenceSubscription;
    private float screenDensity;
    private final double ITEM_HEIGHT_RATIO_NORMAL = 0.9;
    private final double ITEM_HEIGHT_RATIO_EXTENDED = 1.4;

    private final Observable<ListItem> itemListObservable = Observable.defer(() -> Observable.from(getArchiveConferences()))
            .toSortedList((conference, conference2) -> new Long(conference2.getStartAt().getMillis()).compareTo(conference.getStartAt().getMillis()))
            .flatMap(Observable::from)
            .map(conference -> new ListItem(conference))
            .flatMap(listItem -> {
                Observable<ListItem> imageOB = Observable.from(getImages(listItem.conference))
                        .map(image -> new ListItem(image));

                Observable<ListItem> confOB = Observable.just(listItem);

                return imageOB.count().filter(integer -> integer > 0).flatMap(i -> Observable.merge(confOB, imageOB));
            });

    private List<GalleryBEFile> getImages(Conference conference) {
        List<GalleryBEFile> imageList = new ArrayList<>();

        try {
            QueryBuilder<Gallery, Integer> qbGallery = ApplicationController.getDatabaseHelper().getGalleriesDao().queryBuilder();
            qbGallery.where().eq(Gallery.CONFERENCE_ID, conference.getId());
            List<Gallery> galleries = new ArrayList<>();
            galleries.addAll(qbGallery.query());

            if (galleries.size() == 0) {
                return new ArrayList<>();
            }

            QueryBuilder<GalleryBEFile, Integer> qb = ApplicationController.getDatabaseHelper().getGalleryFilesDao().queryBuilder();
            Where<GalleryBEFile, Integer> where = qb.where();

            for (int i = 0; i < galleries.size(); i++) {
                Gallery gallery = galleries.get(i);
                where.eq(GalleryBEFile.GALLERY_ID, gallery.getId());
                if (i != galleries.size() - 1) {
                    where.or();
                }
            }

            imageList.addAll(qb.query());
            images.addAll(imageList);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!imageList.isEmpty()) {
            imagesCounterPerConference.add(imageList.size());
        }

        return imageList;
    }

    private void loadItems() {

        Observable<ListItem> presenterObservable = itemListObservable;

        conferenceSubscription = presenterObservable
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setItems, Throwable::printStackTrace,
                        this::expandAll);

    }

    public ArchiveGalleryAdapter(Context context) {
        super(context);

        loadItems();

        this.context = context;
        screenDensity = context.getResources().getDisplayMetrics().density;

        displayImageOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();
    }

    @Override
    public ExpandableStaggeredRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new ConferenceViewHolder(inflate(R.layout.item_archive_conference_header, parent));
            default:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.archive_gallery_item, parent, false);
                ImageViewHolder viewHolder = new ImageViewHolder(itemView);
                itemView.setTag(viewHolder);
                itemView.setOnClickListener(this);
                return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(ExpandableStaggeredRecyclerAdapter.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(true);
                holder.itemView.setLayoutParams(layoutParams);
                ((ConferenceViewHolder) holder).bind(position);
                break;
            case TYPE_ITEM:
                new Handler().postDelayed(() -> {
                    ViewGroup.LayoutParams params = ((ImageViewHolder) holder).layout.getLayoutParams();
                    ((ImageViewHolder) holder).image.post(() -> loadImage(params, (ImageViewHolder) holder, position));
                }, 50);
        }
    }

    private void loadImage(ViewGroup.LayoutParams params, ImageViewHolder holder, int position) {

        String imageUrl = context.getResources().getString(R.string.endpoint) + getImageUrl(position, holder.image);
        int itemWidth = (int) (screenDensity * AutoFitStaggeredGridLayoutManager.ITEM_WIDTH);

        params.height = (int) (itemWidth * ITEM_HEIGHT_RATIO_NORMAL);
        holder.layout.setLayoutParams(params);

        if (!imageUrl.isEmpty() && (Utils.isInternetConnection(context) || Utils.isImageInCache(imageUrl))) {
            ImageLoader.getInstance().displayImage(
                    imageUrl, holder.image, displayImageOptions, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            holder.progressBar.setVisibility(View.GONE);

                            if (loadedImage.getHeight() > loadedImage.getWidth()) {
                                params.height = (int) (itemWidth * ITEM_HEIGHT_RATIO_EXTENDED);
                                holder.layout.setLayoutParams(params);
                            }
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            holder.progressBar.setVisibility(View.GONE);
                            holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.no_image));
                        }
                    });
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.no_image));
        }
    }

    private String getImageUrl(int position, ImageView imageView) {
        String imageUrl;
        GalleryBEFile image = visibleItems.get(position).image;
        imageView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        if (image.getImage().getWidth() != 0 && image.getImage().getHeight() != 0) {
            if (image.getImage().getWidth() >= image.getImage().getHeight()) {
                imageUrl = context.getResources().getString(R.string.images_url_custom_size, image.getImage().getFileId(), imageView.getWidth(), "width");
            } else {
                imageUrl = context.getResources().getString(R.string.images_url_custom_size, image.getImage().getFileId(), imageView.getHeight(), "height");
            }
        } else {
            imageUrl = context.getResources().getString(R.string.images_url_default, image.getImage().getFileId());
        }

        return imageUrl;
    }

    @Override
    public void onClick(View v) {
        ImageViewHolder holder = (ImageViewHolder) v.getTag();
        int position = holder.getAdapterPosition();

        List<GalleryBEFile> imageList = new ArrayList<>();
        int counter = 0;
        int offset = 0;
        int end = 0;

        for (int i = 0; i < imagesCounterPerConference.size(); i++) {
            if (headersMap.valueAt(i) == 1) {
                counter += imagesCounterPerConference.get(i);
            } else {
                offset += imagesCounterPerConference.get(i);
            }

            end += imagesCounterPerConference.get(i);

            if (counter >= (position - i)) {
                int start = end - imagesCounterPerConference.get(i);
                imageList = images.subList(start, end);
                position += offset;
                position -= (start + i);
                break;
            }
        }

        if (imageList.size() != 0) {
            ApplicationController.getBus().post(new OpenGalleryImageEvent(imageList, position));
        }
    }

    private List<Conference> getArchiveConferences() {
        List<Conference> conferenceList = new ArrayList<>();
        try {
            QueryBuilder<Conference, Integer> conferenceQB = ApplicationController.getDatabaseHelper().getConferenceDao().queryBuilder();
            Where where = conferenceQB.where();
            where.eq(Conference.STATUS_COLUMN, Conference.CONFERENCE_ARCHIVE);
            conferenceList.addAll(conferenceQB.query());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conferenceList;
    }

    public void unSubscribe() {
        conferenceSubscription.unsubscribe();
    }

    public void update() {
        loadItems();
    }

    public class ConferenceViewHolder extends ExpandableStaggeredRecyclerAdapter.HeaderViewHolder {

        @Bind(R.id.tvItemArchiveConferenceHeaderTitle)
        protected TextView title;

        public ConferenceViewHolder(View view) {
            super(view, ButterKnife.findById(view, R.id.ivItemArchiveConferenceHeaderArrow));
            ButterKnife.bind(this, view);
        }

        @Override
        public void bind(int position) {
            super.bind(position);
            title.setText(Html.fromHtml(visibleItems.get(position).conference.getName()));
        }
    }

    public class ImageViewHolder extends ExpandableStaggeredRecyclerAdapter.ViewHolder {

        @Bind(R.id.rvGalleryLayout)
        protected RelativeLayout layout;
        @Bind(R.id.rvGalleryItem)
        protected ImageView image;
        @Bind(R.id.rvGalleryProgressBar)
        protected ProgressBar progressBar;

        public ImageViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            progressBar.getIndeterminateDrawable().setColorFilter(
                    ContextCompat.getColor(context, R.color.primary), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static class ListItem extends ExpandableStaggeredRecyclerAdapter.ListItem {

        public Conference conference;
        public GalleryBEFile image;

        public ListItem(Conference conference) {
            super(TYPE_HEADER);
            this.conference = conference;
        }

        public ListItem(GalleryBEFile image) {
            super(TYPE_ITEM);
            this.image = image;
        }

        @Override
        public String toString() {
            if (conference != null) {
                return "conference: " + conference.getName();
            } else if (image != null) {
                return "image: " + image.getId();
            }

            return "null";
        }
    }
}
