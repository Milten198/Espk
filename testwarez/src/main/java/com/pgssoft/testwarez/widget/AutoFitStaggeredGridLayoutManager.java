package com.pgssoft.testwarez.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.pgssoft.testwarez.util.Utils;

/**
 * Created by brosol on 2016-06-13.
 */
public class AutoFitStaggeredGridLayoutManager extends StaggeredGridLayoutManager {

    public static final int ITEM_WIDTH = 200;

    private Context context;

    public AutoFitStaggeredGridLayoutManager(int spanCount, int orientation, Context context) {
        super(spanCount, orientation);
        this.context = context;
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);

        if(Utils.isTablet(context)) {
            int width = View.MeasureSpec.getSize(widthSpec);
            float scaleFactor = context.getResources().getDisplayMetrics().density * ITEM_WIDTH;
            int columns = (int) (width / scaleFactor);

            setSpanCount(columns);
        }
    }
}
