package com.tata.trackit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.tata.trackit.base.BaseActivity;

public class ActivityWorkDog extends BaseActivity implements OnClickListener {
    
    RelativeLayout start_work_dog_layout ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTitleAndContentView(R.layout.activity_work_dog, R.layout.default_title);
        initTitle();
        start_work_dog_layout = (RelativeLayout) findViewById(R.id.start_work_dog_layout);
        start_work_dog_layout.setOnClickListener(this);
    }


    public void initTitle() {
        txt_title_center.setText("遛狗");
        img_title_left.setImageResource(R.drawable.back_imagae);
        img_title_right.setImageResource(R.drawable.work_dog_title_right);
    }

    public static void showActivity(Context ctx) {
        Intent intent = new Intent(ctx, ActivityWorkDog.class);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            
        case R.id.framelayout_add_iv:
            break;
        case R.id.start_work_dog_layout :
            ActivityWorkRun.showActivity(this);
            
            break;
        default:
            break;
        }

    }

}
