package com.globalLibrary.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.globalLibrary.util.ImageLoaderUtil;
import com.tata.trackit.R;

public class AddImageHorizontalScrollViewAdapter {

	private LayoutInflater mInflater;
	private List<String> mDatas;
	private Context mContext;

	public AddImageHorizontalScrollViewAdapter(Context context,
			List<String> mDatas) {
		mInflater = LayoutInflater.from(context);
		this.mDatas = mDatas;
		this.mContext = context;
	}

	public int getCount() {
		return mDatas.size();
	}

	public Object getItem(int position) {
		return mDatas.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.adapter_addded_image_item,
					parent, false);
			viewHolder.mImg = (ImageView) convertView
					.findViewById(R.id.ImgViewAdded);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		ImageLoaderUtil.getInstance(mContext).displayImage(
				mDatas.get(position), viewHolder.mImg);
		return convertView;
	}

	private class ViewHolder {
		ImageView mImg;
	}
}
