package com.globalLibrary.util;

import java.io.*;

public class IOUtil {
    /**
     * 将输入流中的数据全部拷贝到输出流
     *
     * @param in  输入流
     * @param out 输出流
     * @return 实际拷贝的字节数
     */
    @SuppressWarnings("UnusedDeclaration")
    public static long copy(InputStream in, OutputStream out) throws IOException {
        long total = 0;
        if (in != null && out != null) {
            byte[] data = new byte[8 * 1024];
            int readSize;
            while ((readSize = in.read(data)) != -1) {
                total += readSize;
                out.write(data, 0, readSize);
            }
        }
        return total;
    }

    /**
     * 读取输入流中的所有数据为一个字符串
     *
     * @param inputStream 输入流（不会自动关闭）
     * @return 读取到的字符串
     */
    public static String toString(InputStream inputStream) {
        if (inputStream == null) {
            return "";
        }
        return new String(toBytes(inputStream));
    }

    /**
     * 将输入流中的数据读取为 byte 数组
     *
     * @param inputStream 输入流。该输入流不会被自动关闭
     * @return 输入流中全部数据的 byte 数组
     */
    public static byte[] toBytes(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024 * 8];
        int size;
        try {
            while ((size = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, size);
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] result = outputStream.toByteArray();
        close(outputStream);
        return result;
    }

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 手动将输入流跳过指定字节数
     *
     * @param inputStream 输入流
     * @param skip        要跳过的字节数
     * @return 实际跳过的字节数
     */
    public static long skip(InputStream inputStream, long skip) {
        long totalBytesSkipped = 0L;
        try {
            long bytesSkipped;
            int readSize;
            while (totalBytesSkipped < skip) {
                bytesSkipped = inputStream.skip(skip - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    readSize = inputStream.read();
                    if (readSize < 0) {
                        break;
                    } else {
                        bytesSkipped = 1;
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return totalBytesSkipped;
    }

    private IOUtil() {
    }
}
