package com.globalLibrary.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;

/**
 * 图形相关操作
 */
public final class ImageUtil {
    /**
     * 创建指定大小的纯色图片
     *
     * @param color  图片颜色
     * @param width  图片宽度
     * @param height 图片高度
     * @return 纯色图片
     */
    public static Bitmap createColorBitmap(int color, int width, int height) {
        Bitmap result = Bitmap.createBitmap(Math.max(width, 1), Math.max(height, 1), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(color);
        return result;
    }

    /**
     * 创建指定大小的矩形图片
     *
     * @param srcBitmap 源图片
     * @param centreX   中心点 X 座标
     * @param centreY   中心点 Y 座标
     * @param width     宽度
     * @param height    高度
     * @return 剪切后的矩形图片
     */
    public static Bitmap createRectangleBitmap(Bitmap srcBitmap, float centreX, float centreY, int width, int height) {
        Bitmap result = null;
        if (srcBitmap != null) {
            result = Bitmap.createBitmap(width, height, srcBitmap.getConfig());
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setAntiAlias(true);

            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawRect(0, 0, width, height, paint);

            Rect rect = new Rect();
            rect.left = (int) (centreX - width / 2);
            rect.top = (int) (centreY - height / 2);
            rect.bottom = rect.top + height;
            rect.right = rect.left + width;

            RectF rectF = new RectF();
            rectF.left = 0;
            rectF.top = 0;
            rectF.bottom = height;
            rectF.right = width;
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(srcBitmap, rect, rectF, paint);
        }
        return result;
    }

    /**
     * 创建圆形图片<br/>
     * 圆心在图片的正中央，半径为图片最短边长的一半
     *
     * @param srcBitmat 源图片
     * @return 圆形图片
     */
    public static Bitmap createRoundBitmap(Bitmap srcBitmat) {
        return srcBitmat == null ? null : createRoundBitmap(srcBitmat, Math.min(srcBitmat.getWidth(), srcBitmat.getHeight()) / 2);
    }

    /**
     * 创建圆形图片，圆心在源图片的正中央
     *
     * @param srcBitmap 源图片
     * @param radius    半径
     * @return 圆形图片
     */
    public static Bitmap createRoundBitmap(Bitmap srcBitmap, float radius) {
        if (srcBitmap != null) {
            return createRoundBitmap(srcBitmap, srcBitmap.getWidth() / 2, srcBitmap.getHeight() / 2, radius);
        } else {
            return null;
        }
    }

    /**
     * 创建圆形图片
     *
     * @param srcBitmap 源图片
     * @param centreX   圆心 X 轴
     * @param centreY   圆心 Y 轴
     * @param radius    半径
     * @return 圆形图片
     */
    public static Bitmap createRoundBitmap(Bitmap srcBitmap, float centreX, float centreY, float radius) {
        Bitmap result = null;
        if (srcBitmap != null) {
            float diameter = radius * 2;
            Bitmap.Config config = srcBitmap.getConfig();
            if (config == Bitmap.Config.RGB_565 || config == Bitmap.Config.ALPHA_8) {
                config = Bitmap.Config.ARGB_4444;
            }
            result = Bitmap.createBitmap((int) diameter, (int) diameter, config);
            Canvas canvas = new Canvas(result);

            Rect rect = new Rect();
            rect.left = (int) (centreX - radius);
            rect.top = (int) (centreY - radius);
            rect.right = (int) (centreX + radius);
            rect.bottom = (int) (centreY + radius);

            RectF rectF = new RectF();
            rectF.left = 0;
            rectF.top = 0;
            rectF.right = diameter;
            rectF.bottom = diameter;

            canvas.drawARGB(0, 0, 0, 0);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            canvas.drawRoundRect(rectF, radius, radius, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(srcBitmap, rect, rectF, paint);
        }
        return result;
    }

    /**
     * 旋转图片
     *
     * @param srcBitmap 原始图片
     * @param degrees   旋转角度，0 ~ 360
     * @return 旋转后的图片
     */
    public static Bitmap rotate(Bitmap srcBitmap, int degrees) {
        if (srcBitmap == null || degrees == 0) {
            return srcBitmap;
        }
        return rotate(srcBitmap, degrees, srcBitmap.getWidth() / 2F, srcBitmap.getHeight() / 2F);
    }

    /**
     * 旋转图片
     *
     * @param srcBitmap 原始图片
     * @param degrees   旋转角度，0 ~ 360
     * @param x         旋转点的 x 座标
     * @param y         旋转点的 y 轴座标
     * @return 旋转后的图片
     */
    public static Bitmap rotate(Bitmap srcBitmap, int degrees, float x, float y) {
        if (srcBitmap == null || degrees == 0) {
            return srcBitmap;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, x, y);
        return useMatrix(srcBitmap, matrix, srcBitmap.getWidth(), srcBitmap.getHeight());
    }

    private static Bitmap useMatrix(Bitmap srcBitmap, Matrix matrix, int targetWidth, int targetHeight) {
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, srcBitmap.getConfig());
        Canvas canvas = new Canvas(targetBitmap);
        canvas.drawBitmap(srcBitmap, matrix, new Paint(Paint.ANTI_ALIAS_FLAG));
        return targetBitmap;
    }

    /**
     * 缩放图片
     *
     * @param srcBitmap 原图片
     * @param scale     缩放倍率
     * @return 缩放后的图片
     */
    public static Bitmap scaleBitmap(Bitmap srcBitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        return useMatrix(srcBitmap, matrix, (int) (srcBitmap.getWidth() * scale), (int) (srcBitmap.getHeight() * scale));
    }

    /**
     * 缩放图片
     *
     * @param srcBitmap 源图片
     * @param newWidth  缩放后的图片宽度，负数则忽略
     * @param newHeight 缩放后的图片高度，负数则忽略
     * @return 缩放后的图片
     */
    public static Bitmap scaleBitmap(Bitmap srcBitmap, float newWidth, float newHeight) {
        Bitmap result = null;
        if (srcBitmap != null) {
            if (newHeight <= 0 && newWidth <= 0) {
                return srcBitmap.copy(srcBitmap.getConfig(), true);
            }
            int srcW = srcBitmap.getWidth();
            int srcH = srcBitmap.getHeight();
            float scaleW = newWidth > 0 && srcW > 0 ? newWidth / srcW : -1;
            float scaleH = newHeight > 0 && srcH > 0 ? newHeight / srcH : -1;

            if (scaleH == -1 && scaleW == -1) {
                return srcBitmap.copy(srcBitmap.getConfig(), true);
            }

            float scale;
            if (scaleW == -1) {
                scale = scaleH;
            } else if (scaleH == -1) {
                scale = scaleW;
            } else {
                scale = Math.min(scaleW, scaleH);
            }

            result = scaleBitmap(srcBitmap, scale);
        }
        return result;
    }

    /**
     * 灰度图
     *
     * @param srcBitamp 原图片
     * @return 灰度图
     */
    public static Bitmap toGray(Bitmap srcBitamp) {
        if (srcBitamp == null) {
            return null;
        }
        Bitmap target = srcBitamp.copy(srcBitamp.getConfig(), true);
        int a, r, g, b, color, m_color;
        int width = target.getWidth();
        int height = target.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                color = target.getPixel(x, y);
                a = Color.alpha(color);
                r = Color.red(color);
                g = Color.green(color);
                b = Color.blue(color);
                m_color = (int) (r * 0.3 + g * 0.59 + b * 0.11);
                m_color = Color.argb(a, m_color, m_color, m_color);
                target.setPixel(x, y, m_color);
            }
        }
        return target;
    }

    public static boolean saveBitmap(Bitmap bm, String Filepath, String Filename) {
        boolean flag = false;

        File f = new File(Filepath, Filename);

        if (f.exists()) {
            return true;
        }
        if (bm == null) {
            return false;
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 50, out);
            out.flush();
            out.close();
            flag = true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return flag;
    }

    private ImageUtil() {
    }
    
	public static String getImageListStr(List<String> listPath) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < listPath.size(); i++) {
			if (i == listPath.size() - 1) {
				builder.append(listPath.get(i));
			} else {
				builder.append(listPath.get(i) + ",");
			}
		}
		return builder.toString();
	}
	
	
}
