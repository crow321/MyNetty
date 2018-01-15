package cn.qtec.qkcl.access.auth.client.security;

/**
 *
 * Created by duhc on 2017/4/19.
 */
public class CrypUtils {
    /**
     * Dao层对key进行加密或解密接口
     * @param passByte
     * @return
     */
    public static byte[] passCryp(byte[] passByte) {
        int i = 0;
        int j = 0;
        char key[] = "g?ol0d!en@s7ec.1u8r$ityf*e#rr3*yw&a^y".toCharArray();
        char key1[] = "3g!#d34&fddf*d4adfd8)de+^dad*d57#daTga".toCharArray();
        char key2[] = "*dne71#dc&ia?yad>lad,ad3h*aducat3~da3)d".toCharArray();
        char key3[] = "-vdg9e*dqa1cF?Ka3,d3emca*^1p)u5i]ag2r*de".toCharArray();
        for (i = 0; i < passByte.length; i++) {
            if (i % 2 == 0) {
                if (i % 5 == 0) {
                    passByte[i] = (byte) (passByte[i] ^ key[j]);
                } else {
                    passByte[i] = (byte) (passByte[i] ^ key1[j]);
                }
            } else {
                if (i % 3 == 0) {
                    passByte[i] = (byte) (passByte[i] ^ key2[j]);
                } else {
                    passByte[i] = (byte) (passByte[i] ^ key3[j]);
                }
            }
            j++;
            if (j > 36) {
                j = 0;
            }
        }
        return passByte;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * Convert hex string to byte[]
     *
     * @return byte[]
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * 转化十六进制编码为字符串
     *
     * @param s
     * @return
     */
    public static String toStringHex(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }
}