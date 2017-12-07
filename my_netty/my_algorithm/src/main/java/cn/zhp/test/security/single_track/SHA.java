package cn.zhp.test.security.single_track;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 安全哈希算法（Secure Hash Algorithm）主要适用于数字签名标准（Digital Signature Standard DSS）
 * 里面定义的数字签名算法（Digital Signature Algorithm DSA）。对于长度小于2^64位的消息，
 * SHA1会产生一个 160位 的消息摘要。
 * 该算法经过加密专家多年来的发展和改进已日益完善，并被广泛使用。该算法的思想是接收一段明文，
 * 然后以一种不可逆的方式将它转换成一段（通常更小）密文，也可以简单的理解为取一串输入码（称为预映射或信息），
 * 并把它们转化为长度较短、位数固定的输出序列即散列值（也称为信息摘要或信息认证代码）的过程。
 * 散列函数值可以说是对明文的一种“指纹”或是“摘要”所以对散列值的数字签名就可以视为对此明文的数字签名。
 *
 * @author Created by zhangp on 2017/9/25.
 * @version v1.0.0
 */
public class SHA {
    public static final String KEY_SHA = "SHA";

    public static String getResult(String input) {
        System.out.println("加密前的数据: " + input);
        BigInteger value = null;
        try {
            MessageDigest md = MessageDigest.getInstance(KEY_SHA);
            md.update(input.getBytes());
            value = new BigInteger(md.digest());
            System.out.println("SHA加密后: " + value.toString(32));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return value.toString(32);
    }

    public static void main(String[] args) {
        String input = "123";
        getResult(input);
        //output:
        //82ug05b311fs6kb56afa3vqsbr5tnfnf
    }
}
/*SHA-1与MD5的比较

因为二者均由MD4导出，SHA-1和MD5彼此很相似。相应的，他们的强度和其他特性也是相似，但还有以下几点不同：
l 对强行攻击的安全性：最显著和最重要的区别是SHA-1摘要比MD5摘要长32 位。使用强行技术，产生任何一个报文使其摘要等于给定报摘要的难度对MD5是2^128数量级的操作，而对SHA-1则是2^160数量级的操作。这样，SHA-1对强行攻击有更大的强度。
l 对密码分析的安全性：由于MD5的设计，易受密码分析的攻击，SHA-1显得不易受这样的攻击。
l 速度：在相同的硬件上，SHA-1的运行速度比MD5慢。*/
