package com.globalLibrary.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Key;

/**
 * DES 加密、解密
 */
public class DESUtil {
    /**
     * 加密（解密）模式/工作模式/填充模式
     */
    private static final String CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";

    /**
     * 加密算法
     */
    private static final String KEY_ALGORITHM = "DES";

    /**
     * 解密
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return 解密后数据
     */
    public static byte[] decrypt(byte[] data, byte[] key) {
        try {
            // 还原密钥
            Key k = toKey(key);
            // 实例化
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            // 初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, k);
            // 解密
            return cipher.doFinal(data);
        } catch (Exception e) {
            LogUtil.d(e);
        }
        return null;
    }

    /**
     * 转换密钥
     *
     * @param key 二进制密钥
     * @return 密钥
     * @throws Exception
     */
    private static Key toKey(byte[] key) throws Exception {
        //  实例化 DES 密钥
        DESKeySpec dks = new DESKeySpec(key);
        // 实例化密钥工厂
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        // 生成密钥
        return keyFactory.generateSecret(dks);
    }

    /**
     * 加密
     *
     * @param data 待加密数据
     * @param key  密钥
     * @return 加密后数据
     */
    public static byte[] encrypt(byte[] data, byte[] key) {
        try {
            // 还原密钥
            Key k = toKey(key);
            // 实例化
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            // 初始化，设置为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, k);
            // 加密
            return cipher.doFinal(data);
        } catch (Exception e) {
            LogUtil.d(e);
        }
        return null;
    }

    private DESUtil() {
    }
}
