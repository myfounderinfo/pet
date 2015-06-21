package com.globalLibrary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import com.globalLibrary.util.BitmapLoader;
import com.globalLibrary.util.UserTask;

import java.util.ArrayList;

/**
 * 显示网络图片
 */
@SuppressWarnings("UnusedDeclaration")
public class NetworkImageView extends ImageView {
    private ArrayList<BitmapLoader.BitmapProcessor> handleBitmaps = new ArrayList<BitmapLoader.BitmapProcessor>();

    private String url;
    private UserTask task;
    private boolean equalsDefaultImageSize = false;

    private boolean isStopTaskWhenDetachedFromWindow = true;
    private boolean strictSize;
    private int defaultImageResId;

    private int width, height;

    public NetworkImageView(Context context) {
        super(context);
    }

    public NetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        if (getDrawable() instanceof BitmapLoader.AsyncDrawable) {
            BitmapLoader.AsyncDrawable asyncDrawable = (BitmapLoader.AsyncDrawable) getDrawable();
            if (asyncDrawable.getUrl() == null || !asyncDrawable.getUrl().equals(url)) {
                showImage();
            } else if (task == null || task.isCancelled()) {
                showImage();
            }
        }
        super.onAttachedToWindow();
    }

    private void showImageDirect() {
        boolean isFullyWrapContent = getLayoutParams() != null && getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT
                && getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT;
        int viewWidth = getMeasuredWidth();
        int viewHeight = getMeasuredHeight();
        if (viewWidth == 0 && viewHeight == 0 && !isFullyWrapContent) {
            return;
        }
        BitmapLoader bitmapLoader = BitmapLoader.with(getContext()).imagePath(url).equalsDefaultImageSize(equalsDefaultImageSize);
        if (width > 0 || height > 0) {
            bitmapLoader.size(width, height).strictSize(strictSize);
        }
        if (defaultImageResId > 0) {
            bitmapLoader.defaultBitmap(defaultImageResId);
        }
        if (!handleBitmaps.isEmpty()) {
            bitmapLoader.addHandleBitmap(handleBitmaps);
        }
        task = bitmapLoader.useImage(NetworkImageView.this);
    }

    private void showImage() {
        if (isShown()) {
            showImageDirect();
        } else {
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    showImageDirect();
                    return false;
                }
            });
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (isStopTaskWhenDetachedFromWindow && task != null) {
            task.cancel(true);
            task = null;
            Drawable drawable = getDrawable();
            if (drawable != null) {
                drawable.setCallback(null);
            }
            setImageDrawable(null);
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable drawable = getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap == null || bitmap.isRecycled()) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            } else {
                setMeasuredDimension(bitmap.getWidth(), bitmap.getHeight());
            }
        } else {
            int wMeasureSpec = widthMeasureSpec;
            int hMeasureSpec = heightMeasureSpec;
            if (width > 0) {
                wMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            }
            if (height > 0) {
                hMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
            super.onMeasure(wMeasureSpec, hMeasureSpec);
        }
    }

    public NetworkImageView addHandleBitmap(BitmapLoader.BitmapProcessor bitmapProcessor) {
        if (bitmapProcessor != null) {
            handleBitmaps.add(bitmapProcessor);
        }
        return this;
    }

    public NetworkImageView equalsDefaultImageSize(final boolean equalsDefaultImageSize) {
        this.equalsDefaultImageSize = equalsDefaultImageSize;
        return this;
    }

    public NetworkImageView height(final int height) {
        this.height = height;
        return this;
    }

    public NetworkImageView setDefaultImageResId(int resId) {
        defaultImageResId = resId;
        return this;
    }

    public NetworkImageView strictSize(boolean strictSize) {
        this.strictSize = strictSize;
        return this;
    }

    public NetworkImageView width(final int width) {
        this.width = width;
        return this;
    }

    public void setImageUrl(String url) {
        this.url = url;
        showImage();
    }

    public void setStopTaskWhenDetachedFromWindow(boolean isStopTaskWhenDetachedFromWindow) {
        this.isStopTaskWhenDetachedFromWindow = isStopTaskWhenDetachedFromWindow;
    }
}
