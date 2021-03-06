package com.pgssoft.testwarez.widget.ratingbar;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

interface SimpleRatingBar {
    void setNumStars(int numStars);

    int getNumStars();

    void setRating(float rating);

    float getRating();

    void setStarPadding(int ratingPadding);

    int getStarPadding();

    void setEmptyDrawable(Drawable drawable);

    void setEmptyDrawableRes(@DrawableRes int res);

    void setFilledDrawable(Drawable drawable);

    void setFilledDrawableRes(@DrawableRes int res);
}