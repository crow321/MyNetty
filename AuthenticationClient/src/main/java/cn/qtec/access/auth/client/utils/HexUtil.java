package cn.qtec.access.auth.client.utils;

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
        return sb.toString().toUpperCase();
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

    public static byte[] bigEndianAddShortToBytes(int intValue) {
        byte[] result = new byte[2];
        if (intValue < DEFAULT_BYTE_VALUE) {
            result[0] = 0;
            result[1] = (byte) intValue;
        } else {
            result[0] = (byte) (intValue >> 8);
            result[1] = (byte) intValue;
        }
        return result;
    }

    public static int bigEndianAddBytesToShort(byte[] bigBytesValue) {
        int length = bigBytesValue.length;
        int result = 0;
        for (byte b : bigBytesValue) {
            result += b >= 0 ? b : (256 + b) << (length - 1) * 8;
        }
        return result;
    }

    public static byte[] littleEndianAddShortToBytes(int value) {
        byte[] result = new byte[2];
        if (value < DEFAULT_BYTE_VALUE) {
            result[0] = (byte) value;
            result[1] = 0;
        } else {
            result[0] = (byte) value;
            result[1] = (byte) (value >> 8);
        }
        return result;
    }

    public static int parseLittleEndianBytes(byte[] bytes) {
        int length = bytes.length;
        int value = 0;
        for (int i = 0; i < length; i++) {
            value += bytes[i] >= 0 ? bytes[i] << 8 * (length - i - 1) : (bytes[i] + 256) << 8 * (length - i - 1);
        }
        return value;
    }

    public int getPadLength(int length) {
        return DEFAULT_BYTE_LENGTH - length / DEFAULT_BYTE_LENGTH;
    }
}
