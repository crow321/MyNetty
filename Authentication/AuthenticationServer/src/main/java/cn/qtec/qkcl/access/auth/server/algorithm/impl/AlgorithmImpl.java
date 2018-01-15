package cn.qtec.qkcl.access.auth.server.algorithm.impl;

import cn.qtec.qkcl.access.auth.server.algorithm.IAlgorithm;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
@Component
public class AlgorithmImpl implements IAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmImpl.class);

    private static final String ALGORITHM_HMAC_SHA256 = "HmacSHA256";
    private static final String ALGORITHM_PBKDF2_SHA256 = "PBKDF2WithHmacSHA256";
    private static final String ALGORITHM_SHA256 = "SHA-256";
    private static final String ALGORITHM_SHA1PRNG = "SHA1PRNG";

    //盐的长度 字节
    private static final int DEFAULT_SALT_SIZE = 32;
    //生成密文的长度 比特位
    private static final int HASH_BIT_SIZE = 256;

    @Override
    public byte[] getHmacSHA256(byte[] key, byte[] data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM_HMAC_SHA256);
            Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA256);
            mac.init(secretKeySpec);
            return mac.doFinal(data);
        } catch (Exception e) {
            logger.error("Get HmacSHA256 Error: {}", e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public byte[] getSHA256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_SHA256);
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Get SHA256 Error: {}", e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public byte[] getPbkdf2SHA256(byte[] passwordBytes, byte[] salt, int iterations) {
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(passwordBytes, salt, iterations);
        return ((KeyParameter) gen.generateDerivedParameters(HASH_BIT_SIZE)).getKey();
    }

    @Override
    public byte[] getPbkdf2SHA256(String password, byte[] salt, int iterationCount) {
        try {
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, HASH_BIT_SIZE);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM_PBKDF2_SHA256);
            return secretKeyFactory.generateSecret(keySpec).getEncoded();
        } catch (Exception e) {
            logger.error("Generate PBKDF2-SHA256 Error, {}", e.getLocalizedMessage());
            return null;
        }
    }
    /**
     * 使用 SHA1PRNG 生成器 生成随机盐
     *
     * @return 返回 32位 字节数组
     */
    @Override
    public byte[] generateRandom(int length) {
        byte[] salt = new byte[length];
        try {
            SecureRandom random = SecureRandom.getInstance(ALGORITHM_SHA1PRNG);
            random.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Generate Salt error: {}", e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public byte[] getXor(byte[] data1, byte[] data2, int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (data1[i] ^ data2[i]);
        }
        return result;
    }

}
