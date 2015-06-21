package com.globalLibrary.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 手机相关工具类，用户获取手机相关信息
 */
@SuppressWarnings("UnusedDeclaration")
public final class PhoneUtil {
    /**
     * 获取屏幕高度
     *
     * @param context 上下文
     * @return 屏幕高度
     */
    @SuppressWarnings("deprecation")
    public static int getScreenHeight(Context context) {
        Integer height;
        Display display = getDefaultDisplay(context);
        try {
            height = Reflect.on(display).call("getRawHeight").get();
        } catch (Reflect.ReflectException e) {
            height = display.getHeight();
        }
        return height;
    }

    private static Display getDefaultDisplay(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay();
    }

    /**
     * 获取屏幕宽度
     *
     * @param context 上下文
     * @return 屏幕宽度
     */
    @SuppressWarnings("deprecation")
    public static int getScreenWidth(Context context) {
        Integer width;
        Display display = getDefaultDisplay(context);
        try {
            width = Reflect.on(display).call("getRawWidth").get();
        } catch (Reflect.ReflectException e) {
            width = display.getWidth();
        }
        return width;
    }

    /**
     * 获取手机屏幕信息
     *
     * @return 手机屏幕信息，如尺寸等
     */
    public static DisplayMetrics getScreenInfo(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        getDefaultDisplay(context).getMetrics(dm);
        return dm;
    }

    /**
     * 获取手机内存信息<br/>
     * 实际上就是读取 /proc/meminfo 文件的内容<br/>
     * 返回的 Map 中，Key 是文件中的字段名，Value 为对应的数值，单位是：KB<br/>
     * 包含的字段及其含义：<br/>
     * <pre>
     *      MemTotal：所有可用RAM大小
     *      MemFree：LowFree与HighFree的总和，被系统留着未使用的内存
     *      Buffers：用来给文件做缓冲大小
     *      Cached：被高速缓冲存储器（cache memory）用的内存的大小（等于diskcache minus SwapCache）
     *      SwapCached：被高速缓冲存储器（cache memory）用的交换空间的大小。已经被交换出来的内存，仍然被存放在swapfile中，用来在需要的时候很快的被替换而不需要再次打开I/O端口
     *      Active：在活跃使用中的缓冲或高速缓冲存储器页面文件的大小，除非非常必要，否则不会被移作他用
     *      Inactive：在不经常使用中的缓冲或高速缓冲存储器页面文件的大小，可能被用于其他途径
     *      SwapTotal：交换空间的总大小
     *      SwapFree：未被使用交换空间的大小
     *      Dirty：等待被写回到磁盘的内存大小
     *      Writeback：正在被写回到磁盘的内存大小
     *      AnonPages：未映射页的内存大小
     *      Mapped：设备和文件等映射的大小
     *      Slab：内核数据结构缓存的大小，可以减少申请和释放内存带来的消耗
     *      SReclaimable：可收回Slab的大小
     *      SUnreclaim：不可收回Slab的大小（SUnreclaim+SReclaimable＝Slab）
     *      PageTables：管理内存分页页面的索引表的大小
     *      NFS_Unstable：不稳定页表的大小
     * </pre>
     *
     * @return 包含手机内存信息的 Map
     */
    public static Map<String, Integer> getMemoryInfo() {
        Map<String, Integer> result = new HashMap<String, Integer>();
        ShellUtil.CommandResult commandResult = ShellUtil.exec("cat /proc/meminfo", false);
        if (commandResult.resultCode == 0 && !StringUtil.isEmpty(commandResult.successMsg)) {
            for (String line : commandResult.successMsg.split("\n")) {
                String[] arrayOfLine = line.split("\\s*:*\\s+");
                if (arrayOfLine.length == 3) {
                    result.put(arrayOfLine[0], Integer.parseInt(arrayOfLine[1]));
                }
            }
        }
        return result;
    }

