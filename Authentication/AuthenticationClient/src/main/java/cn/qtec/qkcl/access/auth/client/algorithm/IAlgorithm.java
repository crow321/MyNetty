package cn.qtec.qkcl.access.auth.client.algorithm;

/**
 * @author Created by zhangp on 2017/9/27.
 * @version v1.0.0
 */
public interface IAlgorithm {
    /**
     * 使用 HMAC-MD5 签名方法对对 data 进行签名
     *
     * @param data 被签名的数据
     * @param key  密钥
     * @return 加密后的数据
     */
    byte[] getHmacSHA256(byte[] key, byte[] data);

    /**
     * 使用SHA256加密，返回加密结果
     *
     * @param key
     * @return
     */
    byte[] getSHA256(byte[] key);

    /**
     * 生成加盐密文
     * password 类型为字节数组时使用
     * @param password      明文密码
     * @param salt          盐
     * @param iterations    迭代次数
     * @return
     */
    byte[] getPbkdf2SHA256(byte[] password, byte[] salt, int iterations);
    /**
     * 生成加盐密文
     * password 类型为字符串时使用
     * @param password          明文密码
     * @param salt              盐
     * @param iterationCount    迭代次数
     * @return
     */
    byte[] getPbkdf2SHA256(String password, byte[] salt, int iterationCount);

    /**
     * 使用 SHA1PRNG 生成器 生成随机盐
     * @param length    返回字节数组长度
     * @return 返回 32位 字节数组
     */
    byte[] generateRandom(int length);

    byte[] getXor(byte[] data1, byte[] data2, int length);

}
