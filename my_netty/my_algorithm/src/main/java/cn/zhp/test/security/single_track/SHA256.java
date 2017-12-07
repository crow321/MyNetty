package cn.zhp.test.security.single_track;

import org.apache.commons.codec.binary.Hex;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Created by zhangp on 2017/9/25.
 * @version v1.0.0
 */
public class SHA256 {
    public static final String KEY_SHA256 = "SHA-256";

    public static String getResult(String input) {
        System.out.println("SHA256加密前的数据: " + input);
        byte[] value = null;
        try {
            MessageDigest md = MessageDigest.getInstance(KEY_SHA256);
            md.update(input.getBytes());
            value = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return value != null ? Hex.encodeHexString(value) : "";
    }

    public static void main(String[] args) {
        String input = "123";
        String res = getResult(input);
        System.out.println("字符串长度："+res.length());
        System.out.println("SHA256加密前的数据: " + res);
        //output:
        //82ug05b311fs6kb56afa3vqsbr5tnfnf
        //a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3
    }
}
/*SHA-1与MD5的比较

因为二者均由MD4导出，SHA-1和MD5彼此很相似。相应的，他们的强度和其他特性也是相似，但还有以下几点不同：
l 对强行攻击的安全性：最显著和最重要的区别是SHA-1摘要比MD5摘要长32 位。使用强行技术，产生任何一个报文使其摘要等于给定报摘要的难度对MD5是2^128数量级的操作，而对SHA-1则是2^160数量级的操作。这样，SHA-1对强行攻击有更大的强度。
l 对密码分析的安全性：由于MD5的设计，易受密码分析的攻击，SHA-1显得不易受这样的攻击。
l 速度：在相同的硬件上，SHA-1的运行速度比MD5慢。*/