    /**
     * 获取 CPU 信息<br/>
     * 实际上就是读取 /proc/cpuinfo 文件的内容
     *
     * @return 包含 CPU 信息的 Map
     */
    public static Map<String, String> getCPUInfo() {
        Map<String, String> cpuInfo = new HashMap<String, String>();
        ShellUtil.CommandResult commandResult = ShellUtil.exec("cat /proc/cpuinfo", false);
        if (commandResult.resultCode == 0 && !StringUtil.isEmpty(commandResult.successMsg)) {
            for (String line : commandResult.successMsg.split("\n")) {
                String[] strs = line.split(":");
                if (strs.length == 2) {
                    cpuInfo.put(strs[0].trim(), strs[1].trim());
                }
            }
        }
        return cpuInfo;
    }

    /**
     * 获取客户端系统版本
     *
     * @return 客户端系统版本
     */
    public static String getClientOSVersion() {
        return "android " + Build.VERSION.RELEASE;
    }

    /**
     * 获取手机的电子串号
     *
     * @return 手机电子串号
     */
    public static String getEsn(Context mContext) {
        return getTelephonyManager(mContext).getDeviceId();
    }

    /**
     * 获取 IMEI 号<br/>
     * IMEI是International Mobile Equipment Identity （国际移动设备标识）的简称<br/>
     * IMEI由15位数字组成的”电子串号”，它与每台手机一一对应，而且该码是全世界唯一的<br/>
     * 其组成为：<br/>
     * 1. 前6位数(TAC)是”型号核准号码”，一般代表机型<br/>
     * 2. 接着的2位数(FAC)是”最后装配号”，一般代表产地<br/>
     * 3. 之后的6位数(SNR)是”串号”，一般代表生产顺序号<br/>
     * 4. 最后1位数(SP)通常是”0″，为检验码，目前暂备用<br/>
     *
     * @return IMEI 号
     */
    public static String getIMEI(Context mContext) {
        return getTelephonyManager(mContext).getDeviceId();
    }

    /**
     * 获取手机的 IMSI 号<br/>
     * IMSI是国际移动用户识别码的简称(International Mobile Subscriber Identity)<br/>
     * IMSI共有15位，其结构如下：<br/>
     * MCC+MNC+MIN<br/>
     * MCC：Mobile Country Code，移动国家码，共3位，中国为460;<br/>
     * MNC:Mobile NetworkCode，移动网络码，共2位<br/>
     * 在中国，移动的代码为电00和02，联通的代码为01，电信的代码为03<br/>
     * 合起来就是（也是Android手机中APN配置文件中的代码）：<br/>
     * 中国移动：46000 46002 46007<br/>
     * 中国联通：46001<br/>
     * 中国电信：46003<br/>
     * 举例，一个典型的IMSI号码为460030912121001<br/>
     *
     * @return IMSI 号
     */
    public static String getIMSI(Context context) {
        TelephonyManager telephonyManager = getTelephonyManager(context);
        int state = telephonyManager.getSimState();
        if (state == TelephonyManager.SIM_STATE_ABSENT || state == TelephonyManager.SIM_STATE_UNKNOWN) {
            return null;
        }
        return telephonyManager.getSubscriberId();
    }

    private static TelephonyManager getTelephonyManager(Context mContext) {
        return (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getModel() {
        String model = Build.MODEL;
        if ("sdk".equals(model)) {
            model = "XT800";
        }
        return model;
    }

    /**
     * 获取手机内置 CONFIG_UA
     *
     * @return CONFIG_UA
     */
    public static String getPhoneUA() {
        return Build.MODEL;
    }

    /**
     * 获取手机的品牌
     *
     * @return 手机品牌，没有则返回空串
     */
    public static String geteBrand() {
        return Build.BRAND == null ? "" : Build.BRAND;
    }

    /**
     * 判断是否是平板电脑
     *
     * @param context 上下文
     * @return true 表示是平板
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * 获取手机屏幕物理尺寸
     *
     * @param context 上下文
     * @return 屏幕物理尺寸
     */
    public static double getScreenPhysicalSize(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        getDefaultDisplay(context).getMetrics(dm);
        double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
        return diagonalPixels / (160 * dm.density);
    }

    /**
     * 获取手机存储卡路径，包括内置存储和外置存储
     *
     * @param context 上下文
     * @return 存储卡路径
     */
    public static String[] getSDCardPath(Context context) {
        String[] paths;
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            paths = Reflect.on(storageManager).call("getVolumePaths").get();
        } catch (Reflect.ReflectException e) {
            paths = null;
        }
        if (paths == null) {
            paths = new String[1];
            paths[0] = Environment.getExternalStorageDirectory().getPath();
        }
        return paths;
    }

    /**
     * 将 dp 单位的值换算成 px 单位
     *
     * @param dp dp 单位的数值
     * @return 转换后的以 px 为单位的数值
     */
    public static int dp2px(Context mContext, int dp) {
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5);
    }

