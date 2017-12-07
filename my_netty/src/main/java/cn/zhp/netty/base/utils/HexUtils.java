package cn.zhp.netty.base.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * @author Created by zhangp on 2017/9/26.
 * @version v1.0.0
 */
public class HexUtils {
    private static final Logger logger = LoggerFactory.getLogger(HexUtils.class);

    public static String bytesToHexString(byte[] data) {
        int length = data.length;
        StringBuilder hex = new StringBuilder(length);
        for (byte b : data) {
            int v = b & 0xFF;
            if (v < 16) {
                hex.append('0');
            }
            hex.append(Integer.toHexString(v).toUpperCase());
        }
        return hex.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        ArrayList<Byte> al = hexStringToArrayList(hexString);
        int length = 0;
        if (al != null) {
            length = al.size();
        }
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = al.get(i);
        }
        return bytes;
    }

    public static ArrayList<Byte> hexStringToArrayList(String hexString) {
        if (hexString == null || hexString.equals("")) {
            logger.error("Receive hexString SHALL NOT Be NULL!");
            return null;
        }
        int length = hexString.length();
        ArrayList<Byte> al = new ArrayList<>();
        for (int i = 0; i < length-1; i += 2) {
            al.add((byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16)));

        }
        return al;
    }

}
