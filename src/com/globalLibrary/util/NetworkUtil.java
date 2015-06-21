package com.globalLibrary.util;

import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Android 中和网络相关的工具方法
 */
public final class NetworkUtil {
    /**
     * 网络状态变化广播接收器
     */
    private static final BroadcastReceiver networkConnectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!NETWORKCONNECTIVITYCHANGELISTENERLIST.isEmpty()) {
                boolean isConnectivity = isNetworkConnectivity(context);
                for (NetworkConnectivityChangeListener listener : NETWORKCONNECTIVITYCHANGELISTENERLIST) {
                    listener.networkConnectivityChange(isConnectivity);
                }
            }
        }
    };
    /**
     * 注册的网络状态监听器
     */
    private static final List<NetworkConnectivityChangeListener> NETWORKCONNECTIVITYCHANGELISTENERLIST = new ArrayList<NetworkConnectivityChangeListener>();
    /**
     * 关闭 APN 网络时，添加的 APN 信息后缀
     */
    private static final String APN_SUFFIX = "_close";

    private static final String CTWAP = "ctwap";
    private static final String[] NETWORK_SETTING_ACTIVITY_CLASS_NAMES = {"com.android.settings.SettingsEMUI", "com.android.settings.SettingsTabActivity",
            "com.android.settings.Settings", "com.android.settings.WirelessSettings"};
    /**
     * 获取 APN 列表的 URI
     */
    private static final Uri APN_LIST_URI = Uri.parse("content://telephony/carriers");
    /**
     * 获取当前使用的 APN 的 URI
     */
    private static final Uri CURRENT_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    /**
     * 获取手机的 IP 地址
     * 需添加权限：
     * <pre>
     *     android.permission.INTERNET
     * </pre>
     *
     * @return 手机的 IP 地址。尚未联网或处于飞行模式时，返回 null
     */
    public static String getLocalIpAddress() {
        String result = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                if (result != null) {
                    break;
                }
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enIpAddress = networkInterface.getInetAddresses(); enIpAddress.hasMoreElements(); ) {
                    InetAddress inetAddress = enIpAddress.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        result = inetAddress.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 判断当前联网类型是否为 2G
     *
     * @param context 上下文
     * @return true 表示为 2G 网络
     */
    public static boolean is2GConnectivity(Context context) {
        boolean result = false;
        if (isNetworkConnectivity(context)) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch (telephonyManager.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    result = true;
                    break;
                default:
                    result = false;
                    break;
            }
        }
        return result;
    }

    /**
     * 判断当前网络是否已连接
     * 需添加权限：
     * <pre>
     *     android.permission.ACCESS_NETWORK_STATE
     * </pre>
     *
     * @param context 上下文
     * @return true 网络已连接
     */
    public static boolean isNetworkConnectivity(Context context) {
        if (!AppUtil.checkPermission(context, "android.permission.ACCESS_NETWORK_STATE")) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return !isAirplaneMode(context) && activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * 判断是否是飞行模式
     *
     * @param context 上下文
     * @return true 当前处于飞行模式
     */
    public static boolean isAirplaneMode(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    /**
     * 判断当前联网类型是否为 4G
     *
     * @param context 上下文
     * @return true 表示为 4G 网络
     */
    public static boolean is4GConnectivity(Context context) {
        boolean result = false;
        if (isNetworkConnectivity(context) && !isWifiConnectivity(context)) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch (telephonyManager.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_LTE:
                    result = true;
                    break;
                default:
                    result = false;
                    break;
            }
        }
        return result;
    }

    /**
     * 判断当前联网类型是否为 WIFI
     *
     * @param context 上下文
     * @return true 表示为 WIFI 网络，false 表示为 3G 或未联网
     */
    public static boolean isWifiConnectivity(Context context) {
        boolean result = false;
        if (isNetworkConnectivity(context)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            result = activeNetworkInfo != null && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return result;
    }

    /**
     * 判断当前联网类型是否为 CTWAP
     *
     * @param context 上下文
     * @return true 表示为 CTWAP 联网，false 表示其他网络类型或未联网
     */
    public static boolean isCTWapConnectivity(Context context) {
        boolean result = false;
        if (is3GConnectivity(context)) {
            result = CTWAP.equalsIgnoreCase(getNetworkType(context));
            if (!result) {
                APN apn = getDefaultAPN(context);
                result = apn != null && !TextUtils.isEmpty(apn.user) && apn.user.toLowerCase().contains(CTWAP);
            }
        }
        return result;
    }

    /**
     * 判断当前联网类型是否为 3G
     *
     * @param context 上下文
     * @return true 表示为 3G 网络
     */
    public static boolean is3GConnectivity(Context context) {
        boolean result = false;
        if (isNetworkConnectivity(context) && !isWifiConnectivity(context)) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch (telephonyManager.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    result = true;
                    break;
                default:
                    result = false;
                    break;
            }
        }
        return result;
    }

    /**
     * 获取联网类型
     * 需添加权限：
     * <pre>
     *     android.permission.ACCESS_NETWORK_STATE
     * </pre>
     *
     * @param context 上下文
     * @return 尚未联网或处于飞行模式时，返回 UNKNOWN
     */
    public static String getNetworkType(Context context) {
        String result = "UNKNOWN";
        if (AppUtil.checkPermission(context, "android.permission.ACCESS_NETWORK_STATE")) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && isNetworkConnectivity(context)) {
                switch (activeNetworkInfo.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        result = "WIFI";
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                    case ConnectivityManager.TYPE_MOBILE_DUN:
                    case ConnectivityManager.TYPE_MOBILE_HIPRI:
                    case ConnectivityManager.TYPE_MOBILE_MMS:
                    case ConnectivityManager.TYPE_MOBILE_SUPL:
                        APN curAPN = getDefaultAPN(context);
                        if (curAPN == null) {
                            result = getNetworkTypeName(activeNetworkInfo.getSubtype());
                        } else {
                            result = curAPN.apn.toUpperCase();
                        }
                        break;
                    default:
                        result = getNetworkTypeName(TelephonyManager.NETWORK_TYPE_UNKNOWN);
                        break;
                }
            }
        }
        return result;
    }

    private static String getNetworkTypeName(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "CDMA - EvDo rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "CDMA - EvDo rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "CDMA - EvDo rev. B";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "CDMA - 1xRTT";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "CDMA - eHRPD";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDEN";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * 获取当前默认 APN
     *
     * @param context 上下文
     * @return 当前默认 APN
     */
    public static APN getDefaultAPN(Context context) {
        Cursor c = context.getContentResolver().query(CURRENT_APN_URI, null, null, null, null);
        APN apn = null;
        if (c != null && c.moveToFirst()) {
            apn = getAPNInfoByCursor(c);
        }
        if (c != null) {
            c.close();
        }
        return apn;
    }

    /**
     * 获取 APN 信息
     *
     * @param cursor 游标
     * @return APN 对象
     */
    private static APN getAPNInfoByCursor(Cursor cursor) {
        APN apn = new APN();
        for (Field field : apn.getClass().getDeclaredFields()) {
            int columnIndex = cursor.getColumnIndex(field.getName());
            if (columnIndex != -1) {
                field.setAccessible(true);
                try {
                    field.set(apn, cursor.getString(columnIndex));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return apn;
    }

    /**
     * 关闭 APN，即使 APN 信息失效
     * 需添加权限：
     * <pre>
     *     android.permission.WRITE_APN_SETTINGS
     * </pre>
     *
     * @param context 上下文
     */
    public static void closeAPN(Context context) {
        if (AppUtil.checkPermission(context, "android.permission.WRITE_APN_SETTINGS")) {
            List<APN> apnList = getAPNList(context);
            ContentResolver contentResolver = context.getContentResolver();
            for (APN apn : apnList) {
                if (!isAPNClose(apn)) {
                    ContentValues values = new ContentValues();
                    values.put("apn", apn.apn + APN_SUFFIX);
                    values.put("type", apn.type + APN_SUFFIX);
                    values.put("user", apn.user + APN_SUFFIX);
                    contentResolver.update(APN_LIST_URI, values, "_id=?", new String[]{apn._id});
                }
            }
        }
    }

    /**
     * 获取 APN 列表
     *
     * @param context 上下文
     * @return APN 列表
     */
    public static List<APN> getAPNList(Context context) {
        List<APN> list = new ArrayList<APN>();
        Cursor c = context.getContentResolver().query(APN_LIST_URI, null, null, null, null);
        while (c != null && c.moveToNext()) {
            list.add(getAPNInfoByCursor(c));
        }
        if (c != null) {
            c.close();
        }
        return list;
    }

    /**
     * 判断 APN 是否已关闭
     *
     * @param apn APN 信息
     * @return true APN 已关闭，即失效
     */
    private static boolean isAPNClose(APN apn) {
        return (apn.apn != null && apn.apn.endsWith(APN_SUFFIX)) || (apn.type != null && apn.type.endsWith(APN_SUFFIX))
                || (apn.user != null && apn.user.endsWith(APN_SUFFIX));
    }

    /**
     * 打开 APN，即恢复失效的 APN 信息
     * 需添加权限：
     * <pre>
     *     android.permission.WRITE_APN_SETTINGS
     * </pre>
     *
     * @param context 上下文
     */
    public static void openAPN(Context context) {
        if (AppUtil.checkPermission(context, "android.permission.WRITE_APN_SETTINGS")) {
            List<APN> apnList = getAPNList(context);
            ContentResolver contentResolver = context.getContentResolver();
            for (APN apn : apnList) {
                if (isAPNClose(apn)) {
                    ContentValues values = new ContentValues();
                    values.put("apn", delApnSuffix(apn.apn));
                    values.put("type", delApnSuffix(apn.type));
                    values.put("user", delApnSuffix(apn.user));
                    contentResolver.update(APN_LIST_URI, values, "_id=?", new String[]{apn._id});
                }
            }
        }
    }

    /**
     * 删除 APN 信息中的后缀
     *
     * @param str 带后缀的 APN 信息
     * @return 去除后缀的 APN 信息
     */
    private static String delApnSuffix(String str) {
        return str.substring(0, str.lastIndexOf(APN_SUFFIX));
    }

    /**
     * 注册网络状态变化监听器
     *
     * @param context  上下文
     * @param listener 网络状态变化监听器
     */
    public static void registNetworkConnectivityChangeListener(Context context, NetworkConnectivityChangeListener listener) {
        if (listener != null && !NETWORKCONNECTIVITYCHANGELISTENERLIST.contains(listener)) {
            NETWORKCONNECTIVITYCHANGELISTENERLIST.add(listener);
            if (context != null) {
                registerReceiverSafe(context, networkConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            }
        }
    }

    private static void registerReceiverSafe(Context context, BroadcastReceiver broadcastReceiver, IntentFilter filter) {
        try {
            context.getApplicationContext().registerReceiver(broadcastReceiver, filter);
        } catch (Exception e) {
        }
    }

    /**
     * 设置飞行模式状态
     * 需添加权限：
     * <pre>
     *     android.permission.WRITE_SETTINGS
     * </pre>
     *
     * @param context          上下文
     * @param isAirplaneModeOn 是否开启飞行模式
     */
    public static void setAirplaneMode(Context context, boolean isAirplaneModeOn) {
        if (AppUtil.checkPermission(context, "android.permission.WRITE_SETTINGS")) {
            Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, isAirplaneModeOn ? 1 : 0);
            // 广播飞行模式的改变，让相应的程序可以处理
            // 不发送广播，在非飞行模式下，Android 2.2.1 上测试关闭了 WIFI，不关闭通话网络（GMS/GPRS等）
            // 不发送广播，在飞行模式下，Android 2.2.1 上测试无法关闭飞行模式
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            context.sendBroadcast(intent);
        }
    }

    /**
     * 设置默认 APN
     * 需添加权限：
     * <pre>
     *     android.permission.WRITE_APN_SETTINGS
     * </pre>
     *
     * @param context 上下文
     * @param apn_id  默认 APN 的 ID
     */
    public static void setDefaultAPN(Context context, String apn_id) {
        if (AppUtil.checkPermission(context, "android.permission.WRITE_APN_SETTINGS")) {
            ContentValues values = new ContentValues();
            values.put("apn_id", apn_id);
            context.getContentResolver().update(CURRENT_APN_URI, values, null, null);
        }
    }

    /**
     * 显示系统设置中的网络设置界面
     *
     * @param context 上下文
     */
    public static void showSystemNetworkSetting(Context context) {
        boolean isStart = false;
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        List<ResolveInfo> resolveInfos;
        for (String className : NETWORK_SETTING_ACTIVITY_CLASS_NAMES) {
            intent.setComponent(new ComponentName("com.android.settings", className));
            resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfos != null && !resolveInfos.isEmpty()) {
                isStart = true;
                ResolveInfo resolveInfo = resolveInfos.get(0);
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                try {
                    intent.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
                    context.startActivity(intent);
                } catch (Exception e) {
                    isStart = false;
                    LogUtil.simpleThrowable(e);
                }
                break;
            }
        }
        if (!isStart) {
            intent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
            intent.setComponent(null);
            context.startActivity(intent);
        }
    }

    /**
     * 反注册网络状态变化监听器
     *
     * @param context  上下文
     * @param listener 网络状态变化监听器
     */
    public static void unRegisterNetworkConnectivityChangeListener(Context context, NetworkConnectivityChangeListener listener) {
        if (NETWORKCONNECTIVITYCHANGELISTENERLIST.contains(listener)) {
            NETWORKCONNECTIVITYCHANGELISTENERLIST.remove(listener);
        }
        if (NETWORKCONNECTIVITYCHANGELISTENERLIST.isEmpty() && context != null) {
            unregisterReceiverSafe(context, networkConnectivityChangeReceiver);
        }
    }

    private static void unregisterReceiverSafe(Context context, BroadcastReceiver broadcastReceiver) {
        try {
            context.getApplicationContext().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
        }
    }

    private NetworkUtil() {
    }

    /**
     * APN 信息
     */
    public static class APN {
        public String _id;

        public String apn;

        public String authtype;

        public String current;

        public String name;

        public String type;

        public String user;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Field field : getClass().getDeclaredFields()) {
                try {
                    sb.append(field.getName()).append(" = ").append(field.get(this)).append(" ; ");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
    }

    /**
     * 网络状态变化监听器
     */
    public static interface NetworkConnectivityChangeListener {
        /**
         * 当网络连接状态变化时调用
         *
         * @param isConnectivity 当前网络是否可用
         */
        public void networkConnectivityChange(boolean isConnectivity);
    }
}
