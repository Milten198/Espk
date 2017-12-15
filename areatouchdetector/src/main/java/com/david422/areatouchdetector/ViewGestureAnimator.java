package com.david422.areatouchdetector;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

/**
 * Created by dpodolak on 30.08.16.
 */
public class ViewGestureAnimator implements GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {

    private OverScroller overScroller;

    private AreaTouchableView areaTouchableView;
    private Context context;


    boolean isScaling = false;

    private int prevPositionX = 0;
    private int prevPositionY = 0;

    /**
     * center coordinates of mainView
     */
    int centerMainX, centerMainY;

    /**
     * Factor using to scale mainView and maskView
     */
    float scaleFactor = 1f;

    float prevScale = 1;

    /**
     *
     */
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;


    public ViewGestureAnimator(AreaTouchableView areaTouchableView) {
        this.context = areaTouchableView.getContext();
        this.areaTouchableView = areaTouchableView;
        overScroller = new OverScroller(context);

        gestureDetector = new GestureDetector(context, this);
        scaleGestureDetector = new ScaleGestureDetector(context, this);

    }

    @Override
    public boolean onDown(MotionEvent e) {
        overScroller.forceFinished(true);
        ViewCompat.postInvalidateOnAnimation(areaTouchableView);
        Log.d("AreaTouchableView", "onDown: ");

        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        // TODO: 04.03.16 when root view has layou_width set exactly, the behavior of these isn't properly
        if (isScaling) {
            return false;
        }

        overScroller.forceFinished(true);
        int dx = (int) distanceX;
        int dy = (int) distanceY;

        int newPositionX = areaTouchableView.positionX + dx;
        int newPositionY = areaTouchableView.positionY + dy;

        float yOffset = getOffsetY();
        float xOffset = getOffsetX();
        newPositionX += xOffset;


        if (newPositionX < 0) {
            dx -= newPositionX;
        } else if (newPositionX > getMaxHorizontal()) {
            dx -= (newPositionX - getMaxHorizontal());
        }

        if (areaTouchableView.mainRect.height() < areaTouchableView.viewRect.height()) {
            dy -= newPositionY;
        } else {
            if ((newPositionY + yOffset) < 0) {
                dy -= newPositionY + yOffset;
            } else if ((newPositionY - yOffset) > 0) {
                dy -= (newPositionY - yOffset);
            }
        }

        overScroller.startScroll(areaTouchableView.positionX, areaTouchableView.positionY, dx, dy, 0);
        ViewCompat.postInvalidateOnAnimation(areaTouchableView);
        return false;
    }

    private int getOffsetX() {
        return (int) ((areaTouchableView.mainView.getScaleX() - 1) * areaTouchableView.mainView.getWidth() / 2);
    }

    private int getOffsetY() {
        return (areaTouchableView.mainRect.height() - areaTouchableView.viewRect.height()) / 2;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        overScroller.forceFinished(true);

        int minX = 0, maxX = 0, minY = 0, maxY = 0;

        int xOffset = getOffsetX();
        maxX += xOffset;
        minX -= xOffset;

        if (areaTouchableView.mainRect.height() > areaTouchableView.viewRect.height()) {
            float yOffset = getOffsetY();

            minY -= yOffset;
            maxY += yOffset;
        }
        Log.d("AreaTouchableView", "onFling: maxX:" + maxX);
        overScroller.fling(areaTouchableView.positionX, areaTouchableView.positionY, (int) -velocityX, (int) -velocityY, minX, maxX, minY, maxY);

        ViewCompat.postInvalidateOnAnimation(areaTouchableView);
        return true;
    }

    public void computeScroll() {

        if (isScaling) {
            return;
        }

        if (overScroller.computeScrollOffset()) {
            areaTouchableView.positionX = overScroller.getCurrX();
            areaTouchableView.positionY = overScroller.getCurrY();

            areaTouchableView.mainView.setX(areaTouchableView.mainView.getX() - (areaTouchableView.positionX - prevPositionX));
            areaTouchableView.mainView.setY(areaTouchableView.mainView.getY() - (areaTouchableView.positionY - prevPositionY));

            ViewCompat.postInvalidateOnAnimation(areaTouchableView);
            prevPositionX = areaTouchableView.positionX;
            prevPositionY = areaTouchableView.positionY;
        }

    }

