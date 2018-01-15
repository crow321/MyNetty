package cn.qtec.qkcl.access.auth.client.utils;

/**
 * @author Created by zhangp on 2017/12/26
 */
public class CRC32Util {
    private static final int POLYNOMIAL = 0x04C11DB7;
    private static int[] table = new int[256];

    public static void main(String[] args) throws InterruptedException {
        byte[] data = HexUtil.hexStringToBytes("53007800440101003001010101AA010101AB011101011111059F264A09248E5EF8C6AFAC8356A7575834E99D73B2608149A02DC252F13D2EDA52A9FDCDD32D08D22F5395407BC0D7EE");
        long crcJava = CRC32Util.getCRC32(data);
        System.out.println("java结果 crc = " + crcJava);
    }

    private static void initTable() {
        for (int i = 0; i < table.length; i++) {
            int crc = i << 24;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x80000000) != 0) {
                    crc = (crc << 1) ^ POLYNOMIAL;
                } else {
                    crc = crc << 1;
                }
            }
            table[i] = crc;
        }
    }

    /**
     * 与C++保持一致，可能与标准库java.util.zip 中的CRC32结果不同
     *
     * @param data
     * @return
     */
    public static long getCRC32(byte[] data) {
        initTable();
        int crc = 0xFFFFFFFF;
        for (byte b : data) {
            crc = ((crc << 8) ^ table[(((crc >> 24) ^ b) & 0xFF)]);
        }
        return ~crc & 0XFFFFFFFFL;
    }
}
