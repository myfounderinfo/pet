package com.globalLibrary.network;

/**
 * 获取网络连接器
 */
public final class ConnectFactory {
    public static final int TYPE_HTTPCLIENT = 1;

    /**
     * 获取可用的连接器
     *
     * @return 可用网络连接器
     */
    public static INetwork getConnect(int type) {
        INetwork iNetwork = null;
        switch (type) {
            case TYPE_HTTPCLIENT:
                iNetwork = new HttpClient();
                break;
        }
        return iNetwork;
    }

    private ConnectFactory() {
    }
}
