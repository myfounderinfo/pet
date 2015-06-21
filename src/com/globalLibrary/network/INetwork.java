package com.globalLibrary.network;

import java.io.InputStream;
import java.util.List;

/**
 * 网络连接接口
 */
public interface INetwork {
    /**
     * WAP 网关地址
     */
    public static final String CTWAP_GATEWAY_IP = "10.0.0.200";
    /**
     * WAP 网关端口号
     */
    public static final int CTWAP_GATEWAY_PORT = 80;

    /**
     * 获取响应内容
     *
     * @return 响应内容
     */
    InputStream getContent();

    /**
     * 获取所有的响应头信息
     *
     * @return 响应头信息
     */
    List<Header> getAllResHeader();

    /**
     * 获取响应状态码
     *
     * @return 响应状态码
     */
    int getStatusCode();

    /**
     * 获取返回内容长度
     *
     * @return 长度
     */
    long getContentLength();

    /**
     * 关闭连接
     */
    void abort();

    /**
     * 设置请求头信息
     *
     * @param name  名称
     * @param value 对应的值
     */
    void addReqHeader(String name, String value);

    /**
     * 以 Get 方式联网
     *
     * @param url 网络地址
     */
    void connectWithGetMethod(String url);

    /**
     * 以 POST 方式联网
     *
     * @param url      网络地址
     * @param postData 通过 post 方式传递的数据
     */
    void connectWithPostMethod(String url, byte[] postData);

    /**
     * 设置联网超时时间
     *
     * @param timeout 超时时间，单位：毫秒
     */
    void setConnectionTimeout(int timeout);

    /**
     * 设置数据读取超时
     *
     * @param readTimeout 超时时长，单位：毫秒
     */
    void setReadTimeout(int readTimeout);
}
