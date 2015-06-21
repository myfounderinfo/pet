package com.globalLibrary.util;

import android.content.Context;
import android.widget.Toast;

/**
 * 主要用于显示Toast消息提示窗体
 * 
 * @author Frank 需要注意的是：Toast消息提示窗体不可以再try-catch块中的catch语句中调用，这样会导致程序直接崩溃
 */
public class ToastUtil {

	/**
	 * 显示的消息是自己定义的字符串
	 * 
	 * @param context
	 *            上下文参数
	 * @param str
	 *            自定义字符串
	 */
	public static void showToast(Context context, String str) {
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 显示的消息是资源文件中的字符串引用ID值
	 * @param context 上下文参数
	 * @param StringId 资源中字符串对应的id参数
	 */
	public static void showToast(Context context, int StringId) {
		Toast.makeText(context, StringId, Toast.LENGTH_SHORT).show();
	}
}
