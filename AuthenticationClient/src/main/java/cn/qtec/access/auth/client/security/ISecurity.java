package cn.qtec.access.auth.client.security;

/**
 * 加解密模块
 *
 * @author zhangp
 * @date 2017/10/25
 */
public interface ISecurity {

    /**
     * 使用加密密钥进行加密
     *
     * @param data      原始数据
     * @param secretKey 加密密钥
     * @return
     * @throws Exception
     */
    byte[] encrypt(byte[] data, byte[] secretKey) throws Exception;

    /**
     * 使用加密密钥进行解密
     *
     * @param encryptedData 加密的数据
     * @param secretKey     加密密钥
     * @return
     * @throws Exception
     */
    byte[] decrypt(byte[] encryptedData, byte[] secretKey) throws Exception;
}
