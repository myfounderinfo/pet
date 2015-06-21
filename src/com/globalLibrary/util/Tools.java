package com.globalLibrary.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;


/**
 * @ClassInfo 个人工具类
 * @Author Frank
 * @CreateDate 2014年8月23日-下午2:20:15
 */
public class Tools {
    // 错误日志开关
    public static boolean showError = true;
    public static boolean DEBUGERROR = true;

    public static String TAG = "Myaiguang";


    /**
     * 判断是否为空
     *
     * @param string
     * @return
     */
    @SuppressLint("NewApi") 
    public static boolean NotNull(String string) {
        if (string != null && !string.isEmpty() && string.length() != 0) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * 判断List是否为空
     */
    public static boolean NotNull(ArrayList<?> list) {
        if (list != null && !list.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * List是否为空
     */
    public static boolean NotNull(List<?> list) {
        if (list != null && !list.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static <T> boolean NotNull(T[] arry) {
        if (arry != null && arry.length != 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isEmail(String email) {
    	String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
    	Pattern p = Pattern.compile(str);
    	Matcher m = p.matcher(email);
    	return m.matches();
    }
    
    //手机，11位 ，1开头，第二位3、4、5、8，
  	public static final String phone_mobile="^[1][3,4,5,8][0-9]{9}$";
    
  	//带区号座机，区号和号码之间用‘-’连接
  	public static final String phone_with_zip="^[0][1-9]{2,3}-[0-9]{5,10}$";
  	
  	//无区号座机
  	public static final String phone_without_zip="^[1-9]{1}[0-9]{5,8}$";
  	
  	//无区号座机
  	public static final String phone_400_or_800="^(400|800)[0-9]{7}$";
  	
  	//所有电话
  	public static final String phone_all_kind="("+phone_mobile+")|("+phone_with_zip+")|("+phone_without_zip+")|("+phone_400_or_800+")";
  	
	public static final String email="[a-zA-Z0-9_-]+@\\w+\\.[a-z]+(\\.[a-z]+)?";
	
	public static boolean isTelephone(String str) {
		Pattern pattern = Pattern.compile(phone_all_kind);
        return pattern.matcher(str).matches();
	}

    /*
     * 判断是否包含指定JsonArray,是否为空
     */
    public static boolean jsonNotNull(JSONObject object, String name)
            throws JSONException {
        if (!object.has(name) || object.getJSONArray(name) == null
                || object.getJSONArray(name).length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * 判断是否包含指定元素,是否为空
     */
    public static boolean NotNull(JSONObject object, String name)
            throws JSONException {
        if (!object.has(name) || !NotNull(object.getString(name))) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @param str
     * @return
     * @MethodsInfo：判断是否为空，为空返回""。不为空返回原字符串
     * @Author LengYue
     * @CreateDate 2014年9月1日-下午2:03:01
     * @Return String
     */
    public static String parseEmpty(String str) {
        if (str == null || "null".equals(str.trim())) {
            str = "";
        }
        return str.trim();
    }

    /**
     * @param str
     * @return
     * @MethodsInfo:利用正则表达式判断字符串是否全是数字
     * @Author LengYue
     * @CreateDate 2014年9月2日-下午1:25:50
     * @Return boolean
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }


    /**
     * 错误日志管理
     *
     * @param e
     */
    public static void error(Exception e) {
        if (showError) {
            Log.e(TAG, e.toString());
        }
    }

    public static void error(String e) {
        if (showError) {
            Log.e(TAG, e);
        }
    }

    public static void log(String string) {
        if (showError) {
            Log.i(TAG, string);
        }
    }

    public static void log(int string) {
        if (showError) {
            Log.i(TAG, string + "");
        }
    }

    public static void log(long string) {
        if (showError) {
            Log.i(TAG, string + "");
        }
    }

    public static void log(String tag, String value) {
        if (showError) {
            Log.i(tag, value);
        }
    }


    /**
     * 将view转换为bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache(true);
        return bitmap;
    }

    /**
     * 将图片转为byte[]
     *
     * @param bitmap
     * @return
     */
    public static byte[] bitmap2byte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * install app
     *
     * @param context
     * @param filePath
     * @return whether apk exist
     */
    public static boolean install(Context context, String filePath) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePath);
        if (file != null && file.length() > 0 && file.exists() && file.isFile()) {
            i.setDataAndType(Uri.parse("file://" + filePath),
                    "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return true;
        }
        return false;
    }

    /**
     * BitMap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        // canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    // 设置字体
    public static Typeface getTypeFace(Context context) {
        Typeface typeFace = Typeface.createFromAsset(context.getAssets(),
                "fonts/STHeiti-Light.ttc");
        return typeFace;
    }
    /**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dp2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dp(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
   
}