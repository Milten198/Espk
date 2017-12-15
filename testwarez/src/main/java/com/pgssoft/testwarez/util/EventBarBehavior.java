package com.pgssoft.testwarez.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.pgssoft.testwarez.R;

import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by rtulaza on 2015-09-10.
 */
public class EventBarBehavior extends CoordinatorLayout.Behavior<RelativeLayout> {

    private int toolbarHeight;
    private int margin;
    private boolean shown = true;
    private View continueIndicator;

    private int[] childLocation = new int[2];


    public EventBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        toolbarHeight = context.getResources().getDimensionPixelSize(R.dimen.toolbar_height);
        margin = context.getResources().getDimensionPixelSize(R.dimen.expanded_toolbar_margin);
    }

    public EventBarBehavior(Context context) {
        super();

        toolbarHeight = context.getResources().getDimensionPixelSize(R.dimen.toolbar_height);
        margin = context.getResources().getDimensionPixelSize(R.dimen.expanded_toolbar_margin);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, RelativeLayout child, View dependency) {
        return dependency instanceof AppBarLayout;
    }


    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, RelativeLayout child, View dependency) {
        if (continueIndicator == null) {
            continueIndicator = ButterKnife.findById(parent, R.id.tvActivityEventContinous);

            child.getLocationOnScreen(childLocation);
        }

        ObjectAnimator hideTrackIndicator = ObjectAnimator.ofFloat(continueIndicator, View.ALPHA, 1f, 0f);
        ObjectAnimator showTrackIndicator = ObjectAnimator.ofFloat(continueIndicator, View.ALPHA, 0f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(200);

        int value = dependency.getHeight() - margin - 3 *toolbarHeight;

        if (shown && dependency.getY() < -value) {
            shown = false;
            set.playTogether(hideTrackIndicator);
            set.start();
        } else if (!shown && dependency.getY() > -value) {
            shown = true;

            set.playTogether(showTrackIndicator);
            set.start();
        }


        return true;
    }
}
