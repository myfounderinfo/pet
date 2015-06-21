package com.pet.adpter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseAdapter<T> extends android.widget.BaseAdapter {
    protected List<T> list = null;
    protected Context context = null;

    private BaseAdapter() {
    }

    public BaseAdapter(Context context, List<T> list) {
        this.context = context;
        this.list = list;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        }

        return 0;
    }

    public void clearData() {
        if (list != null) {
            list.clear();
            notifyDataSetChanged();
        }
    }

    public void addData(List<T> mDate) {
        if (list == null) {
            list = new ArrayList<T>();
        }
        if (mDate != null) {
            list.addAll(mDate);
        }
    }

    @Override
    public T getItem(int position) {
        if (list != null && list.size() > position) {
            return list.get(position);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    // -------------------------------------------
    // item 中 某个控件 点击监听器
    // -------------------------------------------
//    public OnItemSubViewClickListener mOnItemSubViewClickListener;
//
//    public void setOnItemSubViewClickListener(OnItemSubViewClickListener listener) {
//        mOnItemSubViewClickListener = listener;
//    }
//
//    public final OnItemSubViewClickListener getOnItemSubViewClickListener() {
//        return mOnItemSubViewClickListener;
//    }
}
