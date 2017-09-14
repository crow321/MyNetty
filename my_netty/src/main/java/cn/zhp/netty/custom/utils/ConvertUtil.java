package cn.zhp.netty.custom.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


/**
 * @version v1.3
 *          Created by zhangp on 2017/9/12.
 */
public class ConvertUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConvertUtil.class);

    public static byte[] arrayListToBytes(ArrayList<Byte> al) {
        if (al != null && al.size() > 0) {
            int size = al.size();
            byte[] resultBytes = new byte[size];
            for (int i = 0; i < size; i++) {
                resultBytes[i] = al.get(i);
            }
            return resultBytes;
        }
        logger.error("_________________传入参数为空...");
        return null;
    }

    public static ArrayList<Byte> bytesToArrayList(byte[] src) {
        if (src != null && src.length > 0) {
            ArrayList<Byte> result = new ArrayList<>();
            int length = src.length;
            for (int i = 0; i < length; i++) {
                result.add(src[i]);
            }
            return result;
        }
        logger.error("_________________传入参数为空...");
        return null;
    }
}
