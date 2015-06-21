package com.globalLibrary.util;

import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 */
public final class StringUtil {
    private static final Pattern ALL_WHITESPACE_PATTERN = Pattern.compile("\\s*|\r|\n|\t");

    private static final String[] ENCODINGS = {"UTF-8", "GBK", "GB2312", "ISO-8859-1"};

    /**
     * 按最大长度切分字符串
     *
     * @param paint    画笔
     * @param str      目标字符串
     * @param maxWidth 最大宽度
     * @return 切分后的字符串数组
     */
    public static String[] splitString(TextPaint paint, String str, int maxWidth) {
        List<String> list = new ArrayList<String>();
        StaticLayout layout = new StaticLayout(str, paint, maxWidth, Layout.Alignment.ALIGN_CENTER, 1F, 0F, true);
        for (int i = 0, count = layout.getLineCount(); i < count; i++) {
            list.add(str.substring(layout.getLineStart(i), layout.getLineEnd(i)));
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 删除字符串中所有的不可见字符
     *
     * @param str 源字符串
     * @return 删除所有不可见字符后的字符串
     */
    public static String delAllWhitespace(String str) {
        String result = str;
        if (str != null) {
            Matcher matcher = ALL_WHITESPACE_PATTERN.matcher(str);
            result = matcher.replaceAll("");
        }
        return result;
    }

    /**
     * 获取字符串的编码格式
     *
     * @param str 待检测的字符串
     * @return 编码格式
     */
    public static String getEncoding(String str) {
        String encoding = null;
        if (!isEmpty(str)) {
            for (String encode : ENCODINGS) {
                try {
                    if (str.equals(new String(str.getBytes(encode), encode))) {
                        encoding = encode;
                        break;
                    }
                } catch (UnsupportedEncodingException e) {
                }
            }
        }
        return encoding;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0 || delAllWhitespace(str).length() == 0;
    }

    /**
     * 使用指定的分隔符连接列表中的项
     *
     * @param strings 待连接的对象
     * @param sep     分隔符
     * @return 连接后的字符串
     */
    public static String join(Iterator strings, String sep) {
        if (strings == null || !strings.hasNext()) {
            return "";
        }
        String start = strings.next().toString();
        if (!strings.hasNext()) {
            return start;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append(start);
        while (strings.hasNext()) {
            sb.append(sep).append(strings.next().toString());
        }
        return sb.toString();
    }

    public static String join(Collection strings, String sep) {
        if (strings == null) {
            return "";
        } else {
            return join(strings.iterator(), sep);
        }
    }

    /**
     * 获取文本行的高度
     *
     * @param paint 画笔
     * @return 高度
     */
    public static float getStringHeight(Paint paint) {
        float height = 0F;
        if (paint != null) {
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            height = fontMetrics.bottom - fontMetrics.top;
        }
        return height;
    }

    /**
     * 获取字符串在屏幕上的显示宽度
     *
     * @param paint 画笔
     * @param str   目标字符串
     * @return 字符串的显示宽度
     */
    public static float getStringWidth(Paint paint, String str) {
        float width = 0F;
        float[] widths = getCharacterWidth(paint, str);
        if (widths != null) {
            for (float f : widths) {
                width += f;
            }
        }
        return width;
    }

    /**
     * 获取字符串中每一个字符在屏幕上显示的宽度
     *
     * @param paint 画笔
     * @param str   目标字符串
     * @return 每一个字符的宽度数组，如 str 为空，则返回 null
     */
    public static float[] getCharacterWidth(Paint paint, String str) {
        float[] widths = null;
        if (!TextUtils.isEmpty(str) && paint != null) {
            widths = new float[str.length()];
            paint.getTextWidths(str, widths);
        }
        return widths;
    }

    private StringUtil() {
    }
}