    private int getMaxHorizontal() {
        return (int) Math.abs(areaTouchableView.viewRect.right - (areaTouchableView.mainView.getWidth() * areaTouchableView.mainView.getScaleX()));
    }

    private int getMaxVertical() {
        return (int) Math.abs(areaTouchableView.viewRect.bottom - (areaTouchableView.mainView.getHeight() * areaTouchableView.mainView.getScaleY()));
    }


    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        areaTouchableView.scale *= detector.getScaleFactor();
        areaTouchableView.scale = Math.max(0.1f, Math.min(areaTouchableView.scale, 5.0f));


        areaTouchableView.mainView.setScaleX(areaTouchableView.scale);
        areaTouchableView.mainView.setScaleY(areaTouchableView.scale);


        int newXOffset = (int) (centerMainX - ((areaTouchableView.mainView.getWidth() * areaTouchableView.scale) / 2));
        int newYOffset = (int) (centerMainY - ((areaTouchableView.mainView.getHeight() * areaTouchableView.scale) / 2));

        areaTouchableView.xOffset = (int) (newXOffset - (areaTouchableView.mainView.getX() / 2));
        areaTouchableView.yOffset = (int) (newYOffset - (areaTouchableView.mainView.getY() / 2));

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        isScaling = true;
        centerMainX = (int) ((areaTouchableView.mainView.getX() + areaTouchableView.mainView.getWidth()) / 2);
        centerMainY = (int) ((areaTouchableView.mainView.getY() + areaTouchableView.mainView.getHeight()) / 2);
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector detector) {
        if (areaTouchableView.scale > 1) {
            isScaling = false;
            return;
        }

        animToDefault();

    }

    public void animToDefault() {

        /**
         * If scale is less than 1, align view to both edges of screen
         */
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(1000);
        animatorSet.setInterpolator(new DecelerateInterpolator());

        animatorSet.addListener(new AnimatorListenerHelper() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                areaTouchableView.mainRect.top = (int) areaTouchableView.mainView.getY() + areaTouchableView.yOffset;
                areaTouchableView.mainRect.bottom = (int) (areaTouchableView.mainRect.top + (areaTouchableView.mainView.getHeight() * areaTouchableView.mainView.getScaleY()));
                areaTouchableView.mainRect.left = (int) (areaTouchableView.xOffset + areaTouchableView.mainView.getX());
                areaTouchableView.mainRect.right = (int) (areaTouchableView.mainRect.left + (areaTouchableView.mainView.getWidth() * areaTouchableView.mainView.getScaleX()));
                isScaling = false;
            }
        });

        areaTouchableView.scale = 1f;
        scaleFactor = 1f;
        prevScale = 1f;


        areaTouchableView.xOffset = 0;
        areaTouchableView.yOffset = 0;

        areaTouchableView.positionX = 0;
        areaTouchableView.positionY = 0;

        prevPositionX = 0;
        prevPositionY = 0;

        ObjectAnimator animXMainView = ObjectAnimator.ofFloat(areaTouchableView.mainView, View.TRANSLATION_X, 0);
        animatorSet.playTogether(animXMainView);

        ObjectAnimator animYMainView = ObjectAnimator.ofFloat(areaTouchableView.mainView, View.TRANSLATION_Y, 0);

        animatorSet.playTogether(animYMainView);


        ObjectAnimator animMainViewX = ObjectAnimator.ofFloat(areaTouchableView.mainView, View.SCALE_X, 1);
        ObjectAnimator animMainViewY = ObjectAnimator.ofFloat(areaTouchableView.mainView, View.SCALE_Y, 1);

        animatorSet.playTogether(animMainViewX, animMainViewY);


        animatorSet.start();
    }

    public void onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
    }
}
