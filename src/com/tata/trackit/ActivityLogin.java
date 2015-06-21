package com.tata.trackit;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.globalLibrary.util.AnimUtil;
import com.globalLibrary.util.DialogManager;
import com.globalLibrary.util.StringUtil;
import com.globalLibrary.view.EditInputView;
import com.tata.trackit.base.BaseActivity;
public class ActivityLogin extends BaseActivity implements OnClickListener {
	private EditInputView  edit_phone,edit_pwd;
	private View lay_btn_login;
	private View lay_txt_regisiter;
	private boolean isPassword=true;
	public Handler ui_handler =new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			DialogManager.closeDialog();
			super.handleMessage(msg);
		}
		
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initTitleAndContentView(R.layout.activity_login, R.layout.default_title);
		initTitle();
		initView();
	}
	public void initTitle(){
		
	}
	public void initView(){
		edit_phone=(EditInputView) findViewById(R.id.edit_phone);
		edit_pwd=(EditInputView) findViewById(R.id.edit_pwd);
		edit_phone.setEditTitleText("");
		edit_phone.getEditContent().setHint("手机号码");
		edit_phone.getEditContent().setInputType(InputType.TYPE_CLASS_PHONE);
		edit_pwd.setEditTitleText("");
		EditText edit_pwd_edittext=edit_pwd.getEditContent();
		edit_pwd_edittext.setHint("登陆密码");
		edit_pwd.setRightImageIconVisibility(true);
		edit_pwd.setLeftImageIcon(R.drawable.icon_password);
		edit_pwd_edittext.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		lay_btn_login=findViewById(R.id.lay_btn_login);
		lay_btn_login.setOnClickListener(this);
		lay_txt_regisiter=findViewById(R.id.lay_txt_regisiter);
		lay_txt_regisiter.setOnClickListener(this);
		edit_pwd.getRightImageIcon().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isPassword){
					edit_pwd.getEditContent().setInputType(InputType.TYPE_CLASS_TEXT);
					edit_pwd.getEditContent().setSelection(edit_pwd.getEditContent().getText().length());
					isPassword=false;
				}else{
					edit_pwd.getEditContent().setInputType(InputType.TYPE_CLASS_TEXT
							| InputType.TYPE_TEXT_VARIATION_PASSWORD);
					edit_pwd.getEditContent().setSelection(edit_pwd.getEditContent().getText().length());
					isPassword=true;
				}
			}
		});
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
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.lay_btn_login:
//			doLogin();
		        ActivityMain.showActivity(this);
			break;
		case R.id.lay_txt_regisiter:
			ActivityRegisiter.showActivity(this);
			break;
		default:
			break;
		}
	}
	
	public void doLogin(){
		if(StringUtil.isEmpty(edit_phone.getText())){
			edit_phone.startAnimation(AnimUtil.shakeAnimation(5));
			return ;
		}
		if(StringUtil.isEmpty(edit_pwd.getText())){
			edit_pwd.startAnimation(AnimUtil.shakeAnimation(5));
			return ;
		}
		DialogManager.showProgressDialog(this, "正在登陆请稍后...");
		ui_handler.sendEmptyMessageDelayed(0, 2000);
	}
	

}
