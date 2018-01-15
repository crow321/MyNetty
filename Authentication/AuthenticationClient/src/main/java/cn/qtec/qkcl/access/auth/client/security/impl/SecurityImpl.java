package cn.qtec.qkcl.access.auth.client.security.impl;

import cn.qtec.qkcl.access.auth.client.security.ISecurity;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

/**
 * @author zhangp
 * @date 2017/10/25
 */
public class SecurityImpl implements ISecurity {
    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding";
    //向量 16位
    private byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    @Override
    public byte[] encrypt(byte[] data, byte[] secretKey) throws Exception {
        Cipher encryptCipher = getCipher(Cipher.ENCRYPT_MODE, secretKey);
        return encryptCipher.doFinal(data);
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, byte[] secretKey) throws Exception {
        Cipher decryptCipher = getCipher(Cipher.DECRYPT_MODE, secretKey);
        return decryptCipher.doFinal(encryptedData);
    }

    private Cipher getCipher(int cipherMode, byte[] secretKey) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, KEY_ALGORITHM);
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
        cipher.init(cipherMode, secretKeySpec, new IvParameterSpec(iv));
        return cipher;
    }
}
