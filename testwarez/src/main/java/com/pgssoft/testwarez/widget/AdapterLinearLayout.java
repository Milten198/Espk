package com.pgssoft.testwarez.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;

/**
 * A linear layout that will contain views taken from an adapter. It differs from the list view in the fact that it will
 * not optimize anything and draw all the views from the adapter. It also does not provide scrolling. However, when you
 * need a layout that will render views horizontally and you know there are not many child views, this is a good
 * option.
 *
 * @author Vincent Mimoun-Prat @ MarvinLabs
 * @see <a href="http://stackoverflow.com/questions/24905186/is-it-possible-to-set-adapter-to-linear-layout">http://stackoverflow.com/questions/24905186/is-it-possible-to-set-adapter-to-linear-layout</a>
 */
public class AdapterLinearLayout extends LinearLayout {

    private Adapter adapter;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AdapterLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AdapterLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdapterLinearLayout(Context context) {
        super(context);
    }

    public void setAdapter(Adapter adapter) {
        if (this.adapter == adapter) return;
        this.adapter = adapter;
        reloadChildViews();
    }

    private void reloadChildViews() {
        removeAllViews();

        if (adapter == null) {
            return;
        }

        int count = adapter.getCount();
        for (int position = 0; position < count; ++position) {
            View v = adapter.getView(position, null, this);
            if (v != null) {
                addView(v);
            }
        }

    }
}