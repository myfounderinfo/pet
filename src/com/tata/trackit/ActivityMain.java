package com.tata.trackit;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.pet.adpter.PetCircleAdapter;
import com.pet.bean.CircleInfoBean;
import com.tata.trackit.base.BaseActivity;

public class ActivityMain extends BaseActivity implements OnClickListener {
    //好友
    TextView friend_tv ;
    //热门
    TextView hot_info;
    //附近
    TextView tv_near ;
    ListView listview;
    //下方选择卡
    //狗狗圈
    LinearLayout pet_cirle_layout;
    ImageView pet_cirle_iv;
    TextView pet_cirle_tv;
    //呼救
    LinearLayout pet_help_layout;
    ImageView pet_help_iv;
    TextView pet_help_tv;
    //聊天
    LinearLayout pet_chat_layout;
    ImageView pet_chat_iv;
    TextView pet_chat_tv;
    //设置
    LinearLayout pet_set_layout;
    ImageView pet_set_iv;
    TextView pet_set_tv;
    
    ListView mListView; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_input_pet_circle);
        friend_tv = (TextView) findViewById(R.id.friend_tv);
        hot_info = (TextView) findViewById(R.id.hot_info);
        tv_near = (TextView) findViewById(R.id.tv_near);
        SDKInitializer.initialize(this.getApplicationContext());
        mListView = (ListView) findViewById(R.id.listview);
        pet_cirle_layout = (LinearLayout) findViewById(R.id.pet_cirle_layout);
        pet_cirle_iv = (ImageView) findViewById(R.id.pet_cirle_iv);
        pet_cirle_tv = (TextView) findViewById(R.id.pet_cirle_tv);
        pet_help_layout = (LinearLayout) findViewById(R.id.pet_help_layout);
        pet_help_iv = (ImageView) findViewById(R.id.pet_help_iv);
        pet_help_tv = (TextView) findViewById(R.id.pet_help_tv);
        pet_chat_layout = (LinearLayout) findViewById(R.id.pet_chat_layout);
        pet_chat_iv = (ImageView) findViewById(R.id.pet_chat_iv);
        pet_chat_tv = (TextView) findViewById(R.id.pet_chat_tv);
        pet_set_layout = (LinearLayout) findViewById(R.id.pet_set_layout);
        pet_set_iv = (ImageView) findViewById(R.id.pet_set_iv);
        pet_set_tv = (TextView) findViewById(R.id.pet_set_tv);
        friend_tv.setOnClickListener(this);
        hot_info.setOnClickListener(this);
        tv_near.setOnClickListener(this);
        hot_info.setTextColor(this.getResources().getColor(R.color.white));
        hot_info.setBackgroundResource(R.drawable.bg_pet_cirle_f);
        pet_cirle_layout.setOnClickListener(this);
        pet_help_layout.setOnClickListener(this);
        pet_chat_layout.setOnClickListener(this);
        pet_set_layout.setOnClickListener(this);
        pet_cirle_iv.setBackgroundResource(R.drawable.pet_cirle_f);
        pet_cirle_tv.setTextColor(getResources().getColor(R.color.txt_blue));
        //测试数据
        ArrayList<CircleInfoBean> mList = new ArrayList<CircleInfoBean>();
        for(int i = 0 ; i < 10 ;i++){
            CircleInfoBean mCircleInfoBean = new CircleInfoBean();
            mCircleInfoBean.favCount = (i+1)+"" ;
            mCircleInfoBean.commentCount = (i+1)+"" ;
            mCircleInfoBean.publicTime = (i+1)+"分钟前" ;
            mCircleInfoBean.petName = (i+1)+"蛋炒饭" ;
            mCircleInfoBean.info = "每天待蛋炒饭出去遛弯，求介绍女朋友，它已经两岁了，欢迎有意者联系"+(i+1) ;
            mList.add(mCircleInfoBean);
        }
        
        mListView.setAdapter(new PetCircleAdapter(this,mList));
        
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ActivityWorkDog.showActivity(ActivityMain.this);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            
        case R.id.friend_tv:
            friend_tv.setTextColor(this.getResources().getColor(R.color.white));
            hot_info.setTextColor(this.getResources().getColor(R.color.black));
            tv_near.setTextColor(this.getResources().getColor(R.color.black));
            friend_tv.setBackgroundResource(R.drawable.bg_pet_cirle_left);
            hot_info.setBackground(null);
            tv_near.setBackground(null);
            break;
        case R.id.hot_info:
            
            friend_tv.setTextColor(this.getResources().getColor(R.color.black));
            hot_info.setTextColor(this.getResources().getColor(R.color.white));
            tv_near.setTextColor(this.getResources().getColor(R.color.black));
            friend_tv.setBackground(null);
            hot_info.setBackgroundResource(R.drawable.bg_pet_cirle_f);
            tv_near.setBackground(null);
            
            break;
        case R.id.tv_near:
            friend_tv.setTextColor(this.getResources().getColor(R.color.black));
            hot_info.setTextColor(this.getResources().getColor(R.color.black));
            tv_near.setTextColor(this.getResources().getColor(R.color.white));
            friend_tv.setBackground(null);
            hot_info.setBackground(null);
            tv_near.setBackgroundResource(R.drawable.bg_pet_cirle_right);
            
            break;
        case R.id.pet_cirle_layout:
            
            pet_cirle_iv.setBackgroundResource(R.drawable.pet_cirle_f);
            pet_cirle_tv.setTextColor(getResources().getColor(R.color.txt_blue));
            pet_help_iv.setBackgroundResource(R.drawable.tab2);
            pet_help_tv.setTextColor(getResources().getColor(R.color.txt_dark_gray));
            pet_chat_iv.setBackgroundResource(R.drawable.tab3);
            pet_chat_tv.setTextColor(getResources().getColor(R.color.txt_dark_gray));
            pet_set_iv.setBackgroundResource(R.drawable.tab4);
            pet_set_tv.setTextColor(getResources().getColor(R.color.txt_dark_gray));
            
            break;
        case R.id.pet_help_layout:
            
            pet_cirle_iv.setBackgroundResource(R.drawable.pet_cirle_d);
            pet_cirle_tv.setTextColor(getResources().getColor(R.color.txt_dark_gray));
            pet_help_iv.setBackgroundResource(R.drawable.tab2_a);
            pet_help_tv.setTextColor(getResources().getColor(R.color.txt_blue));
            pet_chat_iv.setBackgroundResource(R.drawable.tab3);
            pet_chat_tv.setTextColor(getResources().getColor(R.color.txt_dark_gray));
            pet_set_iv.setBackgroundResource(R.drawable.tab4);
            pet_set_tv.setTextColor(getResources().getColor(R.color.txt_dark_gray));
            
            break;
        case R.id.pet_chat_layout:
            
            pet_cirle_iv.setBackgroundResource(R.drawable.pet_cirle_d);
            pet_cirle_tv.setTextColor(getResources().getColor(R.color.txt_dark_gray));
            pet_help_iv.setBackgroundResource(R.drawable.tab2);
            pet_help_tv.setTextColor(getResources().getColor(R.color.txt_dark_gray));
            pet_chat_iv.setBackgroundResource(R.drawable.tab3_a);
            pet_chat_tv.setTextColor(getResources().getColor(R.color.txt_blue));
            pet_set_iv.setBackgroundResource(R.drawable.tab4);
            pet_set_tv.setTextColor(getResources().getColor(R.color.txt_dark_gray));
            
            break;
        case R.id.pet_set_layout:
            
            pet_cirle_iv.setBackgroundResource(R.drawable.pet_cirle_d);
            pet_cirle_tv.setTextColor(getResources().getColor(R.color.txt_dark_gray));
            pet_help_iv.setBackgroundResource(R.drawable.tab2);
            pet_help_tv.setTextColor(getResources().getColor(R.color.txt_dark_gray));
            pet_chat_iv.setBackgroundResource(R.drawable.tab3);
            pet_chat_tv.setTextColor(getResources().getColor(R.color.txt_dark_gray));
            pet_set_iv.setBackgroundResource(R.drawable.tab4_a);
            pet_set_tv.setTextColor(getResources().getColor(R.color.txt_blue));
            
            break;
        default:
            break;
        }

    }
    public static void showActivity(Context ctx) {
        Intent intent = new Intent(ctx, ActivityMain.class);
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
