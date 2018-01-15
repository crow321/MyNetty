package cn.qtec.qkcl.access.auth.client.utils;

/**
 * @author Created by zhangp on 2017/9/27.
 * @version v1.0.0
 */
public class HexUtil {
    private static final int DEFAULT_BYTE_VALUE = 0xFF;
    private static final int DEFAULT_BYTE_LENGTH = 0x10;

    /**
     * 字节数组转成十六进制字符串
     *
     * @param byteArray
     * @return
     */
    public static String bytesToHexString(byte[] byteArray) {
        int length = byteArray.length;
        StringBuilder sb = new StringBuilder(length);
        for (byte b : byteArray) {
            int v = b & 0xFF;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));

        }
        return sb.toString().toLowerCase();
    }

    /**
     * 十六进制字符串转成字节数组
     *
     * @param hex
     * @return
     */
    public static byte[] hexStringToBytes(String hex) {
        int length = hex.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length - 1; i += 2) {
            data[i / 2] = (byte) (((Character.digit(hex.charAt(i), 16) << 4) + (Character.digit(hex.charAt(i + 1), 16))));
        }
        return data;
    }

    public static byte[] toLittleEndianBytes(long value, int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (value >> (8 * i) & 0xFF);
        }
        return result;
    }

    /**
     * 返回无符号整型
     * @param bytes
     * @return
     */
    public static long parseLittleEndianBytes(byte[] bytes) {
        int length = bytes.length;
        long value = 0;
        for (int i = 0; i < length; i++) {
            long temp = bytes[i] >= 0 ? bytes[i] << 8 * i : (bytes[i] + 256) << 8 * i;
            value += temp & 0xFFFFFFFFL;
        }
        return value;
    }

    public static byte[] toBigEndianBytes(long value, int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (value >> (8 * (length - i - 1)) & 0xFF);
        }
        return result;
    }

    /**
     * 返回无符号整型
     * @param bytes
     * @return
     */
    public static long parseBigEndianBytes(byte[] bytes) {
        int length = bytes.length;
        long value = 0;
        for (int i = 0; i < length; i++) {
            long temp = bytes[i] >= 0 ? bytes[i] << 8 * (length - i - 1) : (bytes[i] + 256) << 8 * (length - i - 1);
            value += temp & 0xFFFFFFFFL;
        }
        return value;
    }
}
