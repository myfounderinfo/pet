package com.globalLibrary.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import com.globalLibrary.network.ConnectFactory;
import com.globalLibrary.network.INetwork;
import com.globalLibrary.util.cache.BitmapCache;
import com.globalLibrary.util.cache.LruMemoryCache;

import java.io.*;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 获取图片
 */
@SuppressLint("NewApi")
@SuppressWarnings("UnusedDeclaration")
public class BitmapLoader {
    public static final int CACHE_MEMORY_DISK = 0;
    public static final int CACHE_NULL = 2;
    public static final int CACHE_ONLY_MEMORY = 1;
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(1);
    private static final Handler TASK_CACHE_REMOVE_HANDLER = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            String cacheKey = msg.obj.toString();
            if (TASK_CACHE.get(cacheKey) != null) {
                TASK_CACHE.remove(cacheKey);
            }
        }
    };
    private static final LruMemoryCache<String, WeakReference<FutureTask<Bitmap>>> TASK_CACHE = new LruMemoryCache<String, WeakReference<FutureTask<Bitmap>>>(10);
    private static final Object PAUSE_LOCK = new Object();
    private static final String ASSET_FILE_PREFIX = "assets://";
    private static final String DISK_CACHE_DIR = Environment.getExternalStorageDirectory().getPath() + "/cache";
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int ERROR = 1;
    private static final int LISTENER_METHOD_ON_FINISH = 3;
    private static final int LISTENER_METHOD_ON_PROGRESS = 4;
    private static final int LISTENER_METHOD_ON_START = 1;
    private static final int SUCCESS = 0;
    private static BitmapCache.CacheParams mCacheParams = null;
    private static BitmapCache mCache;
    private static ThreadPoolExecutor mExecutor;
    private static boolean hasinNativeAllocField = true;
    private static boolean isDebug = false;
    private static boolean isPause = false;
    /**
     * 并发数
     */
    private static int concurrency = CORE_POOL_SIZE;
    private final BitmapConfig bitmapCfg;
    private Context mContext = null;

    private Handler listenerHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LISTENER_METHOD_ON_START:
                    listener.onStart();
                    break;
                case LISTENER_METHOD_ON_PROGRESS:
                    listener.onProgress(msg.arg1, msg.arg2);
                    break;
                case LISTENER_METHOD_ON_FINISH:
                    listener.onFinish((Bitmap) msg.obj);
                    break;
            }
        }
    };
    private Listener listener;
    private String[] cacheKeys = {"", ""};
    private String imagePath;
    private boolean defImageSize = false;

    /**
     * 图片尺寸严格等于自定大小
     * 当图片高宽比与给定尺寸不一致时，将把图片放大到最短边和给定尺寸相同，然后居中剪切图片到指定大小
     * 如果缩放后图片已和给定大小相同，则不再处理
     * 如果该设置为 false，那么缩放图片时将按宽高中缩放比较小的一个处理，以确保缩放后的图片尺寸不会大于给定值，
     * 结果可能是图片尺寸小于给定大小
     */
    private boolean isStrictSize = false;

    private int bitmapTotalSize = -1;

    private int cacheType = CACHE_MEMORY_DISK;
    private int connectionTimeout = 30000;
    private int readTimeout = 10000;

    public static BitmapLoader with(Context context) {
        BitmapLoader instance = new BitmapLoader();
        instance.mContext = context;
        if (BitmapConfig.maxHeight == 0) {
            BitmapConfig.maxHeight = PhoneUtil.getScreenHeight(context);
        }
        if (BitmapConfig.maxWidth == 0) {
            BitmapConfig.maxWidth = PhoneUtil.getScreenWidth(context);
        }
        return instance;
    }

    /**
     * 关闭缓存
     */
    public static void close() {
        new CacheTask().execute(CacheTask.MESSAGE_CLOSE);
    }

    public static void maxBitmapHeight(int maxHeight) {
        BitmapConfig.maxHeight = maxHeight;
    }

    public static void maxBitmapWidth(int maxWidth) {
        BitmapConfig.maxWidth = maxWidth;
    }

    public static void pause() {
        synchronized (PAUSE_LOCK) {
            isPause = true;
        }
    }

    public static void setCacheParams(BitmapCache.CacheParams mCacheParams) {
        BitmapLoader.mCacheParams = mCacheParams;
    }

    public static void setConcurrency(int concurrency) {
        BitmapLoader.concurrency = concurrency;
    }

    public static void setDebug(boolean debug) {
        isDebug = debug;
        if (mCache != null) {
            mCache.isDebug = isDebug;
        }
    }

    public static void start() {
        synchronized (PAUSE_LOCK) {
            isPause = false;
            PAUSE_LOCK.notifyAll();
        }
    }

    private BitmapLoader() {
        if (mCache == null) {
            synchronized (BitmapLoader.class) {
                if (mCache == null) {
                    if (mCacheParams == null) {
                        mCacheParams = new BitmapCache.CacheParams(DISK_CACHE_DIR);
                    }
                    if (mCacheParams.diskCacheDir == null) {
                        mCacheParams.diskCacheDir = new File(DISK_CACHE_DIR);
                    }
                    mCache = new BitmapCache(mCacheParams);
                    mCache.isDebug = isDebug;
                    new CacheTask().execute(CacheTask.MESSAGE_INIT_DISK_CACHE);
                }
            }
        }
        this.bitmapCfg = new BitmapConfig();
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = get(imagePath);
        if (bitmap == null) {
            bitmap = getDefaultImage();
        }
        return bitmap;
    }

    /**
     * 该方法非异步执行，如在主线程中调用，可能出现 ANR 问题
     */
    private Bitmap get(String path) {
        callListener(LISTENER_METHOD_ON_START);
        Bitmap bitmap = getBitmapFromCache(path);
        if (bitmap == null && !StringUtil.isEmpty(path)) {
            if (URLUtil.isNetworkUrl(path)) {
                bitmap = getNetworkBitmap(path);
            } else if (path.startsWith(ASSET_FILE_PREFIX)) {
                bitmap = getAssetBitmap(path.substring(ASSET_FILE_PREFIX.length()));
            } else if (TextUtils.isDigitsOnly(path)) {
                bitmap = getResourceBitmap(Integer.parseInt(path));
            } else {
                bitmap = getLocalDiskImageBitmap(path);
            }
            bitmap = handleBitmap(bitmap, false, path);
            if (isDebug) {
                bitmap = BitmapCache.debugBitmap(bitmap, BitmapCache.BITMAP_FROM_OTHER);
            }
        }
        callListener(LISTENER_METHOD_ON_FINISH, bitmap);
        return bitmap;
    }

    private Bitmap getBitmapFromCache(String path) {
        String cacheKey = cacheKey(false, path);
        Bitmap bitmap = null;
        if (mCache != null && cacheType != CACHE_NULL) {
            bitmap = mCache.getBitmapFromMemoryCache(cacheKey);
            if (bitmap != null) {
                LogUtil.d("获取到内存缓存图片：" + path);
            }
            if (bitmap == null && !onlyCacheToMemory()) {
                bitmap = mCache.getBitmapFromDiskCache(cacheKey);
                if (bitmap != null) {
                    LogUtil.d("获取到磁盘缓存图片：" + path);
                }
            }
            if (bitmap != null && !bitmap.isRecycled() && cacheType != CACHE_NULL) {
                new CacheTask().execute(onlyCacheToMemory() ? CacheTask.MESSAGE_ADD_TO_MEMORY_CACHE : CacheTask.MESSAGE_ADD_TO_CACHE, cacheKey, bitmap);
            }
        }
        if (bitmap != null && bitmap.isRecycled()) {
            bitmap = null;
            new CacheTask().execute(CacheTask.MESSAGE_CLEAR_MEMORY, cacheKey);
        }
        return bitmap;
    }

    private String cacheKey(boolean defaultImage, String path) {
        if (StringUtil.isEmpty(cacheKeys[defaultImage ? 0 : 1])) {
            if (!defaultImage && defImageSize) {
                Bitmap defBitmap = getDefaultImage();
                bitmapCfg.width = defBitmap.getWidth();
                bitmapCfg.height = defBitmap.getHeight();
            }
            StringBuilder sb = new StringBuilder();
            sb.append(path).append("_size(").append(bitmapCfg.width).append("_").append(bitmapCfg.height).append(")");
            sb.append("_maxSize(").append(BitmapConfig.maxWidth).append("_").append(BitmapConfig.maxHeight).append(")");
            sb.append("_isStrictSize").append(isStrictSize);
            if (!this.bitmapCfg.bitmapProcessors.isEmpty()) {
                sb.append("_handleBitmaps(");
            }
            boolean isFirst = true;
            LinkedList<BitmapProcessor> handleBitmaps = new LinkedList<BitmapProcessor>(this.bitmapCfg.bitmapProcessors);
            Collections.sort(handleBitmaps, new Comparator<BitmapProcessor>() {
                @Override
                public int compare(BitmapProcessor processor, BitmapProcessor processor2) {
                    return processor.toString().compareTo(processor2.toString());
                }
            });
            for (BitmapProcessor handle : handleBitmaps) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append("_");
                }
                sb.append(handle.toString());
            }
            if (!this.bitmapCfg.bitmapProcessors.isEmpty()) {
                sb.append(")");
            }
            if (isDebug) {
                sb.append("_debug");
            }
            cacheKeys[defaultImage ? 0 : 1] = sb.toString();
        }
        return cacheKeys[defaultImage ? 0 : 1];
    }

    private boolean onlyCacheToMemory() {
        return cacheType == CACHE_ONLY_MEMORY || StringUtil.isEmpty(imagePath) || TextUtils.isDigitsOnly(imagePath);
    }

    private Bitmap getNetworkBitmap(String url) {
        Bitmap bitmap = null;
        InputStream inputStream = getNetworkBitmapInputStream(url);
        if (inputStream != null) {
            bitmap = getBitmapFromInputStream(inputStream);
            IOUtil.close(inputStream);
        }
        if (bitmap != null) {
            LogUtil.d("获取到互联网图片：" + url);
        } else {
            LogUtil.d("获取互联网图片失败：" + url);
        }
        return bitmap;
    }

    private InputStream getNetworkBitmapInputStream(String url) {
        INetwork connect = ConnectFactory.getConnect(ConnectFactory.TYPE_HTTPCLIENT);
        connect.setConnectionTimeout(connectionTimeout);
        connect.setReadTimeout(readTimeout);
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            connect.connectWithGetMethod(url);
            int statusCode = connect.getStatusCode();
            if (statusCode == 200 || statusCode == 206) {
                InputStream inputStream = connect.getContent();
                bitmapTotalSize = (int) connect.getContentLength();
                byteArrayOutputStream = new ByteArrayOutputStream();
                IOUtil.copy(inputStream, byteArrayOutputStream);

                byteArrayOutputStream.flush();
                return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            }
        } catch (Exception e) {
            LogUtil.w(e.getClass().getName() + " - " + e.getMessage());
        } finally {
            IOUtil.close(byteArrayOutputStream);
        }
        return null;
    }

    private Bitmap getBitmapFromInputStream(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024 * 8];
        int size;
        int total = 0;
        try {
            while ((size = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, size);
                outputStream.flush();
                total += size;
                if (bitmapTotalSize > 0) {
                    callListener(LISTENER_METHOD_ON_PROGRESS, total, bitmapTotalSize);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] data = outputStream.toByteArray();
        IOUtil.close(outputStream);
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            int dataLength = data.length;
            BitmapFactory.Options options = createOptions(true, imagePath);
            BitmapFactory.decodeByteArray(data, 0, dataLength, options);

            setSampleSize(options, bitmapCfg.width, bitmapCfg.height);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(data, 0, dataLength, options);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }

    private BitmapFactory.Options createOptions(boolean onlyGetImageInof, String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        if (hasinNativeAllocField) {
            try {
                Reflect.on(options).set("inNativeAlloc", true);
                hasinNativeAllocField = true;
            } catch (Reflect.ReflectException e) {
                hasinNativeAllocField = false;
            }
        }
        if (TextUtils.isDigitsOnly(imagePath)) {
            int resId = Integer.parseInt(imagePath);
            TypedValue typedValue = new TypedValue();
            mContext.getResources().openRawResource(resId, typedValue);
            options.inDensity = typedValue.density;
            options.inTargetDensity = PhoneUtil.getScreenInfo(mContext).densityDpi;
        } else {
            options.inDensity = DisplayMetrics.DENSITY_DEFAULT;
            options.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
        }
        options.inJustDecodeBounds = onlyGetImageInof;
        return options;
    }

    private void setSampleSize(BitmapFactory.Options options, int width, int height) {
        int targetH = height;
        int targetW = width;
        if (targetH <= 0 && targetW <= 0) {
            targetH = BitmapConfig.maxHeight;
            targetW = BitmapConfig.maxWidth;
        }

        int bitmapW = options.outWidth;
        int bitmapH = options.outHeight;
        if (bitmapH > targetH || bitmapW > targetW) {
            if (height > 0 && width > 0) {
                options.inSampleSize = Math.min(bitmapH / height, bitmapW / width);
            } else if (height > 0) {
                options.inSampleSize = bitmapH / height;
            } else if (width > 0) {
                options.inSampleSize = bitmapW / width;
            } else {
                options.inSampleSize = 1;
            }
            options.inSampleSize = (int) Math.pow(2, Math.ceil(Math.log(options.inSampleSize) / Math.log(2)));
        } else {
            options.inSampleSize = 1;
        }
        if (options.inSampleSize == 0) {
            options.inSampleSize = 1;
        }
    }

    private Bitmap getAssetBitmap(String bitmapPath) {
        Bitmap target = null;
        InputStream inputStream = null;
        try {
            inputStream = mContext.getAssets().open(bitmapPath);
            target = getBitmapFromInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.close(inputStream);
        }
        if (target != null) {
            LogUtil.d("获取到 Asset 目录资源：" + bitmapPath);
        } else {
            LogUtil.d("获取 Asset 目录资源失败：" + bitmapPath);
        }
        return target;
    }

    private Bitmap getResourceBitmap(int resID) {
        try {
            Resources resources = mContext.getResources();
            BitmapFactory.Options options = createOptions(true, String.valueOf(resID));
            BitmapFactory.decodeResource(resources, resID, options);

            setSampleSize(options, bitmapCfg.width, bitmapCfg.height);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeResource(resources, resID, options);
            if (bitmap != null) {
                LogUtil.d("获取到图片：" + resID);
            } else {
                LogUtil.d("获取图片失败：" + resID);
            }
            return bitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        LogUtil.d("获取图片失败：" + resID);
        return null;
    }

    /**
     * 获取本地指定大小的图片
     *
     * @param bitmapFilePath 图片路径
     * @return 得到的指定大小的图片
     */
    private Bitmap getLocalDiskImageBitmap(String bitmapFilePath) {
        Bitmap target = null;
        if (!TextUtils.isEmpty(bitmapFilePath) && !URLUtil.isNetworkUrl(bitmapFilePath)) {
            FileInputStream inputStream = null;
            bitmapFilePath=bitmapFilePath.replace("file://", "");
            try {
                inputStream = new FileInputStream(bitmapFilePath);
                target = getBitmapFromInputStream(inputStream);
            } catch (FileNotFoundException e) {
                LogUtil.w(e.getClass().getName() + " -- " + e.getMessage());
            } finally {
                IOUtil.close(inputStream);
            }
        }
        if (target != null) {
            LogUtil.d("获取到图片：" + bitmapFilePath);
        } else {
            LogUtil.d("获取图片失败：" + bitmapFilePath);
        }
        return target;
    }

    private Bitmap handleBitmap(Bitmap bitmap, boolean isDefaultImage, String path) {
        Bitmap targetBitmap = bitmap;
        if (targetBitmap != null) {
            Bitmap tmpBitmap;
            if (bitmapCfg.bitmapProcessors.size() > 0) {
                // 在并发时，每次仅允许一个进行图片处理，避免多个图片同时处理造成内存溢出
                synchronized (BitmapLoader.class) {
                    for (BitmapProcessor bitmapProcessor : bitmapCfg.bitmapProcessors) {
                        tmpBitmap = bitmapProcessor.process(targetBitmap);
                        targetBitmap.recycle();
                        targetBitmap = tmpBitmap;
                        LogUtil.d("图片资源处理完毕：[" + bitmapProcessor.toString() + "] => [" + targetBitmap.getWidth() + ", " + targetBitmap.getHeight() + "] => " + path);
                    }
                }
            }
            if ((bitmapCfg.width > 0 && targetBitmap.getWidth() != bitmapCfg.width) || (bitmapCfg.height > 0 && targetBitmap.getHeight() != bitmapCfg.height)) {
                synchronized (BitmapLoader.class) {
                    tmpBitmap = targetBitmap;
                    targetBitmap = scaleBitmap(targetBitmap);
                    tmpBitmap.recycle();
                }
                LogUtil.d("图片资源大小调整完毕：[" + targetBitmap.getWidth() + ", " + targetBitmap.getHeight() + "] => " + path);
            }
            if (cacheType != CACHE_NULL) {
                new CacheTask().execute(onlyCacheToMemory() ? CacheTask.MESSAGE_ADD_TO_MEMORY_CACHE
                        : CacheTask.MESSAGE_ADD_TO_CACHE, cacheKey(isDefaultImage, path), targetBitmap);
            }
        }
        return targetBitmap;
    }

    private Bitmap scaleBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if ((bitmapCfg.width <= 0 && bitmapCfg.height <= 0) || (width == bitmapCfg.width && height == bitmapCfg.height)) {
            return bitmap.copy(bitmap.getConfig(), true);
        }
        if (!isStrictSize) {
            return ImageUtil.scaleBitmap(bitmap, bitmapCfg.width, bitmapCfg.height);
        }
        float scale = Math.max(bitmapCfg.width * 1.0F / width, bitmapCfg.height * 1.0F / height);
        int targetWidth;
        int targetHeight;
        if (bitmapCfg.width <= 0 && bitmapCfg.height > 0) {
            targetHeight = bitmapCfg.height;
            targetWidth = bitmapCfg.height * width / height;
        } else if (bitmapCfg.width > 0 && bitmapCfg.height <= 0) {
            targetWidth = bitmapCfg.width;
            targetHeight = targetWidth * height / width;
        } else {
            targetWidth = bitmapCfg.width;
            targetHeight = bitmapCfg.height;
        }
        Bitmap targetBitmap = ImageUtil.scaleBitmap(bitmap, scale);
        if (targetWidth == targetBitmap.getWidth() && targetHeight == targetBitmap.getHeight()) {
            return targetBitmap;
        }
        Bitmap tmpBitmap = targetBitmap;
        targetBitmap = ImageUtil.createRectangleBitmap(targetBitmap, targetBitmap.getWidth() / 2, targetBitmap.getHeight() / 2, targetWidth, targetHeight);
        tmpBitmap.recycle();
        return targetBitmap;
    }

    private void callListener(int method_type, Object... argv) {
        if (this.listener != null) {
            switch (method_type) {
                case LISTENER_METHOD_ON_FINISH:
                    if (argv != null && argv.length >= 1 && argv[0] instanceof Bitmap) {
                        listenerHandler.sendMessage(listenerHandler.obtainMessage(method_type, argv[0]));
                    }
                    break;
                case LISTENER_METHOD_ON_PROGRESS:
                    if (argv != null && argv.length >= 2) {
                        Message message = listenerHandler.obtainMessage(method_type, Integer.parseInt(argv[0].toString()), Integer.parseInt(argv[1].toString()));
                        listenerHandler.sendMessage(message);
                    }
                    break;
                default:
                    listenerHandler.sendEmptyMessage(method_type);
                    break;
            }
        }
    }

    private Bitmap getDefaultImage() {
        String path = String.valueOf(bitmapCfg.defBitmapResId);
        String cacheKey = cacheKey(true, path);
        Bitmap bitmap = mCache.getBitmapFromMemoryCache(cacheKey);
        if (bitmap == null && bitmapCfg.defBitmapResId > 0) {
            bitmap = getResourceBitmap(bitmapCfg.defBitmapResId);
            bitmap = handleBitmap(bitmap, true, path);
            mCache.addBitmapToMemoryCache(cacheKey, bitmap);
        }
        return bitmap;
    }

    private Bitmap getHandleBitmap(BitmapProcessor bitmapProcessor, Bitmap bitmap) {
        return bitmapProcessor == null ? bitmap : bitmapProcessor.process(bitmap);
    }

    private FutureTask<Bitmap> getFutureTaskFromCache(String key) {
        WeakReference<FutureTask<Bitmap>> weakReference = TASK_CACHE.get(key);
        if (weakReference != null && weakReference.get() != null) {
            return weakReference.get();
        }
        return null;
    }

    public BitmapLoader addHandleBitmap(BitmapProcessor bitmapProcessor) {
        if (bitmapProcessor != null) {
            this.bitmapCfg.bitmapProcessors.add(bitmapProcessor);
        }
        return this;
    }

    public BitmapLoader addHandleBitmap(Collection<BitmapProcessor> bitmapProcessors) {
        if (bitmapProcessors != null) {
            for (BitmapProcessor bitmapProcessor : bitmapProcessors) {
                this.bitmapCfg.bitmapProcessors.add(bitmapProcessor);
            }
        }
        return this;
    }

    public BitmapLoader cacheType(final int cacheType) {
        this.cacheType = cacheType;
        return this;
    }

    public BitmapLoader connectionTimeout(final int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public BitmapLoader defaultBitmap(int resId) {
        this.bitmapCfg.defBitmapResId = resId;
        if (bitmapCfg.width <= 0 && bitmapCfg.height <= 0) {
            size(BitmapLoader.with(mContext).imageResId(bitmapCfg.defBitmapResId).addHandleBitmap(bitmapCfg.bitmapProcessors).getBitmapSize());
        }
        return this;
    }

    public BitmapLoader size(int[] size) {
        bitmapCfg.width = size[0];
        bitmapCfg.height = size[1];
        return this;
    }

    /**
     * 获取图片尺寸
     *
     * @return {width, height}
     */
    public int[] getBitmapSize() {
        int[] size = {0, 0};
        Bitmap bitmap = get(imagePath);
        if (bitmap != null) {
            size[0] = bitmap.getWidth();
            size[1] = bitmap.getHeight();
        }
        return size;
    }

    public BitmapLoader equalsDefaultImageSize(boolean defImageSize) {
        this.defImageSize = defImageSize;
        return this;
    }

    public BitmapLoader imageAssets(String assetsPath) {
        this.imagePath = ASSET_FILE_PREFIX + assetsPath;
        return this;
    }

    public BitmapLoader imagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }

    public BitmapLoader imageResId(int resId) {
        this.imagePath = String.valueOf(resId);
        return this;
    }

    public BitmapLoader listener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public BitmapLoader readTimeout(final int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public BitmapLoader size(int width, int height) {
        bitmapCfg.width = width;
        bitmapCfg.height = height;
        return this;
    }

    public BitmapLoader strictSize(final boolean isStrictSize) {
        this.isStrictSize = isStrictSize;
        return this;
    }

    public UserTask useBackground(View view) {
        return use(view, true);
    }

    private UserTask use(View view, boolean isBackground) {
        if (StringUtil.isEmpty(imagePath)) {
            Bitmap defaultImage = getDefaultImage();
            if (defaultImage != null && !defaultImage.isRecycled()) {
                setBitmap(view, defaultImage, isBackground);
            }
            return null;
        }
        Drawable curDrawable = getDrawable(view, isBackground);
        if (curDrawable instanceof AsyncDrawable) {
            AsyncDrawable asyncDrawable = (AsyncDrawable) curDrawable;
            BitmapTask task = asyncDrawable.getTask();
            if (task != null) {
                if (imagePath != null && imagePath.equals(task.getUrl())) {
                    return task;
                } else {
                    task.cancel(true);
                }
            }
        } else if (curDrawable instanceof SmartDrawable) {
            SmartDrawable drawable = (SmartDrawable) curDrawable;
            if (imagePath.equals(drawable.getUrl()) && !drawable.isRecycled()) {
                return null;
            }
        }
        BitmapTask task = null;
        String cacheKey = cacheKey(false, imagePath);
        Bitmap bitmapMemoryCache = mCache != null && cacheType != CACHE_NULL ? mCache.getBitmapFromMemoryCache(cacheKey) : null;
        if (bitmapMemoryCache != null) {
            LogUtil.d("获取到内存缓存图片：" + imagePath);
            setBitmap(view, bitmapMemoryCache, isBackground);
            new CacheTask().execute(CacheTask.MESSAGE_ADD_TO_MEMORY_CACHE, cacheKey, bitmapMemoryCache);
        } else {
            task = new BitmapTask(new BitmapUseHandler(view, imagePath, isBackground));
            task.execute(getThreadPoolExecutor(), imagePath);
            setDrawable(view, new AsyncDrawable(task, view.getResources(), imagePath, getDefaultImage()), isBackground);
        }
        return task;
    }

    private Drawable getDrawable(View view, boolean isBackground) {
        Drawable drawable = null;
        if (isBackground) {
            drawable = view.getBackground();
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            drawable = imageView.getDrawable();
        }
        return drawable;
    }

    private void setBitmap(View view, Bitmap bitmap, boolean isBackground) {
        setDrawable(view, new SmartDrawable(mContext.getResources(), imagePath, bitmap), isBackground);
    }

    private ThreadPoolExecutor getThreadPoolExecutor() {
        if (mExecutor == null || mExecutor.isShutdown()) {
            synchronized (BitmapLoader.class) {
                if (mExecutor == null || mExecutor.isShutdown()) {
                    mExecutor = new ThreadPoolExecutor(concurrency, MAXIMUM_POOL_SIZE, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable runnable) {
                            Thread thread = new Thread(runnable, "BitmapTask #" + THREAD_NUMBER.getAndIncrement());
                            thread.setPriority(Thread.NORM_PRIORITY - 1);
                            return thread;
                        }
                    });
                    if (PhoneUtil.getPhoneSDK() >= 9) {
                        mExecutor.allowCoreThreadTimeOut(true);
                    }
                }
            }
        }
        return mExecutor;
    }

   // @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    private void setDrawable(View view, SmartDrawable drawable, boolean isBackground) {
