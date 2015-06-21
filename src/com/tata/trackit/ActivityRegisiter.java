package com.tata.trackit;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.globalLibrary.util.AnimUtil;
import com.globalLibrary.util.LogUtil;
import com.globalLibrary.util.StringUtil;
import com.globalLibrary.util.ToastUtil;
import com.globalLibrary.view.EditInputView;
import com.tata.trackit.base.BaseActivity;

public class ActivityRegisiter extends BaseActivity  implements OnClickListener{
	private View edit_phone_number;
	private EditInputView edit_sms_code;
	private View lay_btn_next;
	private EditText edit_content;
	private TextView txt_send_sms_code;
	
	private CheckBox cb_agree_user_protocol,cb_agree_funds_protocol;
	private View lay_agree_user_protocol,lay_agree_funds_protocol;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initTitleAndContentView(R.layout.activity_regisiter, R.layout.default_title);
		initTitle();
		initView();
	}
    public void initView(){
    	edit_phone_number=findViewById(R.id.edit_phone_number);
    	edit_sms_code=(EditInputView) findViewById(R.id.edit_sms_code);
    	lay_btn_next=findViewById(R.id.lay_btn_next);
    	lay_btn_next.setOnClickListener(this);   	
    	edit_sms_code.setEditTitleText("中国+86");
    	edit_sms_code.getTitleTextView().setVisibility(View.INVISIBLE);
    	edit_sms_code.setLeftImageIconVisibility(false);
    	txt_send_sms_code=(TextView) findViewById(R.id.txt_send_sms_code);
    	edit_content=(EditText) findViewById(R.id.edit_content);
    	txt_send_sms_code.setOnClickListener(this);
    	cb_agree_user_protocol=(CheckBox) findViewById(R.id.cb_agree_user_protocol);
    	cb_agree_funds_protocol=(CheckBox) findViewById(R.id.cb_agree_funds_protocol);
    	lay_agree_user_protocol=findViewById(R.id.lay_agree_user_protocol);
    	lay_agree_funds_protocol=findViewById(R.id.lay_agree_funds_protocol);
    }
    public void sendSmsCode(){
    	if(StringUtil.isEmpty(edit_content.getText().toString())){
    		edit_phone_number.startAnimation(AnimUtil.shakeAnimation(5));
    		return;
    	}
    	try {
    		AVOSCloud.requestSMSCodeInBackgroud(edit_content.getText().toString(), new RequestMobileCodeCallback(){

				@Override
				public void done(AVException arg0) {
					// TODO Auto-generated method stub
					LogUtil.e(arg0.getMessage()+"  code="+arg0.getCode());
					
					ToastUtil.showToast(ActivityRegisiter.this, arg0.getMessage());
				}
				
			});
			
    	} catch (Exception e) {
			e.printStackTrace();
			ToastUtil.showToast(this, "发送失败");
		}
    }
    public void doNext(){
//    	if(StringUtil.isEmpty(edit_content.getText().toString())){
//    		edit_phone_number.startAnimation(AnimUtil.shakeAnimation(5));
//    		return;
//    	}
//    	if(StringUtil.isEmpty(edit_sms_code.getText().toString())){
//    		edit_sms_code.startAnimation(AnimUtil.shakeAnimation(5));
//    		return;
//    	}
//    	if(!cb_agree_user_protocol.isChecked()){
//    		lay_agree_user_protocol.startAnimation(AnimUtil.shakeAnimation(5));
//    		return;
//    	}
//    	if(!cb_agree_funds_protocol.isChecked()){
//    		lay_agree_funds_protocol.startAnimation(AnimUtil.shakeAnimation(5));
//    		return;
//    	}
//    	ToastUtil.showToast(this, "验证码错误");
        Intent intent = new Intent(this, ActivityInputBasicInfo.class);
        this.startActivity(intent);
    }
    public void initTitle(){
    	txt_title_center.setText("注册");
    	
    }
	public static void showActivity(Context ctx){
		Intent intent =new Intent(ctx,ActivityRegisiter.class);
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
		case R.id.txt_send_sms_code:
			sendSmsCode();
			break;
		case R.id.lay_btn_next:
			doNext();
			break;

		default:
			break;
		}
		
	}

	
	
}
