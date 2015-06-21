package com.tata.trackit;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.pet.adpter.HositoryRunSortAdapter;
import com.pet.bean.HostroySortBean;
import com.tata.trackit.base.BaseActivity;

public class ActivityWorkRunSort extends BaseActivity implements OnClickListener {

    ListView listview ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTitleAndContentView(R.layout.activity_run_work_sort, R.layout.default_title);
        initTitle();
        listview = (ListView) findViewById(R.id.listview);
        ArrayList<HostroySortBean> mList = new ArrayList<HostroySortBean>();
        for(int i = 0 ; i < 20 ;i++){
            HostroySortBean mHostroySortBean = new HostroySortBean();
            mHostroySortBean.comment=i+"";
            mHostroySortBean.runDistance=i+"km";
            mHostroySortBean.sort=(i+4)+"";
            mList.add(mHostroySortBean);
        }
        
        listview.setAdapter(new HositoryRunSortAdapter(this,mList));
    }
    public void initTitle() {
        txt_title_center.setText("今日排行");
        img_title_right.setImageResource(R.drawable.sort_history);
        img_title_right.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
               
                
                
                
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        default:
            break;
        }

    }

    public static void showActivity(Context ctx) {
        Intent intent = new Intent(ctx, ActivityWorkRunSort.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}
