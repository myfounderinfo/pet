package com.globalLibrary.view.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class ViewfinderView extends View {
    private Paint mPaint = new Paint();
    private Rect frameBorder = new Rect();
    private Rect frameRect = new Rect();

    public Rect getFrameRect() {
        return this.frameRect;
    }

    private boolean frameCenterSetByUser = false;
    private int borderColor = 0xFFFF0000;
    private int borderSize = 2;
    private int frameCenterX = -Integer.MAX_VALUE;
    private int frameCenterY = -Integer.MAX_VALUE;
    private int frameColor = 0X00000000;
    private int frameHeight = 0;

    private int frameWidth = 0;
    private int maskColor = 0x60000000;

    private int x, y;

    public ViewfinderView(Context context) {
        super(context);
    }

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        // 绘制遮罩层
        mPaint.setColor(maskColor);
        canvas.drawRect(0, 0, width, frameRect.top, mPaint);
        canvas.drawRect(0, frameRect.top, frameRect.left, frameRect.bottom, mPaint);
        canvas.drawRect(0, frameRect.bottom, width, height, mPaint);
        canvas.drawRect(frameRect.right, frameRect.top, width, frameRect.bottom, mPaint);

        // 绘制边框线
        mPaint.setColor(borderColor);
        canvas.drawRect(frameRect.left, frameRect.top - borderSize, frameRect.right + borderSize, frameRect.top, mPaint);
        canvas.drawRect(frameRect.left, frameRect.top, frameRect.left + borderSize, frameRect.bottom, mPaint);
        canvas.drawRect(frameRect.left, frameRect.bottom, frameRect.right + borderSize, frameRect.bottom + borderSize, mPaint);
        canvas.drawRect(frameRect.right, frameRect.top, frameRect.right + borderSize, frameRect.bottom, mPaint);

        // 绘制窗口
        mPaint.setColor(frameColor);
        canvas.drawRect(frameRect.left + borderSize, frameRect.top, frameRect.right, frameRect.bottom, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setFrameBorder(0, 0, getMeasuredWidth(), getMeasuredHeight());
        initRect(frameWidth <= 0 ? getMeasuredWidth() * 3 / 4 : frameWidth, frameHeight <= 0 ? getMeasuredHeight() * 3 / 4 : frameHeight);
    }

    public ViewfinderView setFrameBorder(int left, int top, int right, int bottom) {
        this.frameBorder.left = left;
        this.frameBorder.top = top;
        this.frameBorder.right = right;
        this.frameBorder.bottom = bottom;
        return this;
    }

    private void initRect(int width, int height) {
        initFrameCenter();
        frameRect.left = frameCenterX - width / 2;
        frameRect.right = frameRect.left + width;
        frameRect.top = frameCenterY - height / 2;
        frameRect.bottom = frameRect.top + height;
        frameWidth = width;
        frameHeight = height;

        if (!frameBorder.isEmpty() && !frameBorder.contains(frameRect)) {
            if (frameBorder.left > frameRect.left) {
                frameRect.left = frameBorder.left;
                frameRect.right = frameRect.left + width;
                frameCenterX = frameRect.centerX();
            }
            if (frameRect.top < frameBorder.top) {
                frameRect.top = frameBorder.top;
                frameRect.bottom = frameRect.top + height;
                frameCenterY = frameRect.centerY();
            }
            if (frameRect.right > frameBorder.right) {
                frameRect.right = frameBorder.right;
                frameRect.left = frameRect.right - width;
                frameCenterX = frameRect.centerX();
            }
            if (frameRect.bottom > frameBorder.bottom) {
                frameRect.bottom = frameBorder.bottom;
                frameRect.top = frameRect.bottom - height;
                frameCenterY = frameRect.centerY();
            }
        }
    }

    private void initFrameCenter() {
        if (!frameCenterSetByUser) {
            frameCenterX = getMeasuredWidth() / 2;
            frameCenterY = getMeasuredHeight() / 2;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = (int) event.getX();
                y = (int) event.getY();
                if (!isTouchFrame(x, y)) {
                    x = -1;
                    y = -1;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int curX = (int) event.getX();
                int curY = (int) event.getY();
                if (x != -1 && y != -1) {
                    x = curX;
                    y = curY;
                    setFrameCenterX(x).setFrameCenterY(y);
                }
                break;
        }
        return true;
    }

    private boolean isTouchFrame(int x, int y) {
        return frameRect.contains(x, y);
    }

    public ViewfinderView setFrameCenterY(int frameCenterY) {
        if (this.frameCenterY != frameCenterY) {
            frameCenterSetByUser = true;
            this.frameCenterY = frameCenterY;
            initRect(frameWidth, frameHeight);
            postInvalidate();
        }
        return this;
    }

    public ViewfinderView borderColor(final int borderColor) {
        if (this.borderColor != borderColor) {
            this.borderColor = borderColor;
            postInvalidate();
        }
        return this;
    }

    public ViewfinderView borderSize(final int borderSize) {
        if (this.borderSize != borderSize) {
            this.borderSize = borderSize;
            postInvalidate();
        }
        return this;
    }

    public ViewfinderView frameColor(final int frameColor) {
        if (this.frameColor != frameColor) {
            this.frameColor = frameColor;
            postInvalidate();
        }
        return this;
    }

    public ViewfinderView maskColor(final int maskColor) {
        if (this.maskColor != maskColor) {
            this.maskColor = maskColor;
            postInvalidate();
        }
        return this;
    }

    public ViewfinderView setFrameBorder(Rect rect) {
        this.frameBorder = rect;
        return this;
    }

    public ViewfinderView setFrameCenterX(int frameCenterX) {
        if (this.frameCenterX != frameCenterX) {
            frameCenterSetByUser = true;
            this.frameCenterX = frameCenterX;
            initRect(frameWidth, frameHeight);
            postInvalidate();
        }
        return this;
    }

    public ViewfinderView setFrameHeight(int height) {
        if (frameHeight != height) {
            initRect(frameWidth, height);
            postInvalidate();
        }
        return this;
    }

    public ViewfinderView setFrameWidth(int width) {
        if (frameWidth != width) {
            initRect(width, frameHeight);
            postInvalidate();
        }
        return this;
    }

    public void setFrameRect(Rect rect) {
        this.frameRect = rect;
        frameWidth = frameRect.right - frameRect.left;
        frameHeight = frameRect.bottom - frameRect.top;
        postInvalidate();
    }
}
