package com.globalLibrary.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public final class AppUtil {
    public static final String FINISH_ALL_ACTIVITY_ACTION = "FINISH_ALL_ACTIVITY_ACTION";
    private static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    private static final String ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";

    /**
     * 判断应用程序是否在前台显示
     *
     * @param context 上下文
     * @return true 表示在前台显示
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean isOnForeground(Context context) {
        boolean foreground = false;
        String packageName = context.getPackageName();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : activityManager.getRunningAppProcesses()) {
            if (packageName.equals(appProcessInfo.processName) && appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                foreground = true;
                break;
            }
        }
        return foreground;
    }

    /**
     * 分享
     *
     * @param context 上下文
     * @param msg     要分享的文字信息
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void share(Context context, String msg) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 关闭应用程序
     */
    @SuppressWarnings({"UnusedDeclaration", "deprecation"})
    public static void exitApp(final Context context) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (Integer.parseInt(Build.VERSION.SDK) < 8) {
                manager.restartPackage(context.getPackageName());
            } else {
                // 结束所有服务
                List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(100);
                for (ActivityManager.RunningServiceInfo runningService : runningServices) {
                    if (runningService.process.startsWith(context.getPackageName())) {
                        Intent intent = new Intent();
                        intent.setComponent(runningService.service);
                        context.stopService(intent);
                    }
                }
                showHome(context);
                context.sendBroadcast(new Intent(FINISH_ALL_ACTIVITY_ACTION));
                // 结束进程
                new Thread() {
                    @Override
                    public void run() {
                        while (getRunningActivityNumber(context) > 0) {
                            try {
                                Thread.sleep(30);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示 Android 系统的 Home 桌面
     *
     * @param context 上下文
     */
    public static void showHome(Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(intent);
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.simpleThrowable(e);
        }
        return "V1.0";
    }

    /**
     * 检测当前程序是否具有指定权限
     *
     * @param context  上下文
     * @param permName 权限
     * @return true 表示具有该权限
     */
    public static boolean checkPermission(Context context, String permName) {
        return context.getPackageManager().checkPermission(permName, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 获取当前应用程序被打开的 Activity 界面数
     *
     * @param context 上下文
     * @return Activity 个数
     */
    public static int getRunningActivityNumber(Context context) {
        int num = 0;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(100);
        String packageName = context.getPackageName();
        for (ActivityManager.RunningTaskInfo taskInfo : runningTasks) {
            if (packageName.equals(taskInfo.baseActivity.getPackageName())) {
                num += taskInfo.numActivities;
            }
        }
        return num;
    }

    public static int getVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.simpleThrowable(e);
        }
        return 1;
    }

    /**
     * 调用相机获取图片
     * 除可通过 saveFile 参数保存剪裁后的图片外，也可在 onActivityResult 方法中通过
     * getParcelableExtra("data") 方法直接获取
     *
     * @param activity
     * @param requestCode
     * @param saveFile    图片
     */
    public static void getPictureFromCamera(Activity activity, int requestCode, File saveFile) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (saveFile != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(saveFile));
        }
        intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 调用相机获取图片并剪裁
     * 除可通过 saveFile 参数保存剪裁后的图片外，也可在 onActivityResult 方法中通过
     * getParcelableExtra("data") 方法直接获取
     *
     * @param activity
     * @param requestCode
     * @param width
     * @param height
     * @param saveFile    剪裁后的图片
     */
    public static void getPictureFromCameraAndCrop(Activity activity, int requestCode, int width, int height, File saveFile) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (saveFile != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(saveFile));
        }
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", width);// 输出图片大小
        intent.putExtra("outputY", height);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 从相册中选择图片并剪裁
     * 除可通过 saveFile 参数保存剪裁后的图片外，也可在 onActivityResult 方法中通过
     * getParcelableExtra("data") 方法直接获取
     *
     * @param activity
     * @param requestCode
     * @param width
     * @param height
     * @param saveFile    剪裁后的图片
     */
    public static void getPictureFromPhotoAlbumAndCrop(Activity activity, int requestCode, int width, int height, File saveFile) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        if (saveFile != null) {
            intent.putExtra("output", Uri.fromFile(saveFile));
        }
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", width);// 输出图片大小
        intent.putExtra("outputY", height);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 创建桌面快捷方式<br/>
     * 需添加权限：<br/>
     * <uses-permission android:name="com.android.launcher.permission.INSTALL_SHORTCUT"/>
     *
     * @param context 上下文
     * @param title   快捷方式显示名称
     * @param icon    快捷方式显示图标的资源 ID
     * @param intent  点击快捷方式时执行的动作
     */
    public static void installShortcut(Context context, String title, int icon, Intent intent) {
        Intent shortcutIntent = new Intent(ACTION_INSTALL_SHORTCUT);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, icon));
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        shortcutIntent.putExtra("duplicate", false); // 不允许重复创建
        context.sendBroadcast(shortcutIntent);
    }

    /**
     * 删除桌面快捷方式<br/>
     * 需添加权限：<br/>
     * <uses-permission android:name="com.android.launcher.permission.UNINSTALL_SHORTCUT"/>
     *
     * @param context 上下文
     * @param title   快捷方式显示名称
     * @param intent  点击快捷方式时执行的操作
     */
    public static void uninstallShortcut(Context context, String title, Intent intent) {
        Intent shortcutIntent = new Intent(ACTION_UNINSTALL_SHORTCUT);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        context.sendBroadcast(shortcutIntent);
    }

    private AppUtil() {
    }
}
