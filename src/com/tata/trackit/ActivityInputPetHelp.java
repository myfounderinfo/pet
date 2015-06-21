package com.tata.trackit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tata.trackit.base.BaseActivity;

public class ActivityInputPetHelp extends BaseActivity implements OnClickListener {
    
    //下一步
    View lay_btn_next;
    FrameLayout framelayout_add_iv ;
    //宠物名
    EditText edit_name ;
    //丢失时间
    EditText edit_diushishijian ;
    //丢失地址
    EditText edit_diushididian;
    //详细地址
    EditText edit_more_detail_place;
    //主人寄言
    EditText edit_master_info;
    //图片信息
    View content_image_layout ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTitleAndContentView(R.layout.activity_input_pet_help, R.layout.default_title);
        initTitle();
        initView();
      
    }

    public void initView() {
        
        lay_btn_next = findViewById(R.id.lay_btn_next);
        framelayout_add_iv = (FrameLayout) findViewById(R.id.framelayout_add_iv);
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_diushishijian = (EditText) findViewById(R.id.edit_diushishijian);
        edit_diushididian = (EditText) findViewById(R.id.edit_diushididian);
        edit_more_detail_place = (EditText) findViewById(R.id.edit_more_detail_place);
        edit_more_detail_place = (EditText) findViewById(R.id.edit_more_detail_place);
        edit_master_info = (EditText) findViewById(R.id.edit_master_info);
        content_image_layout = findViewById(R.id.content_image_layout);
        framelayout_add_iv.setOnClickListener(this);
    }

    public void initTitle() {
        txt_title_center.setText("发起呼救");

    }

    public static void showActivity(Context ctx) {
        Intent intent = new Intent(ctx, ActivityInputPetHelp.class);
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
        
        default:
            break;
        }

    }

}
