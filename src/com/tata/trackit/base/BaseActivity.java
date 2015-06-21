package com.tata.trackit.base;
import com.globalLibrary.util.DialogManager;
import com.tata.trackit.R;

import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class BaseActivity  extends Activity{
	
	public View lay_title_left;
	public ImageView img_title_left;
	public View lay_title_center;
	public TextView txt_title_center;
	public View lay_title_right;
	public ImageView img_title_right;
	public TextView txt_title_right;

	public void initTitleAndContentView(int content_layout, int title_layout) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(content_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, title_layout);
		lay_title_left = findViewById(R.id.lay_title_left);
		img_title_left = (ImageView) findViewById(R.id.img_title_left);
		lay_title_center = findViewById(R.id.lay_title_center);
		txt_title_center = (TextView) findViewById(R.id.txt_title_center);
		lay_title_right = findViewById(R.id.lay_title_right);
		txt_title_right = (TextView) findViewById(R.id.txt_title_right);
		img_title_right = (ImageView) findViewById(R.id.img_title_right);
	}

	protected void showProcessDialog(int res) {
		if (!DialogManager.isDialogShowing()) {
			DialogManager.showProgressDialog(this, res);
		}
	}

	protected void hideProcessDialog() {
		if (DialogManager.isDialogShowing()) {
			DialogManager.closeDialog();
		}
	}
	

}
