package cn.qtec.qkcl.access.auth.client.jni;

import cn.qtec.qkcl.access.auth.client.utils.HexUtil;

/**
 * @author Created by zhangp on 2017/12/19
 */
public class CRC32Local {

    public static void main(String[] args) throws InterruptedException {
        System.loadLibrary("CRC32");
        byte[] data = HexUtil.hexStringToBytes("51007800440101003001010101aa010101ab011101011111053eb28feaf7625a0dcf27c266be78a30b0e5d90c4d662c029259ac7b124d8594a587b0da965a0d7c53a4f81dda183a640");

        CRC32Local crc32Local = new CRC32Local();

        long crc = crc32Local.getCrcValue(data);
        System.out.println("\nc++结果 crc = " + crc);
    }

    public native long getCrcValue(byte[] data);
}
