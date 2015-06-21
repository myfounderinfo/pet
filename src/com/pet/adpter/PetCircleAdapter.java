
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

import com.pet.bean.CircleInfoBean;
import com.tata.trackit.R;

public class PetCircleAdapter extends BaseAdapter<CircleInfoBean>{
    private final LayoutInflater mInflater;
    public PetCircleAdapter(Context context, List list) {
        super(context, list);
        mInflater = LayoutInflater.from(context);
    }
    final static class ViewHolder {
        public ImageView pet_large_iv;
        public ImageView pet_icon_iv;
        public TextView pet_name;
        public TextView pet_info;
        public TextView pet_circle_public_time;
        public TextView pet_circle_comment;
        public TextView pet_circle_fav;
        public View show_comment_info_layout ;
        public ImageView show_comment_info;
        ImageView hide_iv ;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        CircleInfoBean item = getItem(position);
        if (item == null) {
            return null;
        }
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adpter_circle_item, null);
            viewHolder = new ViewHolder();
            viewHolder.pet_large_iv = (ImageView) convertView.findViewById(R.id.pet_large_iv);
            viewHolder.pet_icon_iv = (ImageView) convertView.findViewById(R.id.pet_icon_iv);
            viewHolder.pet_name = (TextView) convertView.findViewById(R.id.pet_name);
            viewHolder.pet_info = (TextView) convertView.findViewById(R.id.pet_info);
            viewHolder.pet_circle_fav = (TextView) convertView.findViewById(R.id.pet_circle_fav);
            viewHolder.pet_circle_public_time = (TextView) convertView.findViewById(R.id.pet_circle_public_time);
            viewHolder.pet_circle_comment = (TextView) convertView.findViewById(R.id.pet_circle_comment);
            viewHolder.show_comment_info_layout= convertView.findViewById(R.id.show_comment_info_layout);
            viewHolder.show_comment_info= (ImageView) convertView.findViewById(R.id.show_comment_info);
            viewHolder.hide_iv= (ImageView) convertView.findViewById(R.id.hide_iv);
            viewHolder.show_comment_info_layout.setVisibility(View.GONE);
            viewHolder.show_comment_info.setVisibility(View.GONE);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        
        viewHolder.pet_name.setText(item.getPetName());
        viewHolder.pet_name.setText(item.getPetName());
        viewHolder.pet_info.setText(item.getInfo());
        viewHolder.pet_circle_fav.setText(item.getFavCount());
        viewHolder.pet_circle_public_time.setText(item.getPublicTime());
        viewHolder.pet_circle_comment.setText(item.getCommentCount());
        viewHolder.show_comment_info.setTag(position);
        viewHolder.pet_circle_comment.setTag(viewHolder);
        viewHolder.hide_iv.setTag(viewHolder);
        viewHolder.hide_iv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ViewHolder viewHolder = (ViewHolder) v.getTag();
                viewHolder.show_comment_info_layout.setVisibility(View.GONE);
                viewHolder.show_comment_info.setVisibility(View.GONE);
                int position = (Integer) viewHolder.show_comment_info.getTag();
                getList().get(position).isShowComment=false;
            }
            
        });
        viewHolder.pet_circle_comment.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                ViewHolder viewHolder = (ViewHolder) v.getTag();
           
                int position = (Integer) viewHolder.show_comment_info.getTag();
                if(getList().get(position).isShowComment){
                    viewHolder.show_comment_info_layout.setVisibility(View.GONE);
                    viewHolder.show_comment_info.setVisibility(View.GONE);
                }else{
                    viewHolder.show_comment_info_layout.setVisibility(View.VISIBLE);
                    viewHolder.show_comment_info.setVisibility(View.VISIBLE);
                }
                getList().get(position).isShowComment = !getList().get(position).isShowComment;
            }
        });
        return convertView;
    }

}



