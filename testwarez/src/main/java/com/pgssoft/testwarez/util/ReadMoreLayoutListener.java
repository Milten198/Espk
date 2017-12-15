package com.pgssoft.testwarez.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgssoft.testwarez.R;

/**
 * Created by rtulaza on 2015-09-10.
 */
public class ReadMoreLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
    private int fullHeight = 0;
    private boolean isExpanded = false;

    private static final int COLLAPSING_MAX_LINES = 15;

    private Context context;
    private View readMore;
    private ImageView readMoreIcon;
    private TextView description;
    private Point screenSize;
    private TextView readMoreText;

    public ReadMoreLayoutListener(Context context, View readMore, ImageView readMoreIcon, TextView readMoreText, TextView description, Point screenSize) {
        this.context = context;
        this.readMore = readMore;
        this.readMoreIcon = readMoreIcon;
        this.readMoreText = readMoreText;
        this.description = description;
        this.screenSize = screenSize;
    }

    @Override
    public void onGlobalLayout() {
        if(description.getWidth() > 0 || description.getHeight() > 0) {

            int lineNumber = description.getLineCount();

            description.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            fullHeight = description.getHeight();

            if(lineNumber > COLLAPSING_MAX_LINES) {

                int collapsedHeight = (int) ((COLLAPSING_MAX_LINES * (description.getTextSize() + description.getLineSpacingExtra())) - description.getLineSpacingExtra() + description.getPaddingTop()); //(int) (height * 20);

                ViewGroup.LayoutParams layoutParams = description.getLayoutParams();
                layoutParams.height = collapsedHeight;
                description.setLayoutParams(layoutParams);

                readMore.setVisibility(View.VISIBLE);
                readMoreIcon.setImageDrawable(Utils.getDrawable(context, R.drawable.expand_white));
                readMoreText.setText(R.string.event_read_more);

                readMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        readMore.setOnClickListener(null);
                        View.OnClickListener me = this;

                        ValueAnimator anim = ValueAnimator.ofInt(collapsedHeight, fullHeight);
                        anim.addUpdateListener(valueAnimator -> {
                            ViewGroup.LayoutParams lp = description.getLayoutParams();
                            lp.height = (int) valueAnimator.getAnimatedValue();
                            description.setLayoutParams(lp);
                        });
                        anim.setDuration(500);

                        anim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if(!isExpanded) {
                                    readMoreIcon.setImageDrawable(Utils.getDrawable(context, R.drawable.collapse_white));
                                    readMoreText.setText(R.string.event_read_less);
                                    isExpanded = true;
                                } else {
                                    readMoreIcon.setImageDrawable(Utils.getDrawable(context, R.drawable.expand_white));
                                    readMoreText.setText(R.string.event_read_more);
                                    isExpanded = false;
                                }
                                readMore.setOnClickListener(me);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                onAnimationEnd(animation);
                            }
                        });

                        if(!isExpanded) {
                            anim.start();
                        } else {
                            anim.reverse();
                        }
                    }
                });
            } else {
                readMore.setVisibility(View.GONE);

                readMore.setOnClickListener(null);
            }
        }
    }
}
