package com.tata.trackit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tata.trackit.base.BaseActivity;

public class ActivityInputPetInfo extends BaseActivity implements OnClickListener {
    
    //下一步
    View lay_btn_next;
    ImageView add_iv ;
    EditText edit_name;
    TextView tv_sex;
    EditText edit_age;
    //体检
    EditText edit_tijian;
    //上次疫苗
    EditText edit_yimiao;
    //活动区域
    EditText edit_huodongquyu;
    //排行
    EditText edit_paihang;
    //最大爱好
    EditText edit_max_love;
    //最爱谁
    EditText edit_must_love;
    //最喜欢吃
    EditText edit_must_like_eat;
    //讨厌的事
    EditText edit_taoyan_shiqing;
    //他最让你难忘的事情
    EditText edit_can_not_forget;
    //最想对它说的话
    EditText edit_must_like_say;
    //是否阉割/去掉卵巢
    TextView edit_is_can_has_baby;
    //是否对他人/动物友好
    TextView edit_is_friendy;
    //展示更多
    TextView show_more ;
    //添加其他狗狗
    TextView add_other_pet ;
    //其他信息系
    LinearLayout content_layout_otherinfo;
    //图片信息
    LinearLayout content_image_layout;
    boolean isShowMore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTitleAndContentView(R.layout.activity_input_pet_info, R.layout.default_title);
        initTitle();
        initView();
        txt_title_right.setText("跳过");
        txt_title_right.setTextColor(this.getResources().getColor(R.color.txt_blue));
        txt_title_right.setVisibility(View.VISIBLE);
        txt_title_right.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                
            }
        });
    }

    public void initView() {
        
        lay_btn_next = findViewById(R.id.lay_btn_next);
        
        add_iv = (ImageView) findViewById(R.id.add_image_iv);
        edit_name = (EditText) findViewById(R.id.edit_name);
        tv_sex = (TextView) findViewById(R.id.tv_sex);
        edit_age = (EditText) findViewById(R.id.edit_age);
        edit_tijian = (EditText) findViewById(R.id.edit_tijian);
        edit_yimiao = (EditText) findViewById(R.id.edit_yimiao);
        edit_huodongquyu = (EditText) findViewById(R.id.edit_huodongquyu);
        edit_paihang = (EditText) findViewById(R.id.edit_paihang);
        edit_max_love = (EditText) findViewById(R.id.edit_max_love);
        edit_must_love = (EditText) findViewById(R.id.edit_must_love);
        edit_must_like_eat = (EditText) findViewById(R.id.edit_must_like_eat);
        edit_taoyan_shiqing = (EditText) findViewById(R.id.edit_taoyan_shiqing);
        edit_can_not_forget = (EditText) findViewById(R.id.edit_can_not_forget);
        edit_must_like_say = (EditText) findViewById(R.id.edit_must_like_say);
        edit_is_can_has_baby = (TextView) findViewById(R.id.edit_is_can_has_baby);
        edit_is_friendy = (TextView) findViewById(R.id.edit_is_friendy);
        show_more = (TextView) findViewById(R.id.show_more);
        add_other_pet = (TextView) findViewById(R.id.add_other_pet);
        content_layout_otherinfo = (LinearLayout) findViewById(R.id.content_layout_otherinfo);
        content_image_layout = (LinearLayout) findViewById(R.id.content_image_layout);
        content_layout_otherinfo.setVisibility(View.GONE);
        show_more.setOnClickListener(this);
        lay_btn_next.setOnClickListener(this);
        isShowMore = false;
    }

    public void initTitle() {
        txt_title_center.setText("填写宠物资料");

    }

    public static void showActivity(Context ctx) {
        Intent intent = new Intent(ctx, ActivityInputPetInfo.class);
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
        case R.id.show_more :
            if(isShowMore){
                content_layout_otherinfo.setVisibility(View.GONE);
                isShowMore = false;
                show_more.setText("展开更多");
                
            }else{
                content_layout_otherinfo.setVisibility(View.VISIBLE);
                isShowMore = true;
                show_more.setText("收起");
            }
            break;
        case R.id.lay_btn_next :
            
//            Intent intent = new Intent(ActivityInputPetInfo.this, ActivityInputPetHelp.class);
//            ActivityInputPetInfo.this.startActivity(intent);
            ActivityMain.showActivity(this);
            break;
        default:
            break;
        }

    }

}
