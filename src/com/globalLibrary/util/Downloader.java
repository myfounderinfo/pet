package com.globalLibrary.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalLibrary.network.ConnectFactory;
import com.globalLibrary.network.Header;
import com.globalLibrary.network.INetwork;

import static com.globalLibrary.util.IOUtil.close;

@SuppressWarnings("UnusedDeclaration")
public class Downloader implements Runnable {
    public static final int STATUS_CANCEL = 4;
    public static final int STATUS_ERROR = 3;
    public static final int STATUS_START = 1;
    public static final int STATUS_SUCCESS = 2;
    public static final int STATUS_WAIT = 0;

    private static final Pattern CONTENT_RANGE = Pattern.compile("(\\d+)-(\\d+)/(\\d+)");
    private static final Pattern RANGE = Pattern.compile("bytes=(\\d+)-(\\d*)");

    /**
     * 连接超时，单位：毫秒
     */
    private static final int CONNECTION_TIMEOUT = 30000;
    /**
     * 读取数据超时，单位：毫秒
     */
    private static final int READ_TIMEOUT = 10000;
    private final Info mInfo;

    private Handler mStatusChangeHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (statusChangeListener != null && msg.arg1 != msg.arg2) {
                statusChangeListener.onStatusChange(msg.arg1, msg.arg2);
            }
        }
    };
    private IDownloadStatusChange statusChangeListener = null;
    private boolean isRun = true;

    private int connectType = ConnectFactory.TYPE_HTTPCLIENT;
    private int connectionTimeout = CONNECTION_TIMEOUT;
    private int readTimeout = READ_TIMEOUT;
    private int status = STATUS_WAIT;

    public int getStatus() {
        return status;
    }

    private void setStatus(int status) {
        mStatusChangeHandler.obtainMessage(0, this.status, status).sendToTarget();
        this.status = status;
    }

    public static Downloader from(String url) {
        Downloader downloader = new Downloader();
        downloader.mInfo.url = url;
        return downloader;
    }

    private Downloader() {
        mInfo = new Info();
    }

    @Override
    public void run() {
        setStatus(STATUS_START);
        // 联网获取数据
        INetwork connect = ConnectFactory.getConnect(connectType);
        connect.setConnectionTimeout(connectionTimeout);
        connect.setReadTimeout(readTimeout);
        connect.addReqHeader("Range", "bytes=" + mInfo.startPoint + "-");
        connect.connectWithGetMethod(mInfo.url);

        int statusCode = connect.getStatusCode();
        // 验证服务器是否支持断点续传，并获取服务器返回的下载起始点和文件总大小
        long size = mInfo.startPoint;
        long[] contentRange = null;
        long range = -1;
        List<Header> resHeaderList = connect.getAllResHeader();
        if (resHeaderList != null) {
            for (Header header : resHeaderList) {
                if ("Content-Range".equalsIgnoreCase(header.name) && !StringUtil.isEmpty(header.value)) {
                    Matcher matcher = CONTENT_RANGE.matcher(header.value);
                    if (matcher.find()) {
                        contentRange = new long[2];
                        contentRange[0] = Long.parseLong(matcher.group(1));
                        contentRange[1] = Long.parseLong(matcher.group(3));
                    }
                    break;
                } else if ("Range".equalsIgnoreCase(header.name) && !StringUtil.isEmpty(header.value)) {
                    Matcher matcher = RANGE.matcher(header.value);
                    if (matcher.find()) {
                        range = Long.parseLong(matcher.group(1));
                    }
                }
            }
        }

        if (contentRange != null) {
            size = contentRange[0];
            mInfo.contentSize = contentRange[1];
        } else if (range != -1) {
            size = range;
        }

        if (statusCode == 200 || statusCode == 206) {
            InputStream inputStream = connect.getContent();
            if (mInfo.contentSize <= 0) {
                mInfo.contentSize = connect.getContentLength();
            }
            if (size < mInfo.startPoint) {
                mInfo.startPoint = size + IOUtil.skip(inputStream, mInfo.startPoint - size);
            }
            RandomAccessFile saveFile = null;
            FileChannel fileChannel = null;
            try {
                saveFile = new RandomAccessFile(mInfo.savePath, "rw");
                fileChannel = saveFile.getChannel();
            } catch (IOException e) {
                setStatus(STATUS_ERROR);
                e.printStackTrace();
            }
            if (fileChannel == null) {
                connect.abort();
                return;
            }
            try {
                ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
                while (isRun && mInfo.downloadSize < mInfo.contentSize) {
                    mInfo.downloadSize += fileChannel.transferFrom(readableByteChannel, mInfo.downloadSize + mInfo.startPoint, mInfo.contentSize - mInfo.downloadSize);
                }
                fileChannel.force(false);
            } catch (IOException e) {
                setStatus(STATUS_ERROR);
                e.printStackTrace();
            }
            close(fileChannel);
            close(saveFile);
            connect.abort();
            if (status != STATUS_ERROR) {
                if (mInfo.contentSize > 0 && mInfo.downloadSize >= mInfo.contentSize) {
                    setStatus(STATUS_SUCCESS);
                } else {
                    setStatus(isRun ? STATUS_ERROR : STATUS_CANCEL);
                }
            }
        } else {
            setStatus(STATUS_ERROR);
        }
    }

    private boolean isRun() {
        return !Thread.currentThread().isInterrupted() && isRun;
    }

    private void download() {

    }

    public Downloader connectType(final int connectType) {
        this.connectType = connectType;
        return this;
    }

    public Downloader connectionTimeout(final int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public Downloader readTimeout(final int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public Downloader startPoiont(long startPoint) {
        mInfo.startPoint = startPoint;
        return this;
    }

    public Downloader statusChangeListener(final IDownloadStatusChange statusChangeListener) {
        this.statusChangeListener = statusChangeListener;
        return this;
    }

    public Downloader to(String savePath) {
        mInfo.savePath = savePath;
        return this;
    }

    public String getSavePath() {
        return mInfo.savePath;
    }

    public String getUrl() {
        return mInfo.url;
    }

    public long getContentSize() {
        return mInfo.contentSize;
    }

    public long getDownloadSize() {
        return mInfo.downloadSize;
    }

    public long getStartPoint() {
        return mInfo.startPoint;
    }

    public void cancel() {
        isRun = false;
    }

    public void startOnExcutor(Executor executor) {
        executor.execute(this);
    }

    public void startOnThread() {
        new Thread(this).start();
    }

    private class Info {
        /**
         * 文件保持地址
         */
        public String savePath;
        /**
         * 需下载的文件网络地址
         */
        public String url;
        /**
         * 已下载文件大小
         */
        public long downloadSize;
        /**
         * 文件总长度
         */
        public long contentSize;
        public long startPoint;
    }

    /**
     * 下载状态变化时调用
     */
    public static interface IDownloadStatusChange {
        void onStatusChange(int oldStatus, int newStatus);
    }
}
