package com.pgssoft.testwarez.feature.plan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.event.ShowFloorEvent;
import com.pgssoft.testwarez.database.model.BuildingPlan;
import com.pgssoft.testwarez.util.SimpleImageLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by dpodolak on 29.02.16.
 */
public class MapBuildingAdapter extends RecyclerView.Adapter<MapBuildingAdapter.BuildingViewHolder> {

    private LayoutInflater inflater;
    private Context context;
    private List<BuildingPlan> buildingPlanList = new ArrayList<>();

    ImageLoader loader;
    DisplayImageOptions mainOptions;


    public MapBuildingAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);

        loader = ImageLoader.getInstance();

        final int divideFactor = context.getResources().getBoolean(R.bool.isTablet) ? 1 : 2;

        mainOptions = new DisplayImageOptions.Builder()
                .preProcessor(bitmap -> Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / divideFactor, bitmap.getHeight() / divideFactor, false))
                .build();
    }

    public void setPlanList(List<BuildingPlan> buildingPlanList) {
        this.buildingPlanList.clear();
        this.buildingPlanList.addAll(buildingPlanList);
        notifyDataSetChanged();
    }

    @Override
    public BuildingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BuildingViewHolder holder = new BuildingViewHolder(inflater.inflate(R.layout.item_building_map, parent, false), context);

        return holder;
    }

    @Override
    public void onBindViewHolder(final BuildingViewHolder holder, int position) {
        final BuildingPlan buildingPlan = buildingPlanList.get(position);

        holder.itemView.setOnClickListener(v -> {
            Timber.i("BuildingPlan: " + (buildingPlan.getPlan() == null ? "false" : "true"));
            ApplicationController.getBus().post(new ShowFloorEvent(holder.buildMap, buildingPlan));
        });

        holder.name.setText(Html.fromHtml(buildingPlan.getName()));

        loader.displayImage(context.getResources().getString(R.string.endpoint) +
                        context.getResources().getString(R.string.images_url_default,
                                buildingPlan.getPlan().getFileId()), holder.buildMap, mainOptions,
                new SimpleImageLoader() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        holder.loader.setVisibility(View.GONE);
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            float ratio = ((float) loadedImage.getHeight()) / loadedImage.getWidth();
                            int height = (int) (ratio * loadedImage.getWidth());
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.buildMap.getLayoutParams();
                            params.height = height;
                            holder.buildMap.setLayoutParams(params);
                        }
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        holder.loader.setVisibility(View.VISIBLE);
                    }

                });
    }

    @Override
    public int getItemCount() {
        return buildingPlanList.size();
    }


    public static class BuildingViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tvItemBuildingMapFloorNumber)
        protected TextView name;

        @Bind(R.id.ivItemBuildingMap)
        protected ImageView buildMap;

        @Bind(R.id.pbItemBuildingMap)
        protected ProgressBar loader;

        public BuildingViewHolder(View itemView, Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            loader.getIndeterminateDrawable().setColorFilter(
                    ContextCompat.getColor(context, R.color.primary), PorterDuff.Mode.SRC_ATOP);
        }
    }

}
