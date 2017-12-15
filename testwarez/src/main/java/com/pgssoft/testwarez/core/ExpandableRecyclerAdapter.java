package com.pgssoft.testwarez.core;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://github.com/grennis/ExpandableRecyclerView/blob/master/lib/src/main/java/com/innodroid/expandablerecycler/ExpandableRecyclerAdapter.java">This class got from here.</a> A few values have been changed
 */
public abstract class ExpandableRecyclerAdapter<T extends ExpandableRecyclerAdapter.ListItem> extends Adapter<ExpandableRecyclerAdapter.ViewHolder> {
    public Context mContext;
    public List<T> allItems = new ArrayList();
    public List<T> visibleItems = new ArrayList();
    private List<Integer> indexList = new ArrayList();
    private SparseIntArray expandMap = new SparseIntArray();
    private int mode;
    public static final int TYPE_HEADER = 1000;
    public static final int TYPE_ITEM_CATEGORY = 1001;
    public static final int TYPE_ITEM_EVENT = 1002;
    private static final int ARROW_ROTATION_DURATION = 150;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_ACCORDION = 1;
    private ExpandItemsListener expandItemsListener;

    public ExpandableRecyclerAdapter(Context context) {
        this.mContext = context;
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public int getItemCount() {
        return this.visibleItems == null ? 0 : this.visibleItems.size();
    }

    protected View inflate(int resourceID, ViewGroup viewGroup) {
        return LayoutInflater.from(this.mContext).inflate(resourceID, viewGroup, false);
    }

    public boolean toggleExpandedItems(int position, boolean notify) {
        if (this.isExpanded(position)) {
            this.collapseItems(position, notify);
            return false;
        } else {
            this.expandItems(position, notify);
            if (this.mode == 1) {
                this.collapseAllExcept(position);
            }

            return true;
        }
    }

    public void expandItems(int position, boolean notify) {
        int count = 0;
        int index = this.indexList.get(position);
        int insert = position;

        int allItemsPosition;
        for (allItemsPosition = index + 1; allItemsPosition < this.allItems.size() && this.allItems.get(allItemsPosition).ItemType != TYPE_HEADER; ++allItemsPosition) {
            ++insert;
            ++count;
            this.visibleItems.add(insert, this.allItems.get(allItemsPosition));
            this.indexList.add(insert, allItemsPosition);
        }

        this.notifyItemRangeInserted(position + 1, count);
        allItemsPosition = this.indexList.get(position);
        this.expandMap.put(allItemsPosition, 1);
        if (notify) {
            this.notifyItemChanged(position);
        }

        if (expandItemsListener != null) {
            expandItemsListener.onExpandOrCollapse();
        }
    }

    public void collapseItems(int position, boolean notify) {
        int count = 0;
        int index = this.indexList.get(position);

        int allItemsPosition;
        for (allItemsPosition = index + 1; allItemsPosition < this.allItems.size() && this.allItems.get(allItemsPosition).ItemType != TYPE_HEADER; ++allItemsPosition) {
            ++count;
            this.visibleItems.remove(position + 1);
            this.indexList.remove(position + 1);
        }

        this.notifyItemRangeRemoved(position + 1, count);
        allItemsPosition = this.indexList.get(position);
        this.expandMap.delete(allItemsPosition);
        if (notify) {
            this.notifyItemChanged(position);
        }

        if (expandItemsListener != null) {
            expandItemsListener.onExpandOrCollapse();
        }
    }

    protected boolean isExpanded(int position) {
        int allItemsPosition = this.indexList.get(position);
        return this.expandMap.get(allItemsPosition, -1) >= 0;
    }

    public int getItemViewType(int position) {
        return this.visibleItems.get(position).ItemType;
    }

    public void setItems(List<T> items) {
        this.allItems = items;
        ArrayList visibleItems = new ArrayList();
        this.expandMap.clear();
        this.indexList.clear();

        for (int i = 0; i < items.size(); ++i) {
            if (items.get(i).ItemType == TYPE_HEADER && items.size() > (i + 1) && items.get(i + 1).ItemType != TYPE_HEADER) {
                this.indexList.add(i);
                visibleItems.add(items.get(i));
            }
        }

        this.visibleItems = visibleItems;
        this.notifyDataSetChanged();
    }

    protected void notifyItemInserted(int allItemsPosition, int visiblePosition) {
        this.incrementIndexList(allItemsPosition, visiblePosition, 1);
        this.incrementExpandMapAfter(allItemsPosition, 1);
        if (visiblePosition >= 0) {
            this.notifyItemInserted(visiblePosition);
        }

    }

    protected void removeItemAt(int visiblePosition) {
        int allItemsPosition = this.indexList.get(visiblePosition);
        this.allItems.remove(allItemsPosition);
        this.visibleItems.remove(visiblePosition);
        this.incrementIndexList(allItemsPosition, visiblePosition, -1);
        this.incrementExpandMapAfter(allItemsPosition, -1);
        this.notifyItemRemoved(visiblePosition);
    }

    private void incrementExpandMapAfter(int position, int direction) {
        SparseIntArray newExpandMap = new SparseIntArray();

        for (int i = 0; i < this.expandMap.size(); ++i) {
            int index = this.expandMap.keyAt(i);
            newExpandMap.put(index < position ? index : index + direction, 1);
        }

        this.expandMap = newExpandMap;
    }

    private void incrementIndexList(int allItemsPosition, int visiblePosition, int direction) {
        ArrayList newIndexList = new ArrayList();

        for (int i = 0; i < this.indexList.size(); ++i) {
            if (i == visiblePosition && direction > 0) {
                newIndexList.add(allItemsPosition);
            }

            int val = this.indexList.get(i);
            newIndexList.add(val < allItemsPosition ? val : val + direction);
        }

        this.indexList = newIndexList;
    }

    public void collapseAll() {
        this.collapseAllExcept(-1);
    }

    public void collapseAllExcept(int position) {
        for (int i = this.visibleItems.size() - 1; i >= 0; --i) {
            if (i != position && this.getItemViewType(i) == TYPE_HEADER && this.isExpanded(i)) {
                this.collapseItems(i, true);
            }
        }
    }

    public void expandDay(int day) {
        if (day < 0) {
            return;
        }

        this.expandItems(day, true);
    }

    public void setExpandItemsListener(ExpandItemsListener expandItemsListener) {
        this.expandItemsListener = expandItemsListener;
    }

    public void expandAll() {
        for (int i = this.visibleItems.size() - 1; i >= 0; --i) {
            if (this.getItemViewType(i) == TYPE_HEADER && !this.isExpanded(i)) {
                this.expandItems(i, true);
            }
        }
    }

    public List<Boolean> getHeadersStatus() {
        List<Boolean> headersStatusList = new ArrayList<>();

        for (int i = 0; i < this.visibleItems.size(); i++) {
            if (this.getItemViewType(i) == TYPE_HEADER) {
                headersStatusList.add(this.isExpanded(i));
            }
        }

        return headersStatusList;
    }

    public static void openArrow(View view) {
        view.animate().setDuration(150L).rotation(0.0F);
    }

    public static void closeArrow(View view) {
        view.animate().setDuration(150L).rotation(180.0F);
    }

    public int getMode() {
        return this.mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public class ItemViewHolder extends ExpandableRecyclerAdapter<T>.ViewHolder {
        public ItemViewHolder(View view) {
            super(view);
        }
    }

    public class StaticViewHolder extends ExpandableRecyclerAdapter<T>.ViewHolder {
        public StaticViewHolder(View view) {
            super(view);
        }
    }

    public class HeaderViewHolder extends ExpandableRecyclerAdapter<T>.ViewHolder {
        public ImageView arrow;

        public HeaderViewHolder(View view, ImageView arrow) {
            super(view);
            this.arrow = arrow;
            view.setOnClickListener(v -> HeaderViewHolder.this.handleClick());
        }

        protected void handleClick() {
            if (ExpandableRecyclerAdapter.this.toggleExpandedItems(this.getLayoutPosition(), false)) {
                ExpandableRecyclerAdapter.openArrow(this.arrow);
            } else {
                ExpandableRecyclerAdapter.closeArrow(this.arrow);
            }

        }

        public void bind(int position) {
            int[] screenLoc = new int[2];
            this.arrow.getLocationOnScreen(screenLoc);
            this.arrow.setRotation(ExpandableRecyclerAdapter.this.isExpanded(position) ? 0.0F : 180.0F);
        }
    }

    public class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }

    public static class ListItem {
        public int ItemType;

        public ListItem(int itemType) {
            this.ItemType = itemType;
        }
    }

    public interface ExpandItemsListener {
        void onExpandOrCollapse();
    }
}