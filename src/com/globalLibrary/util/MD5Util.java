package com.globalLibrary.util;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 工具类
 */
public final class MD5Util {
    /**
     * 获取指定文件的 MD5 值
     *
     * @param path 文件路径
     * @return MD5 值
     */
    public static String getFileMD5String(String path) {
        String result = null;
        if (!TextUtils.isEmpty(path)) {
            result = getFileMD5String(new File(path));
        }
        return result;
    }

    /**
     * 获取文件的 MD5 值
     *
     * @param file 目标文件
     * @return MD5 字符串
     */
    public static String getFileMD5String(File file) {
        String result = null;
        MessageDigest messageDigest = getMD5MessageDigest();
        if (messageDigest != null && file != null && file.exists() && file.isFile()) {
            FileInputStream in = null;
            FileChannel ch = null;
            try {
                in = new FileInputStream(file);
                ch = in.getChannel();
                ByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
                messageDigest.reset();
                messageDigest.update(byteBuffer);
                result = ByteUtil.bytesToHex(messageDigest.digest());
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                IOUtil.close(ch);
                IOUtil.close(in);
            }
        }
        return result;
    }

    private static MessageDigest getMD5MessageDigest() {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
        }
        return messageDigest;
    }

    /**
     * MD5 加密字符串
     *
     * @param str 目标字符串
     * @return MD5 加密后的字符串
     */
    public static String getMD5String(String str) {
        return TextUtils.isEmpty(str) ? null : getMD5String(str.getBytes());
    }

    /**
     * MD5 加密以 byte 数组表示的字符串
     *
     * @param bytes 目标数组
     * @return MD5加密后的字符串
     */
    public static String getMD5String(byte[] bytes) {
        String result = null;
        MessageDigest messageDigest = getMD5MessageDigest();
        if (messageDigest != null && bytes != null && bytes.length > 0) {
            messageDigest.reset();
            messageDigest.update(bytes);
            result = ByteUtil.bytesToHex(messageDigest.digest());
        }
        return result;
    }

    private MD5Util() {
    }
}
