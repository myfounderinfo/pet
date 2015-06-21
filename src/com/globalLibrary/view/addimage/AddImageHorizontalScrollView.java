package com.globalLibrary.view.addimage;

import java.util.HashMap;
import java.util.Map;

import com.tata.trackit.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
public class AddImageHorizontalScrollView extends HorizontalScrollView implements
		OnClickListener {

	public interface CurrentImageChangeListener {
		void onCurrentImgChanged(int position, View viewIndicator);
	}

	public interface OnItemClickListener {
		void onClick(View view, int pos);
	}

	private CurrentImageChangeListener mListener;

	private OnItemClickListener mOnClickListener;

	private OnItemClickListener mOnAddBtClickListener;
	
	private static final String TAG = "MyHorizontalScrollView";

	private LinearLayout mContainer;

	private int mChildWidth;

	private int mChildHeight;

	private int mCurrentIndex;

	private int mFristIndex;

	private AddImageHorizontalScrollViewAdapter mAdapter;

	private int mCountOneScreen;

	private int mScreenWitdh;

	private int mResBtAddImg;
	
	private boolean mIsBtAdded = false;

	private Map<View, Integer> mViewPos = new HashMap<View, Integer>();

	public AddImageHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 获得屏幕宽度
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		mScreenWitdh = outMetrics.widthPixels;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mContainer = (LinearLayout) getChildAt(0);
	}

	/**
	 * 加载下一张图片
	 */
	protected void loadNextImg() {
		// 数组边界值计算
		if (mCurrentIndex == mAdapter.getCount() - 1) {
			if (!mIsBtAdded) {
				attachAddBt();
			}
			return;
		}
		// 移除第一张图片，且将水平滚动位置置0
		scrollTo(0, 0);
		mViewPos.remove(mContainer.getChildAt(0));
		mContainer.removeViewAt(0);

		// 获取下一张图片，并且设置onclick事件，且加入容器中
		View view = mAdapter.getView(++mCurrentIndex, null, mContainer);
		view.setOnClickListener(this);
		mContainer.addView(view);
		mViewPos.put(view, mCurrentIndex);

		// 当前第一张图片小标
		mFristIndex++;
		// 如果设置了滚动监听则触发
		if (mListener != null) {
			notifyCurrentImgChanged();
		}

	}

	private void attachAddBt() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_addded_image_item, null, false);
        view.setId(R.id.bt_add_image);
        ImageView imageView = (ImageView) view.findViewById(R.id.ImgViewAdded);
		imageView.setImageResource(mResBtAddImg);
		imageView.setScaleType(ScaleType.CENTER_CROP);
		mContainer.addView(view);
		if (mOnAddBtClickListener != null) {
			view.setOnClickListener(this);
		}
		mIsBtAdded = true;
	}

	/**
	 * 加载前一张图片
	 */
	protected void loadPreImg() {

		// 如果当前已经是第一张，则返回
		if (mFristIndex == 0)
			return;
		// 获得当前应该显示为第一张图片的下标
		int index = mCurrentIndex - mCountOneScreen;
		if (index >= 0) {
			// mContainer = (LinearLayout) getChildAt(0);
			// 移除最后一张
			int oldViewPos = mContainer.getChildCount() - 1;
			mViewPos.remove(mContainer.getChildAt(oldViewPos));
			mContainer.removeViewAt(oldViewPos);

			// 将此View放入第一个位置
			View view = mAdapter.getView(index, null, mContainer);
			mViewPos.put(view, index);
			mContainer.addView(view, 0);
			view.setOnClickListener(this);
			// 水平滚动位置向左移动view的宽度个像素
			scrollTo(mChildWidth, 0);
			// 当前位置--，当前第一个显示的下标--
			mCurrentIndex--;
			mFristIndex--;
			// 回调
			if (mListener != null) {
				notifyCurrentImgChanged();
			}
		}
	}

	/**
	 * 滑动时的回调
	 */
	public void notifyCurrentImgChanged() {
		// 先清除所有的背景色，点击时会设置为蓝色
		for (int i = 0; i < mContainer.getChildCount(); i++) {
			mContainer.getChildAt(i).setBackgroundColor(Color.WHITE);
		}

		mListener.onCurrentImgChanged(mFristIndex, mContainer.getChildAt(0));

	}

	/**
	 * 初始化数据，设置数据适配器
	 * 
	 * @param mAdapter
	 */
	public void initDatas(AddImageHorizontalScrollViewAdapter mAdapter) {
		this.mAdapter = mAdapter;
		mContainer = (LinearLayout) getChildAt(0);
		mContainer.removeAllViews();
		// 获得适配器中第一个View
		if (mAdapter.getCount() <= 0) {
			attachAddBt();
			return;
		}
		final View view = mAdapter.getView(0, null, mContainer);
		mContainer.addView(view);

		// 强制计算当前View的宽和高
		if (mChildWidth == 0 && mChildHeight == 0) {
			int w = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			view.measure(w, h);
			mChildHeight = view.getMeasuredHeight();
			mChildWidth = view.getMeasuredWidth();
			Log.e(TAG, view.getMeasuredWidth() + "," + view.getMeasuredHeight());
			mChildHeight = view.getMeasuredHeight();
			// 计算每次加载多少个View
			mCountOneScreen = (mScreenWitdh / mChildWidth == 0) ? mScreenWitdh
					/ mChildWidth + 1 : mScreenWitdh / mChildWidth + 2;

			Log.e(TAG, "mCountOneScreen = " + mCountOneScreen
					+ " ,mChildWidth = " + mChildWidth);

		}
		// 初始化第一屏幕的元素
		initFirstScreenChildren(mCountOneScreen);
	}

	public void refresh() {
		initDatas(mAdapter);
	}
	/**
	 * 加载第一屏的View
	 * 
	 * @param mCountOneScreen
	 */
	public void initFirstScreenChildren(int mCountOneScreen) {
		mContainer = (LinearLayout) getChildAt(0);
		mContainer.removeAllViews();
		mViewPos.clear();

		for (int i = 0; (i < mCountOneScreen) && (i < mAdapter.getCount()); i++) {
			View view = mAdapter.getView(i, null, mContainer);
			view.setOnClickListener(this);
			mContainer.addView(view);
			mViewPos.put(view, i);
			mCurrentIndex = i;
			if (i == mAdapter.getCount() - 1) {
				attachAddBt();
			}
		}

		if (mListener != null) {
			notifyCurrentImgChanged();
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_MOVE:
			// Log.e(TAG, getScrollX() + "");

			int scrollX = getScrollX();
			// 如果当前scrollX为view的宽度，加载下一张，移除第一张
			if (scrollX >= mChildWidth && mAdapter.getCount() > 0) {
				loadNextImg();
			}
			// 如果当前scrollX = 0， 往前设置一张，移除最后一张
			if (scrollX == 0 && mAdapter.getCount() > 0) {
				loadPreImg();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bt_add_image && mOnAddBtClickListener != null) {
			mOnAddBtClickListener.onClick(v, 0);
			return;
		}
		if (mOnClickListener != null) {
			for (int i = 0; i < mContainer.getChildCount(); i++) {
				mContainer.getChildAt(i).setBackgroundColor(Color.WHITE);
			}
			mOnClickListener.onClick(v, mViewPos.get(v));
		}

	}

	public void setOnItemClickListener(OnItemClickListener mOnClickListener) {
		this.mOnClickListener = mOnClickListener;
	}
	
	public void setOnAddBtClickListner(OnItemClickListener mOnClickListener) {
		this.mOnAddBtClickListener = mOnClickListener;
	}

	public void setCurrentImageChangeListener(
			CurrentImageChangeListener mListener) {
		this.mListener = mListener;
	}

	public void setBtAddImgRes(int resBtAddImg) {
		this.mResBtAddImg = resBtAddImg;
	}

}
