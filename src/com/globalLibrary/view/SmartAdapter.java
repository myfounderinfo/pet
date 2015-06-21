package com.globalLibrary.view;

import android.os.Handler;
import android.os.Looper;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 列表适配器
 * 避免列表数据变化及列表更新发生在不同线程而造成的错误
 */
public abstract class SmartAdapter<T> extends BaseAdapter {
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private List<T> data = new ArrayList<T>();

    public SmartAdapter(List<T> list) {
        setData(list);
    }

    private void setData(List<T> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public T getItem(int i) {
        return i >= 0 && i < getCount() ? data.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public List<T> getData() {
        return Collections.unmodifiableList(data);
    }

    public void notifyDataSetChanged(final List<T> list) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                setData(list);
                notifyDataSetChanged();
            }
        });
    }
}
