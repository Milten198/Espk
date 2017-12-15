package com.pgssoft.testwarez.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;

/**
 * Created by dpodolak on 11.04.16.
 */
public class AnimatedImageView extends ImageView{

    public AnimatedImageView(Context context) {
        super(context);
        initilize(context);
    }

    public AnimatedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initilize(context);
    }

    public AnimatedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initilize(context);
    }

    private void initilize(Context context){

    }

    public void setContentHeight(final int contentHeight) {
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = contentHeight;
        setLayoutParams(layoutParams);
    }

    public void setContentWidth(final int contentWidth) {
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = contentWidth;
        setLayoutParams(layoutParams);
    }

    public int getContentHeight() {
        return getLayoutParams().height;
    }

    public int getContentWidth() {
        return getLayoutParams().width;
    }

    public int getContentX() {
        return ((MarginLayoutParams) getLayoutParams()).leftMargin;
    }

    public void setContentX(final int contentX) {
//        final MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
//        layoutParams.leftMargin = contentX;
//        setLayoutParams(layoutParams);
        setX(contentX);
    }

    public int getContentY() {
        return ((MarginLayoutParams) getLayoutParams()).topMargin;
    }

    public void setContentY(final int contentY) {
//        final MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
//        layoutParams.topMargin = contentY;
//        setLayoutParams(layoutParams);
        setY(contentY);
    }
}
