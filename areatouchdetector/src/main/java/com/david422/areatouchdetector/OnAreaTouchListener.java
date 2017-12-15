package com.david422.areatouchdetector;

/**
 * Created by dpodolak on 22.02.16.
 */
public interface OnAreaTouchListener<T> {
    public void onAreaTouch(int color, T object);
}
