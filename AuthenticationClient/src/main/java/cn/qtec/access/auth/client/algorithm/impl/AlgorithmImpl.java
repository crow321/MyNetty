package cn.qtec.access.auth.client.algorithm.impl;

import cn.qtec.access.auth.client.algorithm.IAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

/**
 * @author Created by zhangp on 2017/9/27.
 */
public class AlgorithmImpl implements IAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmImpl.class);

    private static final String ALGORITHM_HMAC_SHA256 = "HmacSHA256";
    private static final String ALGORITHM_PBKDF2_SHA256 = "PBKDF2WithHmacSHA256";
    private static final String ALGORITHM_SHA256 = "SHA-256";
    private static final String ALGORITHM_SHA1PRNG = "SHA1PRNG";
    private static final String ENCODING_UTF_8 = "UTF-8";

    //盐的长度 字节
    private static final int DEFAULT_SALT_SIZE = 32;
    //生成密文的长度 比特位
    private static final int HASH_BIT_SIZE = 256;

    /**
     * 使用 SHA1PRNG 生成器 生成随机盐
     *
     * @return 返回 32位 字节数组
     */
    public static byte[] generateRandom() {
        byte[] salt = new byte[DEFAULT_SALT_SIZE];
        try {
            SecureRandom random = SecureRandom.getInstance(ALGORITHM_SHA1PRNG);
            random.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException e) {
            logger.error("generate Salt error, {}", e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public byte[] getHmacSHA256(byte[] key, byte[] data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM_HMAC_SHA256);
            Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA256);
            mac.init(secretKeySpec);
            return mac.doFinal(data);
        } catch (Exception e) {
            logger.error("get HmacSHA256 Error, {}", e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public byte[] getSHA256(byte[] key) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_SHA256);
            return md.digest(key);
        } catch (NoSuchAlgorithmException e) {
            logger.error("get SHA256 Error, {}", e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public byte[] getPbkdf2SHA256(byte[] password, byte[] salt, int iterationCount) {
        try {
            char[] passwordChars = new String(password, ENCODING_UTF_8).toCharArray();
            KeySpec keySpec = new PBEKeySpec(passwordChars, salt, iterationCount, HASH_BIT_SIZE);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM_PBKDF2_SHA256);
            return secretKeyFactory.generateSecret(keySpec).getEncoded();
        } catch (Exception e) {
            logger.error("get PBKDF2-SHA256 Error, {}", e.getLocalizedMessage());
            return null;
        }
    }

    public byte[] getXor(byte[] data1, byte[] data2, int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (data1[i] ^ data2[i]);
        }
        return result;
    }

}
