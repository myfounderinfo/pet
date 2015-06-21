
/**
* Filename:    PetCircleAdapter.java
* Copyright:   Copyright (c)2010
* Company:     Founder Mobile Media Technology(Beijing) Co.,Ltd.g
* @version:    1.0
* @since:       JDK 1.6.0_21
* Create at:   2015-6-11 下午3:51:10
* Description:
* Modification History:
* Date     Author           Version           Description
* ------------------------------------------------------------------
* 2015-6-11    王涛             1.0          1.0 Version
*/
package com.pet.adpter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.pet.bean.HostroySortBean;
import com.tata.trackit.R;

public class HositoryRunSortAdapter extends BaseAdapter<HostroySortBean>{
    private final LayoutInflater mInflater;
    public HositoryRunSortAdapter(Context context, List list) {
        super(context, list);
        mInflater = LayoutInflater.from(context);
    }
    final static class ViewHolder {
        public TextView number;
        public ImageView icon_iv;
        public TextView run_distance;
        public TextView pet_circle_fav;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        HostroySortBean item = getItem(position);
        if (item == null) {
            return null;
        }
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.hostory_sort_info_item, null);
            viewHolder = new ViewHolder();
            viewHolder.number = (TextView) convertView.findViewById(R.id.number);
            viewHolder.icon_iv = (ImageView) convertView.findViewById(R.id.icon_iv);
            viewHolder.run_distance = (TextView) convertView.findViewById(R.id.run_distance);
            viewHolder.pet_circle_fav = (TextView) convertView.findViewById(R.id.pet_circle_fav);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        
        viewHolder.run_distance.setText(item.getRunDistance());
        viewHolder.pet_circle_fav.setText(item.getComment());
        return convertView;
    }

}



