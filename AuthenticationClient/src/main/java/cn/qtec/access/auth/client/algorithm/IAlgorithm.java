package cn.qtec.access.auth.client.algorithm;

/**
 * @author Created by zhangp on 2017/9/27.
 * @version v1.0.0
 */
public interface IAlgorithm {
    /**
     * 使用 HmacSHA256 算法对 data 进行签名
     *
     * @param data 原始数据
     * @param key  密钥
     * @return
     * @throws Exception
     */
    byte[] getHmacSHA256(byte[] key, byte[] data);

    /**
     * 使用 SHA256 算法计算输入数据的哈希值
     *
     * @param key 原始数据
     * @return
     */
    byte[] getSHA256(byte[] key);

    /**
     * 使用 PBKDF2WithHmacSHA256 生成加盐哈希值
     *
     * @param password       明文密码
     * @param salt           盐值
     * @param iterationCount 迭代次数
     * @return
     */
    byte[] getPbkdf2SHA256(byte[] password, byte[] salt, int iterationCount);
}
