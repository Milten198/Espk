package com.david422.areatouchdetector;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.david422.areatouchableimage.R;

import java.util.HashMap;

/**
 * Created by dpodolak on 15.02.16.
 */
public class AreaTouchableView extends FrameLayout implements View.OnTouchListener {


    /**
     * ImageView with bitmap which should be display for users
     */
    ImageView mainView;
    ImageView dummyView;

    /**
     * Determine color tolerance for comparing color from maskView and colorList
     */
    private int colorTolerance = 25;

    /**
     * Represent the coordinates of mainView and maskView
     */
    Rect mainRect = new Rect();

    /**
     * Represent the coordinated of this view
     */
    Rect viewRect = new Rect();
    private int width;
    private int height;

    public void enableGesture(boolean b) {
        isGestures = false;
    }


    boolean isClickable = true;

    int xOffset, yOffset;

    float scale = 1;


    /**
     * Point setting on MOTION_DOWN
     */
    PointF startPoint = new PointF();


    /**
     * Contains object as value, and color as key. When user tap at the specified color, the object will be return
     */
    private HashMap<Integer, Object> colorMap = new HashMap<>();


    int positionX = 0;
    int positionY = 0;

    /**
     * Store pixels values for mask
     * First dimension is a width, second height
     */
    private int[][] maskPixelsMatrix;

    /**
     * Index:
     * 0 - width
     * 1 - height
     */
    private int[] mainViewSize = new int[2];


    /**
     * Index:
     * 0 - width
     * 1 - height
     *
     * @serial
     */
    private int[] maskBitmapSize = new int[2];


    /**
     * If true, this view handling gestures
     */
    private boolean isGestures = true;
    private Drawable mainViewDrawable;
    private Drawable maskViewDrawable;
    private Bitmap dummyBitmap;

    private OnAreaTouchListener onAreaTouchListener;


    private Bitmap mainTempBitmap;
    private Bitmap maskTempBitmap;

    private ViewGestureAnimator viewGestureAnimator;

    public AreaTouchableView(Context context) {
        super(context);
        initialize(context);
    }

