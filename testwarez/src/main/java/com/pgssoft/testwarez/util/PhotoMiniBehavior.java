package com.pgssoft.testwarez.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.pgssoft.testwarez.R;

/**
 * Created by rtulaza on 2015-08-18.
 */
public class PhotoMiniBehavior extends CoordinatorLayout.Behavior<View> {
    private int toolbarHeight;
    private boolean shown = true;

    public PhotoMiniBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        toolbarHeight = context.getResources().getDimensionPixelSize(R.dimen.toolbar_height);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        ObjectAnimator hideX = ObjectAnimator.ofFloat(child, "scaleX", 1f, 0f);
        ObjectAnimator hideY = ObjectAnimator.ofFloat(child, "scaleY", 1f, 0f);

        ObjectAnimator showX = ObjectAnimator.ofFloat(child, "scaleX", 0f, 1f);
        ObjectAnimator showY = ObjectAnimator.ofFloat(child, "scaleY", 0f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(200);

        if(shown && dependency.getY() < -toolbarHeight/3) {
            shown = false;

            set.playTogether(hideX, hideY);
        } else if(!shown && dependency.getY() > -toolbarHeight/3) {
            shown = true;

            set.playTogether(showX, showY);
        }
        set.start();

        return true;
    }
}
