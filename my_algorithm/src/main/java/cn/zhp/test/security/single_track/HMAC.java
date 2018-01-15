package cn.zhp.test.security.single_track;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Created by zhangp on 2017/9/25.
 * @version v1.0.0
 */
public class HMAC {
    private static final String ALGORITHM_HMACMD5 = "HmacMD5";
    private static final String ALGORITHM_PBKDF2 = "PBKDF2WithHmacSHA1";

    public static String initMacKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_HMACMD5);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.encryptBASE64(secretKey.getEncoded());
    }

    /**
     * HMAC加密  ：主要方法
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static String hmacEncrypt(byte[] data,String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(Base64.decryptBASE64(key), ALGORITHM_HMACMD5);
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);
        return new String(mac.doFinal(data));
    }

    public static void main(String[] args) throws Exception {
        String data = "abc";
        String key = "123";
        String result = hmacEncrypt(data.getBytes(), key);
        System.out.println("========: " + result);
    }
}