    public AreaTouchableView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        }

        isGestures = attrs.getAttributeBooleanValue(R.styleable.AreaTouchableView_disable_gestures, true);
        initialize(context);
        prepareDrawables(context, attrs);

    }

    public AreaTouchableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            return;
        }

        isGestures = attrs.getAttributeBooleanValue(R.styleable.AreaTouchableView_disable_gestures, true);
        initialize(context);
        prepareDrawables(context, attrs);
    }

    private void prepareDrawables(Context context, AttributeSet attrs) {

        long maxMemoryRuntime = Runtime.getRuntime().maxMemory() / 1024;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AreaTouchableView);

        int mainId = a.getResourceId(R.styleable.AreaTouchableView_top, -1);
        if (mainId != -1) {
            setMainView(mainId);
        }


        int maskId = a.getResourceId(R.styleable.AreaTouchableView_mask, -1);
        if (maskId != -1) {
            setMaskView(maskId);
        }


    }

    public void setMaskView(@DrawableRes int maskId) {
        BitmapFactory.Options maskOptions = getBitmapMaskOptions(maskId);
        Bitmap maskBitmap = BitmapFactory.decodeResource(getResources(), maskId, maskOptions);

        setMaskView(maskBitmap);
    }


    public void setMaskView(Bitmap mBitmap) {

        BitmapDrawable bd = (BitmapDrawable) dummyView.getDrawable();

        if (bd != null) {
            int w = mBitmap.getWidth();
            int h = mBitmap.getHeight();
            int[] maskPixelsArray = new int[w * h];
            maskPixelsMatrix = new int[w][h];

            maskBitmapSize[0] = w;
            maskBitmapSize[1] = h;

            //inflate pixels matrix from pixel array
            mBitmap.getPixels(maskPixelsArray, 0, mBitmap.getWidth(), 0, 0, mBitmap.getWidth(), mBitmap.getHeight());

            int l = 0;
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    maskPixelsMatrix[i][j] = maskPixelsArray[l++];
                }
            }

            Bitmap mainBitmap = bd.getBitmap();

            float mainRatio = (float) mainBitmap.getWidth() / mainBitmap.getHeight();
            float maskRatio = (float) mBitmap.getWidth() / mBitmap.getHeight();

            if (Math.abs(mainRatio - maskRatio) > 0.01) {
                throw new IllegalStateException("Mask bitmap has different ratio than main bitmap. Both should have the same ratio");
            }
        } else {
            maskTempBitmap = mBitmap;
        }
    }

    public void setMainView(@DrawableRes int mainId) {
        BitmapFactory.Options mainOptions = getBitmapMainOptions(mainId);
        Bitmap mainBitmap = BitmapFactory.decodeResource(getResources(), mainId, mainOptions);

        setMainView(mainBitmap);
    }


    public void setMainView(Bitmap mBitmap) {

        if (width == 0 || height == 0) {
            mainTempBitmap = mBitmap;
            return;
        }

        int orientation = getContext().getResources().getConfiguration().orientation;
        float ratio = (float) mBitmap.getWidth() / mBitmap.getHeight();

        // TODO: 30.08.16 take into account ratio when height is greater than width
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (mBitmap.getHeight() != height) {
                dummyBitmap = Bitmap.createScaledBitmap(mBitmap, (int) (height * ratio), height, false);
            }
        } else {
            if (mBitmap.getWidth() != width) {
                dummyBitmap = Bitmap.createScaledBitmap(mBitmap, width, (int) (width / ratio), false);
            }
        }

        if (dummyBitmap == null) {
            mainViewSize[0] = mBitmap.getWidth();
            mainViewSize[1] = mBitmap.getHeight();
        } else {
            mainViewSize[0] = dummyBitmap.getWidth();
            mainViewSize[1] = dummyBitmap.getHeight();
        }

        mainViewDrawable = new BitmapDrawable(getResources(), mBitmap);
        mainView.setImageDrawable(mainViewDrawable);
        dummyView.setImageDrawable(mainViewDrawable);

        mainView.setScaleX(scale);
        mainView.setScaleY(scale);
        mainView.setTranslationX(positionX);
        mainView.setTranslationY(positionY);

        dummyView.setScaleX(scale);
        dummyView.setScaleY(scale);
        dummyView.setTranslationX(positionX);
        dummyView.setTranslationY(positionY);


        if (maskTempBitmap != null) {
            setMaskView(maskTempBitmap);
            maskTempBitmap = null;
        }

    }

    public ImageView getMainView() {
        return mainView;
    }

    private BitmapFactory.Options getBitmapMainOptions(int resource) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resource, options);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inJustDecodeBounds = false;
        options.inScaled = false;
        return options;
    }

    private BitmapFactory.Options getBitmapMaskOptions(int resource) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resource, options);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        options.inScaled = false;
        return options;
    }

    public void setDefaultScale(boolean anim) {
        if (anim) {
            viewGestureAnimator.animToDefault();
        } else {
            scale = 1;
            mainView.setScaleX(scale);
            mainView.setScaleY(scale);
            mainView.setX(0);
            mainView.setY(getHeight() / 2 - mainView.getHeight() / 2);
        }
    }

    private long getBitmapSize(Drawable drawable) {
        return getBitmapSize(((BitmapDrawable) drawable).getBitmap());
    }

    private long getBitmapSize(Bitmap bitmap) {
        return bitmap.getByteCount();
    }


    public void addExistingColor(int color, Object o) {
        colorMap.put(color, o);
    }

    public void addExistingColorRGB(String color, Object o) {
        colorMap.put(Color.parseColor(color), o);
    }


    public void setOnAreaTouchListener(OnAreaTouchListener onAreaTouchListener) {
        this.onAreaTouchListener = onAreaTouchListener;
    }

    /**
     * Set tolerance color between mask color and color existed in colorList
     *
     * @param colorTolerance
     */
    public void setColorTolerance(int colorTolerance) {
        this.colorTolerance = colorTolerance;
    }

    private void initialize(Context context) {

        inflate(context, R.layout.root_view, this);
        mainView = (ImageView) findViewById(R.id.main);
        dummyView = (ImageView) findViewById(R.id.dummy);

        setOnTouchListener(this);
        viewGestureAnimator = new ViewGestureAnimator(this);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int evX = (int) event.getX(0);
        int evY = (int) event.getY(0);

        /**Set coordinates*/
        mainRect.left = (int) (xOffset + mainView.getX());
        mainRect.top = (int) mainView.getY() + yOffset;
        mainRect.right = (int) (mainRect.left + (mainView.getWidth() * mainView.getScaleX()));
        mainRect.bottom = (int) (mainRect.top + (mainView.getHeight() * mainView.getScaleY()));

        viewGestureAnimator.onTouchEvent(event);

        int action = event.getAction() & MotionEvent.ACTION_MASK;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startPoint.set(evX, evY);

                break;

            case MotionEvent.ACTION_UP:

                /**Distance to determine treshold for recognize color*/
                float distance = distance(startPoint, new PointF(evX, evY));

                if (distance < 30) {
                    /**Detect color if*/
                    if (isClickable) {
                        detectColor(evX, evY);
                    }
                }

                break;
        }

        return true;
    }

    /**
     * Detect color on mask mechanism
     *
     * @param evX
     * @param evY
     */
    private void detectColor(int evX, int evY) {
        if (!mainRect.contains(evX, evY)) {
            return;
        }

        int pixel = getHotspotColor(evX, evY);

        if (pixel != -1) {
            int color = findColor(pixel);

            if (color != -1) {
                if (onAreaTouchListener != null) {
                    onAreaTouchListener.onAreaTouch(color, colorMap.get(color));
                }
            }
        }
    }


    /**
     * Calculate distance between two points
     *
     * @param startPoint
     * @param endPointF
     * @return
     */
    private float distance(PointF startPoint, PointF endPointF) {
        float x = startPoint.x - endPointF.x;
        float y = startPoint.y - endPointF.y;
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Get color of pixel
     *
     * @param evX x coordinate where pixel should take
     * @param evY y coordinate where pixel should take
     * @return
     */
    private int getHotspotColor(int evX, int evY) {

        if ((evY - mainRect.top) / scale < 0) {
            return -1;
        }

        int positionX = (int) (((evX - mainRect.left)) / scale);
        int positionY = (int) (((evY - mainRect.top)) / scale);

        int px = (maskBitmapSize[0] * positionX) / mainViewSize[0];
        int py = (maskBitmapSize[1] * positionY) / mainViewSize[1];

        if (maskBitmapSize[0] < px) {
            return -1;
        } else if (maskBitmapSize[1] < py) {
            return -1;
        }

        return maskPixelsMatrix[px][py];
    }

    /**
     * Return color of pixel
     *
     * @param pixelColor
     * @return
     */
    private int findColor(int pixelColor) {

        int red = Color.red(pixelColor);
        int green = Color.green(pixelColor);
        int blue = Color.blue(pixelColor);
        for (Integer color : colorMap.keySet()) {
            int redDiff = (int) Math.abs(red - Color.red(color));
            int greenDiff = (int) Math.abs(green - Color.green(color));
            int blueDiff = (int) Math.abs(blue - Color.blue(color));

            if (redDiff < colorTolerance && greenDiff < colorTolerance && blueDiff < colorTolerance) {
                return color;
            }
        }

        return -1;
    }


    private Rect mContentRect = new Rect(mainRect);

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        getGlobalVisibleRect(viewRect);
        mContentRect.set(getPaddingLeft(), getPaddingTop(), getWidth(), getHeight());


    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        width = right;
        height = bottom;

        if (mainTempBitmap != null) {
            setMainView(mainTempBitmap);

            mainTempBitmap = null;
            mainView.requestLayout();
        }

    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        viewGestureAnimator.computeScroll();
    }

    public void disableClick() {
        isClickable = false;
    }
}
