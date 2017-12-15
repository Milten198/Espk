package com.pgssoft.testwarez.feature.archive.gallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.event.ShowGalleryRecycler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brosol on 2016-04-20.
 */
public abstract class ExpandableStaggeredRecyclerAdapter <T extends ExpandableStaggeredRecyclerAdapter.ListItem> extends RecyclerView.Adapter<ExpandableStaggeredRecyclerAdapter.ViewHolder> {

    protected Context context;
    public List<T> allItems = new ArrayList<>();
    public List<T> visibleItems = new ArrayList<>();
    public SparseIntArray headersMap = new SparseIntArray();
    List<Integer> imagesCounterPerConference = new ArrayList<>();

    public static final int TYPE_HEADER = 100;
    public static final int TYPE_ITEM = 200;

    public ExpandableStaggeredRecyclerAdapter(Context context) {
        this.context = context;
    }

    protected View inflate(int resourceID, ViewGroup viewGroup) {
        return LayoutInflater.from(this.context).inflate(resourceID, viewGroup, false);
    }

    public void setItems(List<T> items) {
        allItems = items;
        headersMap.clear();
        visibleItems.clear();

        for(int i = 0; i < items.size(); i++) {
            if (items.get(i).itemType == TYPE_HEADER) {
                visibleItems.add(items.get(i));
                headersMap.put(visibleItems.size() - 1, 0);
            }
        }

        if(visibleItems.size() != 0) {
            ApplicationController.getBus().post(new ShowGalleryRecycler(true));
        } else {
            ApplicationController.getBus().post(new ShowGalleryRecycler(false));
        }

        notifyDataSetChanged();
    }

    public boolean toggleExpandedItems(int position) {
        if(isExpanded(position)) {
            collapseItems(position);
            return false;
        } else {
            expandItems(position);
            return true;
        }
    }

    public void expandItems(int position) {
        int count = 0;
        int insert = position;
        int allItemsPosition = getAllItemsPosition(position);

        for(int i = allItemsPosition + headersMap.indexOfKey(position) + 1; i < allItems.size() && allItems.get(i).itemType != TYPE_HEADER; i++) {
            ++insert;
            ++count;
            visibleItems.add(insert, allItems.get(i));
        }

        notifyItemRangeInserted(position + 1, count);
        updateHeadersMap(position, count, false);
        this.notifyItemChanged(position);
    }

    public void collapseItems(int position) {
        int count = 0;

        while((position + 1) < visibleItems.size() && visibleItems.get(position + 1).itemType != TYPE_HEADER) {
            ++count;
            visibleItems.remove(position + 1);
        }

        notifyItemRangeRemoved(position + 1, count);
        updateHeadersMap(position, count, true);
        this.notifyItemChanged(position);
    }

    public void expandAll() {
        for(int i = visibleItems.size() - 1; i >= 0; --i) {
            if(getItemViewType(i) == TYPE_HEADER && !isExpanded(i)) {
                expandItems(i);
            }
        }
    }

    private int getAllItemsPosition(int position) {
        int allPosition = 0;

        for(int i = 0; i < headersMap.indexOfKey(position); i++) {
            allPosition += imagesCounterPerConference.get(i);
        }

        return allPosition;
    }

    private void updateHeadersMap(int position, int count, boolean collapse) {
        if(collapse) {
            updateHeadersMapIfCollapsed(position, count);
        } else {
            updateHeadersMapIfExpanded(position, count);
        }
    }

    private void updateHeadersMapIfCollapsed(int position, int count) {
        headersMap.put(position, 0);
        for(int i = headersMap.indexOfKey(position) + 1; i < headersMap.size() ; i++) {
            int value = headersMap.keyAt(i) - count;
            int expanded = headersMap.valueAt(i);
            headersMap.removeAt(i);
            headersMap.put(value, expanded);
        }
    }

    private void updateHeadersMapIfExpanded(int position, int count) {
        headersMap.put(position, 1);
        for(int i = headersMap.size() - 1; i > headersMap.indexOfKey(position); i--) {
            int value = headersMap.keyAt(i) + count;
            int expanded = headersMap.valueAt(i);
            headersMap.removeAt(i);
            headersMap.put(value, expanded);
        }
    }

    protected boolean isExpanded(int position) {
        return headersMap.get(position) == 1;
    }

    public int getItemViewType(int position) {
        return visibleItems.get(position).itemType;
    }

    public int getItemCount() {
        return visibleItems.size();
    }

    public static void openArrow(View view) {
        view.animate().setDuration(150L).rotation(0.0F);
    }

    public static void closeArrow(View view) {
        view.animate().setDuration(150L).rotation(180.0F);
    }

    public class HeaderViewHolder extends ExpandableStaggeredRecyclerAdapter<T>.ViewHolder {
        ImageView arrow;

        public HeaderViewHolder(View view, ImageView arrow) {
            super(view);
            this.arrow = arrow;
            view.setOnClickListener(v -> HeaderViewHolder.this.onClick());
        }

        protected void onClick() {
            if (toggleExpandedItems(getLayoutPosition())) {
                ExpandableStaggeredRecyclerAdapter.openArrow(this.arrow);
            } else {
                ExpandableStaggeredRecyclerAdapter.closeArrow(this.arrow);
            }

        }

        public void bind(int position) {
            this.arrow.setRotation(ExpandableStaggeredRecyclerAdapter.this.isExpanded(position)?0.0F:180.0F);
        }
    }

    public class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }

    public static class ListItem {
        public int itemType;

        public ListItem(int itemType) {
            this.itemType = itemType;
        }
    }
}
