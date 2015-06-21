package com.globalLibrary.util.cache;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.*;
import com.globalLibrary.util.IOUtil;
import com.globalLibrary.util.LogUtil;
import com.globalLibrary.util.MD5Util;
import com.globalLibrary.util.PhoneUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class BitmapCache {
    public static final int BITMAP_FROM_DISK = 1;

    public static final int BITMAP_FROM_MEMORY = 0;
    public static final int BITMAP_FROM_OTHER = 2;
    private static final int DISK_CACHE_INDEX = 0;
    public boolean isDebug = false;
    private final CacheParams mCacheParams;
    private final Object mDiskCacheLock = new Object();
    private LruDiskCache mLruDiskCache;
    private LruMemoryCache<String, BitmapReference> mLruMemoryCache;
    // 磁盘缓存是否正在初始化中
    private boolean mDiskCacheStarting = true;

    public BitmapCache(CacheParams mCacheParams) {
        this.mCacheParams = mCacheParams;
        init(mCacheParams);
    }

    private void init(CacheParams mCacheParams) {
        if (mCacheParams.memoryCacheEnabled) {
            mLruMemoryCache = new LruMemoryCache<String, BitmapReference>(mCacheParams.memoryCacheSize) {
                @Override
                protected int sizeOf(String key, BitmapReference value) {
                    if (value == null) {
                        return 0;
                    }
                    Bitmap bitmap = value.get();
                    if (bitmap == null || bitmap.isRecycled()) {
                        mLruMemoryCache.remove(key);
                    }
                    return value.size;
                }

                @Override
                protected void entryRemoved(boolean evicted, String key, BitmapReference oldValue, BitmapReference newValue) {
                    bitmapRemoved(key, oldValue, newValue);
                }
            };
        }

        if (mCacheParams.initDiskCacheOnCreate && mCacheParams.diskCacheEnabled) {
            initDiskCache();
        }
    }

    protected void bitmapRemoved(String key, BitmapReference oldValue, BitmapReference newValue) {

    }

    /**
     * 初始化磁盘缓存
     */
    public void initDiskCache() {
        synchronized (mDiskCacheLock) {
            mDiskCacheStarting = true;
            if (mLruDiskCache == null || mLruDiskCache.isClosed()) {
                if (mCacheParams.diskCacheEnabled && mCacheParams.diskCacheDir != null) {
                    if (!mCacheParams.diskCacheDir.exists()) {
                        mCacheParams.diskCacheDir.mkdirs();
                    }
                    if (PhoneUtil.getUsableSpace(mCacheParams.diskCacheDir) >= mCacheParams.diskCacheSize) {
                        try {
                            mLruDiskCache = LruDiskCache.open(mCacheParams.diskCacheDir, 1, 1, mCacheParams.diskCacheSize);
                        } catch (IOException e) {
                            mLruDiskCache = null;
                            mCacheParams.diskCacheDir = null;
                            LogUtil.d(e);
                        }
                    }
                }
            }
            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    @Override
    public String toString() {
        return mLruMemoryCache == null ? "" : mLruMemoryCache.toString();
    }

    /**
     * 从磁盘缓存中获取图片
     *
     * @param data 唯一标识
     * @return 缓存图片，未缓存则返回 null
     */
    public Bitmap getBitmapFromDiskCache(String data) {
        Bitmap bitmap = null;
        String key = MD5Util.getMD5String(data);
        if (mCacheParams.diskCacheEnabled) {
            synchronized (mDiskCacheLock) {
                if (mDiskCacheStarting) {
                    try {
                        mDiskCacheLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (mLruDiskCache != null) {
                InputStream in = null;
                try {
                    LruDiskCache.Snapshot snapshot = mLruDiskCache.get(key);
                    if (snapshot != null) {
                        in = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (in != null) {
                            bitmap = BitmapFactory.decodeStream(in);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    IOUtil.close(in);
                }
            }
        }
        return bitmap;
    }

    /**
     * 从内存缓存中获取图片
     *
     * @param data 图片唯一标识
     * @return 缓存图片，未缓存则返回 null
     */
    public Bitmap getBitmapFromMemoryCache(String data) {
        Bitmap bitmap = null;
        if (mLruMemoryCache != null) {
            String key = MD5Util.getMD5String(data);
            SoftReference<Bitmap> softReference = mLruMemoryCache.get(key);
            if (softReference != null) {
                bitmap = softReference.get();
                if (bitmap == null || bitmap.isRecycled()) {
                    bitmap = null;
                    mLruMemoryCache.remove(key);
                }
            }
        }
        return bitmap;
    }

    /**
     * 添加 bitmap 对象到缓存中
     *
     * @param data   bitmap 对象的唯一标识
     * @param bitmap 待缓存对象
     */
    public void addBitmapToCache(String data, Bitmap bitmap) {
        addBitmapToMemoryCache(data, bitmap);
        addBitmapToDiskCache(data, bitmap);
    }

    public void addBitmapToMemoryCache(String data, Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }
        String key = MD5Util.getMD5String(data);
        if (mLruMemoryCache != null && mLruMemoryCache.get(key) == null) {
            if (isDebug) {
                mLruMemoryCache.put(key, new BitmapReference(debugBitmap(bitmap, BITMAP_FROM_MEMORY)));
            } else {
                mLruMemoryCache.put(key, new BitmapReference(bitmap));
            }
        }
    }

    public static Bitmap debugBitmap(Bitmap bitmap, int type) {
        if (bitmap != null) {
            Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

            Paint paint = new Paint();
            paint.setAntiAlias(true);

            Canvas canvas = new Canvas(result);
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawBitmap(bitmap, 0, 0, paint);

            switch (type) {
                case BITMAP_FROM_MEMORY:
                    paint.setColor(Color.GREEN);
                    break;
                case BITMAP_FROM_DISK:
                    paint.setColor(Color.YELLOW);
                    break;
                case BITMAP_FROM_OTHER:
                    paint.setColor(Color.RED);
                    break;
            }
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(bitmap.getWidth() - 10, bitmap.getHeight() - 10, bitmap.getWidth(), bitmap.getHeight(), paint);

            return result;
        } else {
            return null;
        }
    }

    public void addBitmapToDiskCache(String data, Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }
        synchronized (mDiskCacheLock) {
            if (mLruDiskCache != null && mLruDiskCache.getDirectory() != null) {
                if (!mLruDiskCache.getDirectory().exists()) {
                    mLruDiskCache.getDirectory().mkdirs();
                }
                String key = MD5Util.getMD5String(data);
                OutputStream out = null;
                try {
                    LruDiskCache.Snapshot snapshot = mLruDiskCache.get(key);
                    if (snapshot == null) {
                        LruDiskCache.Editor editor = mLruDiskCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            Bitmap tmp = isDebug ? debugBitmap(bitmap, BITMAP_FROM_DISK) : bitmap;
                            tmp.compress(mCacheParams.compressFormat, mCacheParams.compressQuality, out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    IOUtil.close(out);
                }
            }
        }
    }

    public void clearCache() {
        clearMemoryCache();
        clearDiskCache();
    }

    public void clearMemoryCache() {
        if (mLruMemoryCache != null) {
            mLruMemoryCache.evictAll();
        }
    }

    public void clearDiskCache() {
        if (mCacheParams.diskCacheEnabled) {
            synchronized (mDiskCacheLock) {
                if (mLruDiskCache != null && !mLruDiskCache.isClosed()) {
                    try {
                        mLruDiskCache.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mLruDiskCache = null;
                    initDiskCache();
                }
            }
        }
    }

    public void clearDiskCache(String data) {
        if (mCacheParams.diskCacheEnabled) {
            synchronized (mDiskCacheLock) {
                if (mLruDiskCache != null && !mLruDiskCache.isClosed()) {
                    try {
                        mLruDiskCache.remove(MD5Util.getMD5String(data));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void clearMemoryCache(String data) {
        if (mLruMemoryCache != null) {
            mLruMemoryCache.remove(data);
        }
    }

    public void close() {
        synchronized (mDiskCacheLock) {
            if (mLruDiskCache != null && !mLruDiskCache.isClosed()) {
                try {
                    mLruDiskCache.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mLruDiskCache = null;
            }
        }
    }

    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mLruDiskCache != null) {
                try {
                    mLruDiskCache.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class BitmapReference extends SoftReference<Bitmap> {
        public final int size;

        public BitmapReference(Bitmap r) {
            super(r);
            if (r == null || r.isRecycled()) {
                size = 0;
            } else {
                size = r.getRowBytes() * r.getHeight();
            }
        }
    }

    public static class CacheParams {
        // 磁盘缓存时图片格式
        public Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
        // 磁盘缓存路径
        public File diskCacheDir;
        // 是否启用磁盘缓存
        public boolean diskCacheEnabled = true;
        public boolean initDiskCacheOnCreate = false;
        // 是否启用内存缓存
        public boolean memoryCacheEnabled = true;
        // 缓存图片清晰度
        public int compressQuality = 70;
        // 磁盘缓存大小：20M
        public int diskCacheSize = 1024 * 1024 * 20;
        // 内存缓存大小：8M
        public int memoryCacheSize = 1024 * 1024 * 8;

        public CacheParams(File diskCacheDir) {
            this.diskCacheDir = diskCacheDir;
            if (!this.diskCacheDir.exists()) {
                this.diskCacheDir.mkdirs();
            }
        }

        public CacheParams(String diskCacheDir) {
            this(new File(diskCacheDir));
        }

        /**
         * 设置内存缓存大小
         *
         * @param context 上下文
         * @param percent 缓存大小占应用程序内存的比例
         */
        @SuppressWarnings("UnusedDeclaration")
        public void setMemoryCacheSize(Context context, float percent) {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            memoryCacheSize = Math.round(manager.getMemoryClass() * percent * 1024 * 1024);
        }
    }
}
