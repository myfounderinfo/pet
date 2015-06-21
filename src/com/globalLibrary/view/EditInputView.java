package com.globalLibrary.view;

import com.globalLibrary.util.StringUtil;
import com.handmark.pulltorefresh.library.internal.Utils;
import com.tata.trackit.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EditInputView extends LinearLayout {
    private LinearLayout mRootView;
    private Context mContext;
    private ImageView img_edit_icon;
    private TextView edit_title;
    private EditText edit_content;
    private ImageView img_edit_right_icon;
    
	public EditInputView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext=context;
		init();
	}

	public EditInputView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext=context;
		init();
	}
	
	
	public void init(){		
		mRootView=(LinearLayout) View.inflate(mContext, R.layout.view_edit_input, null);
		LinearLayout.LayoutParams lp= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		addView(mRootView,lp);
		img_edit_icon=(ImageView) findViewById(R.id.img_edit_icon);
		edit_title=(TextView) findViewById(R.id.edit_title);
		edit_content=(EditText) findViewById(R.id.edit_content);
		img_edit_right_icon=(ImageView) findViewById(R.id.img_edit_right_icon);
	}
	
	
	public void setLeftImageIconVisibility(boolean visibility){
		img_edit_icon.setVisibility(visibility?View.VISIBLE:View.GONE);
	}
	
	public void setLeftImageIcon(int resid){
		setLeftImageIconVisibility(true);
		try {
			img_edit_icon.setImageResource(resid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void setEditTitleText(String title){
		if(StringUtil.isEmpty(title)){
			edit_title.setVisibility(View.GONE);
		}else{
			edit_title.setVisibility(View.VISIBLE);
			edit_title.setText(title);
		}
		
	}
	
	public void setRightImageIconVisibility(boolean visibility){
		img_edit_right_icon.setVisibility(visibility?View.VISIBLE:View.GONE);
	}
	
	public void setRightImageIcon(int resid){
		setLeftImageIconVisibility(true);
		try {
			img_edit_right_icon.setImageResource(resid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public ImageView getRightImageIcon(){
		return img_edit_right_icon;
	}
	public EditText getEditContent(){
		return edit_content;
	}
	public String getText(){
		return edit_content.getText().toString();
	}

	public TextView getTitleTextView(){
		return edit_title;
	}
}
