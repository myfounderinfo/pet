package com.globalLibrary.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ImageLoaderUtil {
	private static final String TAG = "ImageLoaderUtil";
	private ImageLoader mImageLoader = null;
	private static ImageLoaderUtil msInstance = null;

	private ImageLoaderUtil() {

	}

	public static ImageLoaderUtil getInstance(Context context) {

		if (msInstance == null) {
			synchronized (ImageLoaderUtil.class) {
				msInstance = new ImageLoaderUtil();
				msInstance.mImageLoader = ImageLoader.getInstance();
				if (!msInstance.mImageLoader.isInited()) {
					msInstance.mImageLoader.init(ImageLoaderConfiguration
							.createDefault(context));
				}
			}
		}
		return msInstance;
	}
	
	public void displayImage(String imgUrl,ImageView imgView, DisplayImageOptions options) {
		if (TextUtils.isEmpty(imgUrl)) {
			Log.e(TAG, "Image url is empty!");
			return;
		}
		Log.d(TAG,"load image url = " + imgUrl);
		msInstance.mImageLoader.displayImage(imgUrl, imgView, options);
	}
	
	public void displayImage(String imgUrl,ImageView imgView) {
        DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
        .imageScaleType(ImageScaleType.EXACTLY).cacheInMemory(true)
        .cacheOnDisk(true).build();
		if (TextUtils.isEmpty(imgUrl)) {
			Log.e(TAG, "Image url is empty!");
			return;
		}
		msInstance.mImageLoader.displayImage(imgUrl, imgView, displayOptions);
	}
	
	public void loadImage(String imgUrl, ImageLoadingListener listener) {
		if (TextUtils.isEmpty(imgUrl)) {
			Log.e(TAG, "Image url is empty!");
			return;
		}
		msInstance.mImageLoader.loadImage(imgUrl, listener);
	}
}
