package com.globalLibrary.util;


import com.tata.trackit.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
public class DialogManager {
    private static MyDialog mDialog;

    public static Builder buildDialog(Context context) {
        return new Builder(context);
    }

    public static void closeDialog() {
        if (isDialogShowing()) {
            try {
                mDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static boolean isDialogShowing() {
        return mDialog != null && mDialog.isShowing();
    }

    public static void showProgressDialog(Context context, String msg) {
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 15;
        lp.bottomMargin = 15;
        lp.leftMargin = 15;
        lp.rightMargin = 15;
        linearLayout.addView(new ProgressBar(context), lp);
        TextView txtView = new TextView(context);
        txtView.setText(msg);
        txtView.setTextColor(Color.BLACK);
        linearLayout.addView(txtView);
        buildDialog(context).setTitle("提示").setView(linearLayout).setCancelable(false).show();
    }

    public static void showProgressDialog(Context context, int msgId) {
    	String msg = context.getResources().getString(msgId);
    	showProgressDialog(context, msg);
    }

    private DialogManager() {
    }

    private static class MyDialog extends Dialog {
        private static LinearLayout.LayoutParams btnLayoutParames;
        private MyDialogViewHolder viewHolder;

        static {
            btnLayoutParames = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            btnLayoutParames.weight = 1;
        }

        public MyDialog(Context context) {
            super(context);
            init(context);
        }

        private void init(Context context) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            View view = View.inflate(context, R.layout.dialog, null);
            super.setContentView(view);
            viewHolder = new MyDialogViewHolder(view);
        }

        @Override
        public void setTitle(CharSequence title) {
            if (!TextUtils.isEmpty(title)) {
                viewHolder.dialogTitle.setVisibility(View.VISIBLE);
                viewHolder.dialogTitleText.setVisibility(View.VISIBLE);
                viewHolder.dialogTitleText.setText(title);
            } else {
                viewHolder.dialogTitleText.setVisibility(View.GONE);
                viewHolder.dialogTitle.setVisibility(View.GONE);
            }
        }

        public void cancelButton(String text, View.OnClickListener onClickListener) {
            if (!TextUtils.isEmpty(text)) {
                viewHolder.buttonBar.setVisibility(View.VISIBLE);
                if (viewHolder.btnOK.getVisibility() == View.VISIBLE) {
                    viewHolder.btn_split.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.btn_split.setVisibility(View.GONE);
                }
                viewHolder.btnCancel.setText(text);
                viewHolder.btnCancel.setOnClickListener(onClickListener);
                viewHolder.btnCancel.setVisibility(View.VISIBLE);
            }
        }

        public void okButton(String text, View.OnClickListener onClickListener) {
            if (!TextUtils.isEmpty(text)) {
                viewHolder.buttonBar.setVisibility(View.VISIBLE);
                if (viewHolder.btnCancel.getVisibility() == View.VISIBLE) {
                    viewHolder.btn_split.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.btn_split.setVisibility(View.GONE);
                }
                viewHolder.btnOK.setText(text);
                viewHolder.btnOK.setOnClickListener(onClickListener);
                viewHolder.btnOK.setVisibility(View.VISIBLE);
            }
        }

        public void setMessage(String msg) {
            if (!TextUtils.isEmpty(msg)) {
                viewHolder.dialogMsg.setText(msg);
            }
        }

        public void setView(View view) {
            if (view != null) {
                viewHolder.dialogCententView.removeAllViews();
                ViewParent parent = view.getParent();
                if (parent instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) parent;
                    viewGroup.removeView(view);
                }
                viewHolder.dialogCententView.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    private static class MyDialogViewHolder {
        public final Button btnCancel;
        public final Button btnOK;
        public final LinearLayout buttonBar;
        public final LinearLayout dialogCententView;
        public final LinearLayout dialogTitle;
        public final TextView dialogMsg;
        public final TextView dialogTitleText;
        public final View btn_split;

        private MyDialogViewHolder(View root) {
            dialogTitle = (LinearLayout) root.findViewById(R.id.dialogTitle);
            dialogTitleText = (TextView) root.findViewById(R.id.dialogTitleText);
            dialogCententView = (LinearLayout) root.findViewById(R.id.dialogCententView);
            dialogMsg = (TextView) root.findViewById(R.id.dialogMsg);
            buttonBar = (LinearLayout) root.findViewById(R.id.buttonBar);
            btnOK = (Button) root.findViewById(R.id.btnOK);
            btnCancel = (Button) root.findViewById(R.id.btnCancel);
            btn_split = root.findViewById(R.id.btn_split_2);
        }
    }

    public static class Builder {
        private MyDialog dialog;

        public Builder(Context context) {
            dialog = new MyDialog(context);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.width = (int) (PhoneUtil.getScreenWidth(context) * 0.8);
            window.setAttributes(attributes);
            window.setBackgroundDrawable(null);
        }

        public Builder cancelButton(String text, View.OnClickListener onClickListener) {
            dialog.cancelButton(text, onClickListener);
            return this;
        }

        public Builder okButton(String text, View.OnClickListener onClickListener) {
            dialog.okButton(text, onClickListener);
            return this;
        }

        public Builder setCancelable(boolean flag) {
            dialog.setCancelable(flag);
            return this;
        }

        public Builder setListMsg(AdapterView.OnItemClickListener listener, String... items) {
            ListView listView = new ListView(dialog.getContext());
            listView.setDivider(dialog.getContext().getResources().getDrawable(R.drawable.divider));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(dialog.getContext(), R.layout.dialog_list_item, R.id.dlgItem, items);
            listView.setAdapter(adapter);
            if (listener != null) {
                listView.setOnItemClickListener(listener);
            }
            return setView(listView);
        }

        public Builder setView(View view) {
            dialog.setView(view);
            return this;
        }

        public Builder setMessage(String msg) {
            dialog.setMessage(msg);
            return this;
        }

        public Builder setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
            dialog.setOnCancelListener(onCancelListener);
            return this;
        }

        public Builder setTitle(String title) {
            dialog.setTitle(title);
            return this;
        }

        public void show() {
            try{
                closeDialog();
                mDialog = dialog;
                dialog.show();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