//        drawable.setBounds(0, 0, drawable.getBitmap().getWidth(), drawable.getBitmap().getHeight());
        if (isBackground) {
            if (PhoneUtil.getPhoneSDK() >= 16) {
               // view.setBackground(drawable);
            } else {
                view.setBackgroundDrawable(drawable);
            }
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            imageView.setImageDrawable(drawable);
        }
    }

    public UserTask useImage(ImageView imgView) {
        return use(imgView, false);
    }

    private static class BitmapConfig {
        public static int maxHeight;
        public static int maxWidth;
        public final LinkedList<BitmapProcessor> bitmapProcessors = new LinkedList<BitmapProcessor>();
        public int defBitmapResId;
        public int height = -1;
        public int width = -1;
    }

    private static class CacheTask extends UserTask<Object, Void, Void> {
        public static final int MESSAGE_ADD_TO_CACHE = 9;
        public static final int MESSAGE_ADD_TO_MEMORY_CACHE = 10;
        public static final int MESSAGE_CLEAR = 0;
        public static final int MESSAGE_CLEAR_DISK = 5;
        public static final int MESSAGE_CLEAR_KEY = 6;
        public static final int MESSAGE_CLEAR_KEY_IN_DISK = 8;
        public static final int MESSAGE_CLEAR_KEY_IN_MEMORY = 7;
        public static final int MESSAGE_CLEAR_MEMORY = 4;
        public static final int MESSAGE_CLOSE = 3;
        public static final int MESSAGE_FLUSH = 2;
        public static final int MESSAGE_INIT_DISK_CACHE = 1;

        @Override
        protected Void doInBackground(Object... mParams) {
            String key;
            if (mCache != null) {
                int what = Integer.parseInt(mParams[0].toString());
                switch (what) {
                    case MESSAGE_CLEAR:
                        clearMemoryCache();
                        mCache.clearCache();
                        break;
                    case MESSAGE_INIT_DISK_CACHE:
                        mCache.initDiskCache();
                        break;
                    case MESSAGE_FLUSH:
                        mCache.flush();
                        break;
                    case MESSAGE_CLOSE:
                        clearMemoryCache();
                        mCache.close();
                        break;
                    case MESSAGE_CLEAR_MEMORY:
                        clearMemoryCache();
                        break;
                    case MESSAGE_CLEAR_DISK:
                        mCache.clearDiskCache();
                        break;
                    case MESSAGE_CLEAR_KEY:
                        key = mParams[1].toString();
                        TASK_CACHE.remove(key);
                        mCache.clearMemoryCache(key);
                        mCache.clearDiskCache(key);
                        break;
                    case MESSAGE_CLEAR_KEY_IN_MEMORY:
                        key = mParams[1].toString();
                        TASK_CACHE.remove(key);
                        mCache.clearMemoryCache(key);
                        break;
                    case MESSAGE_CLEAR_KEY_IN_DISK:
                        mCache.clearDiskCache(mParams[1].toString());
                        break;
                    case MESSAGE_ADD_TO_CACHE:
                        key = mParams[1].toString();
                        Bitmap bitmap = (Bitmap) mParams[2];
                        mCache.addBitmapToCache(key, bitmap);
                        break;
                    case MESSAGE_ADD_TO_MEMORY_CACHE:
                        key = mParams[1].toString();
                        mCache.addBitmapToMemoryCache(key, (Bitmap) mParams[2]);
                        break;
                }
            }
            return null;
        }

        private void clearMemoryCache() {
            mCache.clearMemoryCache();
            TASK_CACHE.evictAll();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        }
    }

    private class BitmapTask extends UserTask<Object, Void, Void> {
        private final Handler mHandler;
        private String url;

        public String getUrl() {
            return url;
        }

        public BitmapTask(Handler handler) {
            mHandler = handler;
        }

        @Override
        protected Void doInBackground(Object... mParams) {
            synchronized (PAUSE_LOCK) {
                if (isPause) {
                    try {
                        PAUSE_LOCK.wait();
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
            }
            url = mParams[0].toString();
            Bitmap bitmap = null;
            String cacheKey = cacheKey(false, url);
            if (!isCancelled()) {
                FutureTask<Bitmap> future = getFutureTask();
                try {
                    bitmap = future.get();
                } catch (InterruptedException e) {
                    LogUtil.d("future.get() 被中断，重新尝试获取图片:" + url);
                    TASK_CACHE_REMOVE_HANDLER.obtainMessage(1, cacheKey).sendToTarget();
                    future = getFutureTask();
                    try {
                        bitmap = future.get();
                    } catch (Exception e1) {
                        LogUtil.d(e1.getClass().getSimpleName() + "[" + e1.getMessage() + "] 获取图片失败：" + url);
                    }
                } catch (Exception e) {
                    LogUtil.d(e.getClass().getSimpleName() + "[" + e.getMessage() + "] 获取图片失败：" + url);
                }
            }
            if (mHandler != null && !isCancelled()) {
                mHandler.obtainMessage(bitmap != null ? SUCCESS : ERROR, bitmap).sendToTarget();
            }
            return null;
        }

        private FutureTask<Bitmap> getFutureTask() {
            String cacheKey = cacheKey(false, url);
            FutureTask<Bitmap> future = getFutureTaskFromCache(cacheKey);
            if (future == null) {
                FutureTask<Bitmap> futureTask = new FutureTask<Bitmap>(new Callable<Bitmap>() {
                    @Override
                    public Bitmap call() throws Exception {
                        return get(url);
                    }
                });
                future = getFutureTaskFromCache(cacheKey);
                if (future == null) {
                    future = futureTask;
                    TASK_CACHE.put(cacheKey, new WeakReference<FutureTask<Bitmap>>(future));
                    TASK_CACHE_REMOVE_HANDLER.sendMessageDelayed(TASK_CACHE_REMOVE_HANDLER.obtainMessage(1, cacheKey), 3000);
                    future.run();
                }
            }
            return future;
        }
    }

    private class BitmapUseHandler extends Handler {
        private final SoftReference<View> viewReference;
        private final String path;
        private final boolean isBackground;

        private BitmapUseHandler(View view, String path, boolean background) {
            super(Looper.getMainLooper());
            this.viewReference = new SoftReference<View>(view);
            this.path = path;
            isBackground = background;
        }

        @Override
        public void handleMessage(Message msg) {
            View view = viewReference.get();
            if (view != null /*&& view.getParent() != null*/) {
                Drawable drawable = getDrawable(view, isBackground);
                if (drawable instanceof AsyncDrawable) {
                    AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                    if (path.equals(asyncDrawable.getTask().getUrl())) {
                        Bitmap bitmap = msg.what == SUCCESS && msg.obj instanceof Bitmap ? (Bitmap) msg.obj : getDefaultImage();
                        setBitmap(view, bitmap, isBackground);
                    }
                }
            }
        }
    }

    public static class AsyncDrawable extends SmartDrawable {
        private final BitmapTask task;

        public BitmapTask getTask() {
            return task;
        }

        public AsyncDrawable(BitmapTask task, Resources res, String url, Bitmap bitmap) {
            super(res, url, bitmap);
            this.task = task;
        }
    }

    public static interface BitmapProcessor {
        // 预处理图片
        Bitmap process(Bitmap bitmap);
    }

    public static interface Listener {
        public void onFinish(Bitmap bitmap);

        public void onProgress(int size, int total);

        public void onStart();
    }

    public static class SimpleListener implements Listener {
        @Override
        public void onFinish(Bitmap bitmap) {

        }

        @Override
        public void onProgress(int size, int total) {

        }

        @Override
        public void onStart() {

        }
    }

    public static class SmartDrawable extends BitmapDrawable {
        private String url;

        public String getUrl() {
            return url;
        }

        public SmartDrawable(Resources res, String url, Bitmap bitmap) {
            super(res, bitmap);
            this.url = url;
        }

        public boolean isRecycled() {
            Bitmap bitmap = getBitmap();
            return getIntrinsicWidth() <= 0 || getIntrinsicHeight() <= 0 || bitmap == null || bitmap.isRecycled();
        }
    }
}
