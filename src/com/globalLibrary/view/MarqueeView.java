package com.globalLibrary.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.globalLibrary.util.PhoneUtil;
import com.globalLibrary.util.StringUtil;








import com.tata.trackit.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 跑马灯
 */
public class MarqueeView extends View {
    private static final int DELAYED_TIME = 50;
    private final Handler scrollHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            mScrollSize = Math.min(mScrollSize + speed, getNextDisplayItemScroll());
            postInvalidate();
        }
    };
    // 根据控件大小、文字大小等处理后的数据，便于显示
    private List<Item> displayItems = new ArrayList<Item>();
    // 原始数据
    private List<Item> srcItems = new ArrayList<Item>();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float mScrollSize = 0F;// 滚动距离

    private float stringHeight;
    private float textSize = 20;
    private int direction = 0;
    private int displayItemIndex = 0;
    private int speed = 5;
    private int textColor = Color.BLACK;

    public MarqueeView(Context context) {
        super(context);
        setTextColor(textColor);
        setTextSize(textSize);
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        mPaint.setColor(textColor);
        stringHeight = StringUtil.getStringHeight(mPaint);
        createDiaplayItems();
    }

    private void createDiaplayItems() {
        displayItems.clear();
        float[] widths;
        int view_width = getWidth();
        if (view_width <= 0) {
            return;
        }
        float sum = 0;
        int begin;
        int end;
        Item displayItem;
        for (Item item : srcItems) {
            begin = end = 0;
            widths = StringUtil.getCharacterWidth(mPaint, item.msg);
            for (float word_width : widths) {
                end++;
                sum += word_width;
                if (sum >= view_width) {
                    displayItem = new Item();
                    displayItem.msg = item.msg.substring(begin, sum > view_width ? end - 1 : end);
                    displayItem.display_time = item.display_time;
                    displayItem.surplusSize = 0;
                    if (begin > 0) {
                        displayItem.isSplit = true;
                    }
                    displayItems.add(displayItem);
                    begin = sum > view_width ? end - 1 : end;
                    sum = 0;
                }
            }
            if (begin != end) {
                displayItem = new Item();
                displayItem.msg = item.msg.substring(begin, end);
                displayItem.display_time = item.display_time;
                displayItem.surplusSize = view_width - sum;
                if (begin > 0) {
                    displayItem.isSplit = true;
                }
                displayItems.add(displayItem);
                begin = 0;
                end = 0;
                sum = 0;
            }
        }
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        mPaint.setTextSize(textSize);
        stringHeight = StringUtil.getStringHeight(mPaint);
        requestLayout();
        createDiaplayItems();
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MarqueeView);
        textSize = typedArray.getDimension(R.styleable.MarqueeView_textSize, textSize);
        textColor = typedArray.getColor(R.styleable.MarqueeView_textColor, textColor);
        speed = typedArray.getInt(R.styleable.MarqueeView_speed, speed);
        direction = typedArray.getInt(R.styleable.MarqueeView_direction, direction);
        typedArray.recycle();
        setTextColor(textColor);
        setTextSize(textSize);
    }

    private float getNextDisplayItemScroll() {
        Item displayItem = displayItems.get(displayItemIndex);
        Item nextDisplayItem = displayItems.get(getNextDiaplayItemIndex());
        float nextDisplayItemScroll = 0;
        float divideSize = getDivideSize();
        switch (direction) {
            case 0:
                nextDisplayItemScroll = StringUtil.getStringWidth(mPaint, displayItem.msg) + displayItem.surplusSize;
                if (!nextDisplayItem.isSplit && divideSize > displayItem.surplusSize) {
                    nextDisplayItemScroll += divideSize - displayItem.surplusSize;
                }
                break;
            case 1:
                nextDisplayItemScroll = stringHeight + divideSize;
                break;
        }
        return nextDisplayItemScroll;
    }

    private int getNextDiaplayItemIndex() {
        return (displayItemIndex + 1) % displayItems.size();
    }

    private float getDivideSize() {
        return direction == 0 ? getWidth() / 4 : getHeight() - stringHeight;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (direction) {
            case 0:
                rightToLeft(canvas);
                break;
            case 1:
                bottomToTop(canvas);
                break;
        }
    }

    private void rightToLeft(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        Rect rect = new Rect(0, 0, width, height);
        Paint.FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
        float baseLine = rect.top + (rect.bottom - rect.top) / 2 - (fontMetricsInt.bottom - fontMetricsInt.top) / 2 - fontMetricsInt.top;
        int nextDiaplayItemIndex = getNextDiaplayItemIndex();
        Item displayItem = displayItems.get(displayItemIndex);
        float nextDisplayItemScroll = getNextDisplayItemScroll();

        canvas.save();
        canvas.translate(-mScrollSize, 0);
        canvas.clipRect(0, 0, width + mScrollSize, height);
        canvas.drawText(displayItem.msg, 0, baseLine, mPaint);
        boolean hasMoreItems = displayItems.size() > 1;
        if (hasMoreItems) {
            displayItem = displayItems.get(nextDiaplayItemIndex);
            canvas.drawText(displayItem.msg, nextDisplayItemScroll, baseLine, mPaint);
        }
        canvas.restore();

        scrollHandler.removeMessages(0);
        if (hasMoreItems) {
            if (mScrollSize == 0) {
                scrollHandler.sendEmptyMessageDelayed(0, displayItem.display_time * 1000);
            } else if (mScrollSize >= nextDisplayItemScroll) {// 下一条数据已经完整显示
                mScrollSize = 0;
                displayItemIndex = nextDiaplayItemIndex;
                scrollHandler.sendEmptyMessageDelayed(0, displayItem.display_time * 1000);
            } else {
                scrollHandler.sendEmptyMessageDelayed(0, DELAYED_TIME);
            }
        }
    }

    private void bottomToTop(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        Rect rect = new Rect(0, 0, width, height);
        Paint.FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
        float baseLine = rect.top + (rect.bottom - rect.top) / 2 - (fontMetricsInt.bottom - fontMetricsInt.top) / 2 - fontMetricsInt.top;
        int nextDiaplayItemIndex = getNextDiaplayItemIndex();
        Item displayItem = displayItems.get(displayItemIndex);
        float nextDisplayItemScroll = getNextDisplayItemScroll();

        canvas.save();
        canvas.translate(0, -mScrollSize);
        canvas.clipRect(0, 0, width, height + mScrollSize);
        canvas.drawText(displayItem.msg, 0, baseLine, mPaint);
        boolean hasMoreItem = displayItems.size() > 1;
        if (hasMoreItem) {
            displayItem = displayItems.get(nextDiaplayItemIndex);
            canvas.drawText(displayItem.msg, 0, baseLine + nextDisplayItemScroll, mPaint);
        }
        canvas.restore();

        if (hasMoreItem) {
            if (mScrollSize == 0) {
                scrollHandler.sendEmptyMessageDelayed(0, displayItem.display_time * 1000);
            } else if (mScrollSize >= nextDisplayItemScroll) {// 下一条数据已经完整显示
                mScrollSize = 0;
                displayItemIndex = nextDiaplayItemIndex;
                scrollHandler.sendEmptyMessageDelayed(0, displayItem.display_time * 1000);
            } else {
                scrollHandler.sendEmptyMessageDelayed(0, DELAYED_TIME);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            createDiaplayItems();
            postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int width = 0;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                width = size;
                break;
            case MeasureSpec.AT_MOST:
                width = (int) Math.min(size, getMaxWidth());
                break;
            case MeasureSpec.UNSPECIFIED:
                width = (int) Math.min(PhoneUtil.getScreenWidth(getContext()), getMaxWidth());
                break;
        }
        return width;
    }

    private float getMaxWidth() {
        float max = 0;
        for (Item item : srcItems) {
            max = Math.max(max, StringUtil.getStringWidth(mPaint, item.msg));
        }
        return max;
    }

    private int measureHeight(int heightMeasureSpec) {
        int height = 0;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                height = size;
                break;
            case MeasureSpec.AT_MOST:
                height = (int) Math.min(size, stringHeight + 10);
                break;
            case MeasureSpec.UNSPECIFIED:
                height = (int) (stringHeight + 10);
                break;
        }
        return height;
    }

    public void addMessage(String msg, long displayTime) {
        srcItems.add(new Item(msg, displayTime));
        createDiaplayItems();
        postInvalidate();
    }

    public void clearMessage() {
        srcItems.clear();
        createDiaplayItems();
        postInvalidate();
    }

    public void setDirection(int direction) {
        this.direction = direction;
        displayItemIndex = 0;
        postInvalidate();
    }

    private static class Item {
        public String msg;// 显示内容
        public boolean isSplit = false;// 是否为长句拆分得到，拆分得到的内容，显示时和前一条数据间为空白分隔
        public float surplusSize;// 文字显示完后，屏幕剩余的宽度，用于确定下一条数据显示的位置
        public long display_time;// 显示时间（秒），超过该时间后滚动显示下一条数据

        public Item() {
        }

        public Item(String msg, long display_time) {
            this.msg = msg;
            this.display_time = display_time;
        }
    }
}
