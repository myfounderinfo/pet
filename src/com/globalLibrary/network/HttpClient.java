package com.globalLibrary.network;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.text.TextUtils;
import android.webkit.URLUtil;

import com.globalLibrary.util.LogUtil;
import com.globalLibrary.util.StringUtil;

//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.ByteArrayBody;

/**
 * 使用 HttpClient 类联网
 */
public class HttpClient implements INetwork {
    private HttpGet getMethod;
    private HttpPost postMethod;
    private HttpResponse response;
    private List<Header> reqHeaderList = new ArrayList<Header>();
    private org.apache.http.client.HttpClient httpClient;

    public HttpClient() {
        httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), 30 * 1000);
    }

    @Override
    public InputStream getContent() {
        InputStream content = null;
        if (response != null) {
            try {
                content = response.getEntity().getContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    @Override
    public int getStatusCode() {
        int statusCode = 200;
        if (response != null) {
            statusCode = response.getStatusLine().getStatusCode();
        }
        return statusCode;
    }

    @Override
    public List<Header> getAllResHeader() {
        List<Header> list = new ArrayList<Header>();
        org.apache.http.Header[] allHeaders = getMethod != null ? getMethod.getAllHeaders() : postMethod.getAllHeaders();
        for (org.apache.http.Header item : allHeaders) {
            list.add(new Header(item.getName(), item.getValue()));
        }
        return list;
    }

    @Override
    public long getContentLength() {
        long length = 0;
        if (response != null) {
            length = response.getEntity().getContentLength();
        }
        return length;
    }

    @Override
    public void abort() {
        if (getMethod != null) {
            getMethod.abort();
        }
        if (postMethod != null) {
            postMethod.abort();
        }
    }

    @Override
    public void addReqHeader(String name, String value) {
        reqHeaderList.add(new Header(name, value));
    }

    @Override
    public void connectWithGetMethod(String url) {
        if (!TextUtils.isEmpty(url) && URLUtil.isNetworkUrl(url)) {
            try {
                getMethod = new HttpGet(url);
                if (reqHeaderList != null && !reqHeaderList.isEmpty()) {
                    for (Header header : reqHeaderList) {
                        getMethod.addHeader(header.name, header.value);
                    }
                }
                response = httpClient.execute(getMethod);
                int statusCode = getStatusCode();
                if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                    org.apache.http.Header header = getMethod.getFirstHeader("location");
                    if (header != null && !StringUtil.isEmpty(header.getValue())) {
                        connectWithGetMethod(header.getValue());
                    }
                }
            } catch (IOException e) {
                LogUtil.w(e.getClass().getName() + " - " + e.getMessage());
            }
        }
    }

    @Override
    public void connectWithPostMethod(String url, byte[] postData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), timeout);
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), readTimeout);
    }
}
