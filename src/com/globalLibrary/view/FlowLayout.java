package com.globalLibrary.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;









import java.util.ArrayList;

import com.tata.trackit.R;



/**
 * 流布局
 */
public class FlowLayout extends ViewGroup {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private ArrayList<ArrayList<View>> views = new ArrayList<ArrayList<View>>();
    // orientaion = VARTICAL 时，每列的宽度
    private SparseIntArray columnWidths = new SparseIntArray();

    // orientaion = HORIZONTAL 时，每行的高度
    private SparseIntArray lineHeights = new SparseIntArray();
    // orientaion = HORIZONTAL 时，每行的宽度
    private SparseIntArray lineWidths = new SparseIntArray();
    private int mGravity = Gravity.LEFT | Gravity.TOP;
    private int orientation = HORIZONTAL;

    public int getOrientation() {
        return orientation;
    }

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        orientation = typedArray.getInt(R.styleable.FlowLayout_orientation, HORIZONTAL);
        typedArray.recycle();
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        LayoutParams lp;
        for (ArrayList<View> viewList : views) {
            for (View view : viewList) {
                lp = (LayoutParams) view.getLayoutParams();
                view.layout(lp.left, lp.top, lp.right, lp.bottom);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        views.clear();

        int size = 0;
        ArrayList<View> lineOrColumnViews = null;
        View childView;
        LayoutParams lp;
        int lineOrColumnNum = 0;
        for (int i = 0, count = getChildCount(); i < count; i++) {
            if (lineOrColumnViews == null) {
                lineOrColumnViews = new ArrayList<View>();
                views.add(lineOrColumnViews);
            }
            childView = getChildAt(i);
            lp = (LayoutParams) childView.getLayoutParams();
            int[] childSize = getChildViewSize(childView, widthMeasureSpec, heightMeasureSpec);
            int childWidth = childView.getMeasuredWidth();
            int childHeight = childView.getMeasuredHeight();
            switch (orientation) {
                case HORIZONTAL:
                    if (size + childSize[0] > width) {
                        lineOrColumnViews = new ArrayList<View>();
                        views.add(lineOrColumnViews);
                        lineOrColumnViews.add(childView);
                        lineOrColumnNum++;
                        lineHeights.put(lineOrColumnNum, Math.max(lineHeights.get(lineOrColumnNum, 0), childSize[1]));
                        size = childSize[0];
                    } else {
                        lineOrColumnViews.add(childView);
                        lineHeights.put(lineOrColumnNum, Math.max(lineHeights.get(lineOrColumnNum, 0), childSize[1]));
                        size += childSize[0];
                    }
                    lp.top = sumSparseIntArray(lineHeights, 0, lineOrColumnNum);
                    lp.right = size;
                    lp.left = lp.right - childWidth;
                    lp.bottom = lp.top + childHeight;
                    break;
                case VERTICAL:
                    if (size + childSize[1] > height) {
                        lineOrColumnViews = new ArrayList<View>();
                        views.add(lineOrColumnViews);
                        lineOrColumnViews.add(childView);
                        columnWidths.put(lineOrColumnNum, Math.max(columnWidths.get(lineOrColumnNum, 0), childSize[0]));
                        size = childSize[1];
                        lineOrColumnNum++;
                    } else {
                        lineOrColumnViews.add(childView);
                        columnWidths.put(lineOrColumnNum, Math.max(columnWidths.get(lineOrColumnNum, 0), childSize[0]));
                        size += childSize[1];
                    }
                    lp.left = sumSparseIntArray(columnWidths, 0, lineOrColumnNum);
                    lp.top = size - childHeight;
                    lp.right = lp.left + childWidth;
                    lp.bottom = lp.top + childHeight;
                    break;
            }
        }
        align();
        switch (orientation) {
            case HORIZONTAL:
                setMeasuredDimension(width, getMeasureSize(sumSparseIntArray(lineHeights, 0, lineHeights.size()), heightMeasureSpec));
                break;
            case VERTICAL:
                setMeasuredDimension(getMeasureSize(sumSparseIntArray(columnWidths, 0, columnWidths.size()), widthMeasureSpec), height);
                break;
        }
    }

    // [width, height]
    private int[] getChildViewSize(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
        return getChildViewSize(child);
    }

    private void align() {
        switch (orientation) {
            case HORIZONTAL:
                alignHorizontal();
                break;
            case VERTICAL:
                alignVertical();
                break;
        }
    }

    private void alignVertical() {
        int column = -1;
        int changeSizeH = 0;
        LayoutParams lp;
        for (ArrayList<View> list : views) {
            column++;
            for (View view : list) {
                if (isRight()) {
                    changeSizeH = columnWidths.get(column, 0) - getChildViewSize(view)[0];
                } else if (isCenterHorizontal()) {
                    changeSizeH = (columnWidths.get(column, 0) - getChildViewSize(view)[0]) / 2;
                }
                lp = (LayoutParams) view.getLayoutParams();
                lp.left += changeSizeH;
                lp.right += changeSizeH;
            }
        }
    }

    private void alignHorizontal() {
        int line = -1;
        int changeSizeH = 0, changeSizeV = 0;
        LayoutParams lp;
        for (ArrayList<View> list : views) {
            line++;
            if (isRight()) {
                changeSizeH = getMeasuredWidth() - getLineWidth(line);
            } else if (isCenterHorizontal()) {
                changeSizeH = (getMeasuredWidth() - getLineWidth(line)) / 2;
            }
            for (View view : list) {
                lp = (LayoutParams) view.getLayoutParams();
                lp.left += changeSizeH;
                lp.right += changeSizeH;
                if (isCenterVertical()) {
                    changeSizeV = (getLineHeight(line) - getChildViewSize(view)[1]) / 2;
                } else if (isBottom()) {
                    changeSizeV = getLineHeight(line) - getChildViewSize(view)[1];
                }
                lp.top += changeSizeV;
                lp.bottom += changeSizeV;
            }
        }
    }

    private boolean isRight() {
        return (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT;
    }

    private boolean isCenterHorizontal() {
        int majorGravity = mGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        return majorGravity == Gravity.CENTER || majorGravity == Gravity.CENTER_HORIZONTAL;
    }

    private int getLineWidth(int line) {
        int lineWidth = lineWidths.get(line, -1);
        if (lineWidth == -1) {
            for (View view : views.get(line)) {
                lineWidth += getChildViewSize(view)[0];
            }
            lineWidths.put(line, lineWidth);
        }
        return lineWidth;
    }

    // [width, height]
    private int[] getChildViewSize(View child) {
        int[] result = {0, 0};
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        result[0] = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
        result[1] = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        return result;
    }

    private boolean isCenterVertical() {
        int majorGravity = mGravity & Gravity.VERTICAL_GRAVITY_MASK;
        return majorGravity == Gravity.CENTER || majorGravity == Gravity.CENTER_VERTICAL;
    }

    private boolean isBottom() {
        return (mGravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM;
    }

    private int getLineHeight(int line) {
        return lineHeights.get(line, 0);
    }

    private int getMeasureSize(int size, int parentMeasureSpec) {
        int result = MeasureSpec.getSize(parentMeasureSpec);
        switch (MeasureSpec.getMode(parentMeasureSpec)) {
            case MeasureSpec.AT_MOST:
                result = Math.min(size, result);
                break;
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
        }
        return result;
    }

    private int sumSparseIntArray(SparseIntArray sparseIntArray, int begin, int end) {
        int sum = 0;
        for (int i = begin; i < end; i++) {
            sum += sparseIntArray.get(i, 0);
        }
        return sum;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    // [left, top, right, bottom]
    private int[] getChildViewMargin(View child) {
        int[] result = {0, 0, 0, 0};
        if (child != null) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            result[0] = lp.leftMargin;
            result[1] = lp.topMargin;
            result[2] = lp.rightMargin;
            result[3] = lp.bottomMargin;
        }
        return result;
    }

    public int getGravity() {
        return mGravity;
    }

    public void setGravity(int mGravity) {
        this.mGravity = mGravity;
        if ((mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == 0) {
            this.mGravity |= Gravity.LEFT;
        }
        if ((mGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
            this.mGravity |= Gravity.TOP;
        }
        postInvalidate();
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
        postInvalidate();
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public int left, top, right, bottom;
        public int leftMargin, rightMargin, topMargin, bottomMargin;

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }
}