    /**
     * 获取手机 SDK 版本
     *
     * @return 手机 SDK 版本
     */
    public static int getPhoneSDK() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取标题栏高度
     *
     * @param activity Activity 对象
     * @return 标题栏高度
     */
    public static int getTitleBarHeight(Activity activity) {
        return activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop() - getStatusBarHeight(activity);
    }

    /**
     * 获取状态栏高度
     *
     * @param activity Activity 对象
     * @return 状态栏高度
     */
    public static int getStatusBarHeight(Activity activity) {
        return getPhoneDisplaySize(activity).top;
    }

    /**
     * 获取手机屏幕显示区域大小，包括标题栏，不包括状态栏
     *
     * @param activity Activity 对象
     * @return 手机屏幕显示区域大小
     */
    public static Rect getPhoneDisplaySize(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect;
    }

    /**
     * 将 px 单位的值转换为 dp 单位
     *
     * @param px 以 px 为单位的数值
     * @return 转换后的以 dp 为单位的数值
     */
    public static int px2dp(Context mContext, int px) {
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5);
    }

    /**
     * 获取手机外部存储器的可用空间大小<br/>
     * 单位：byte
     *
     * @return 外部存储器不存在，则返回 -1
     */
    public static long getAvailableExternalMemorySize() {
        if (!hasSDCard()) {
            return -1;
        }
        File path = Environment.getExternalStorageDirectory();
        return getUsableSpace(path);
    }

    /**
     * 判断是否有SD卡
     *
     * @return 是否有可用 SD 卡
     */
    public static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取指定文件夹路径的可用空间大小
     *
     * @param path 文件夹
     * @return 可用空间大小
     */
    public static long getUsableSpace(File path) {
        if (path == null || path.isFile() || !path.exists()) {
            return 0;
        }
        StatFs statFs = new StatFs(path.getPath());
        return 1L * statFs.getBlockSize() * statFs.getAvailableBlocks();
    }

    /**
     * 获得手机内存的可用空间大小<br/>
     * 单位：byte
     *
     * @return 内存可用空间大小
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        return getUsableSpace(path);
    }

    /**
     * 获取手机外部存储器的总空间大小<br/>
     * 单位：byte
     *
     * @return 外部存储器不存在，则返回 -1
     */
    public static long getTotalExternalMemorySize() {
        if (!hasSDCard()) {
            return -1;
        }
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long blockCount = stat.getBlockCount();
        return 1L * blockSize * blockCount;
    }

    /**
     * 获取手机内存的总空间大小<br/>
     * 单位：byte
     *
     * @return 手机内存总大小
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return 1L * totalBlocks * blockSize;
    }

    private PhoneUtil() {
    }
    
	public static int px2sp(Context mContext, float pxValue) {
		float scale = mContext.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / scale + 0.5f);
	}
	
	public static void callPhone(Context ctx,String phoneNumber){
		Intent phoneIntent = new Intent("android.intent.action.CALL",Uri.parse("tel:" + phoneNumber));
		phoneIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(phoneIntent); 
	}
	public static void sendSMS(Context ctx,String phoneNumber,String message){
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNumber));          
        intent.putExtra("sms_body", message);       
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
}
}
