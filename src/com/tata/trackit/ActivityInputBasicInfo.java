package com.tata.trackit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tata.trackit.base.BaseActivity;

public class ActivityInputBasicInfo extends BaseActivity implements OnClickListener {
    
    //上传图片
    ImageView mUploadIv;
    //名称输入框
    EditText mEditName;
    //密码输入框
    EditText mEditPwd;
    //性别输入框
    TextView mEditSex;
    //密码可见
    ImageView mSeePwd;
    //宠物家长
    View petFamily;
    //宠物医生
    View petdoctor;
    //宠物爱好者
    View petLiker;
    //下一步
    View nextBt;
    ImageView pet_famile_iv ;
    TextView pet_famile_tv ;
    ImageView pet_doctor_iv ;
    TextView pet_doctor_tv ;
    ImageView pet_liker_iv ;
    TextView pet_liker_tv ;
    AlertDialog dialog ;
    boolean isSeePwd ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTitleAndContentView(R.layout.activity_input_basic_info, R.layout.default_title);
        initTitle();
        initView();
        isSeePwd = false;
    }

    public void initView() {
        
        pet_famile_iv  = (ImageView) findViewById(R.id.pet_famile_iv);
        pet_famile_tv  = (TextView) findViewById(R.id.pet_famile_tv);
        pet_doctor_iv  = (ImageView) findViewById(R.id.pet_doctor_iv);
        pet_doctor_tv  = (TextView) findViewById(R.id.pet_doctor_tv);
        pet_liker_iv  = (ImageView) findViewById(R.id.pet_liker_iv);
        pet_liker_tv  = (TextView) findViewById(R.id.pet_liker_tv);
        mUploadIv = (ImageView) findViewById(R.id.upload_iv);
        mEditName = (EditText) findViewById(R.id.edit_name);
        mEditPwd = (EditText) findViewById(R.id.edit_pwd);
        mEditSex = (TextView) findViewById(R.id.edit_sex);
        mSeePwd = (ImageView) findViewById(R.id.see_pwd);
        petFamily =findViewById(R.id.pet_family_layout);
        petdoctor =findViewById(R.id.pet_doctor_layout);
        petLiker =findViewById(R.id.pet_liker_layout);
        nextBt =(View) findViewById(R.id.lay_btn_next);
        nextBt.setOnClickListener(this);
        petFamily.setOnClickListener(this);
        petdoctor.setOnClickListener(this);
        petLiker.setOnClickListener(this);
        mSeePwd.setOnClickListener(this);
        mEditSex.setOnClickListener(this);
    }

    public void initTitle() {
        txt_title_center.setText("填写基本信息");

    }

    public static void showActivity(Context ctx) {
        Intent intent = new Intent(ctx, ActivityInputBasicInfo.class);
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
        case R.id.lay_btn_next:
            Intent intent = new Intent(ActivityInputBasicInfo.this, ActivityInputPetInfo.class);
            ActivityInputBasicInfo.this.startActivity(intent);
            
            break;
            
        case R.id.edit_sex :
            dialog =  new AlertDialog.Builder(ActivityInputBasicInfo.this)
            .setTitle("请选择")
            .setIcon(android.R.drawable.ic_dialog_info)                
            .setSingleChoiceItems(new String[] {"男","女"}, mEditSex.getText().toString().equals("男")?0:1, 
              new DialogInterface.OnClickListener() {
                                        
                 public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if(which == 0){
                        mEditSex.setText("男");
                    }else{
                        mEditSex.setText("女");
                    }
                 }
              }
            )
            .setNegativeButton("取消", null)
            .show();
            break;
        case R.id.pet_family_layout:
            pet_famile_iv.setImageResource(R.drawable.pet_famile_2);
            pet_doctor_iv.setImageResource(R.drawable.pet_doctor_1);
            pet_liker_iv.setImageResource(R.drawable.pet_liker_1);
            pet_famile_tv.setTextColor(this.getResources().getColor(R.color.txt_blue));
            pet_doctor_tv.setTextColor(this.getResources().getColor(R.color.txt_dark_gray));
            pet_liker_tv.setTextColor(this.getResources().getColor(R.color.txt_dark_gray));
            
            break;
        case R.id.pet_doctor_layout:
            pet_famile_iv.setImageResource(R.drawable.pet_famile_1);
            pet_doctor_iv.setImageResource(R.drawable.pet_doctor_2);
            pet_liker_iv.setImageResource(R.drawable.pet_liker_1);
            
            pet_famile_tv.setTextColor(this.getResources().getColor(R.color.txt_dark_gray));
            pet_doctor_tv.setTextColor(this.getResources().getColor(R.color.txt_blue));
            pet_liker_tv.setTextColor(this.getResources().getColor(R.color.txt_dark_gray));
            
            break;
        case R.id.pet_liker_layout:
            pet_famile_iv.setImageResource(R.drawable.pet_famile_1);
            pet_doctor_iv.setImageResource(R.drawable.pet_doctor_1);
            pet_liker_iv.setImageResource(R.drawable.pet_liker_2);
            
            pet_famile_tv.setTextColor(this.getResources().getColor(R.color.txt_dark_gray));
            pet_doctor_tv.setTextColor(this.getResources().getColor(R.color.txt_dark_gray));
            pet_liker_tv.setTextColor(this.getResources().getColor(R.color.txt_blue));
            
            break;
            
        case R.id.see_pwd :
            if(isSeePwd){
                
                isSeePwd = false;
                mSeePwd.setImageResource(R.drawable.see_pwd);
                mEditPwd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }else{
                
                isSeePwd = true;
                mEditPwd.setInputType(InputType.TYPE_CLASS_TEXT);
                mSeePwd.setImageResource(R.drawable.see_pwd_sure);
            }
            
            
            
            break;
        default:
            break;
        }

    }

}
