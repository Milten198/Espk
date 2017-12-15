package com.pgssoft.testwarez.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RatingBar;

/**
 * Created by brosol on 2017-07-13.
 */

public class StarRatingBar extends RatingBar {

    public StarRatingBar(Context context) {
        super(context);
    }

    public StarRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE && isPressed() && getOnRatingBarChangeListener() != null) {
            getOnRatingBarChangeListener().onRatingChanged(this, getRating(), false);
        }

        return super.onTouchEvent(event);
    }
}
