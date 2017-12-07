package test.utils;

import cn.zhp.netty.base.utils.HexUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Created by zhangp on 2017/9/26.
 * @version v1.0.0
 */
public class HexUtilsTest {
    @Test
    public void bytesToHexString() throws Exception {
        byte[] data = {1, 2, -120};
//        byte[] data = new byte[4];
        String hex = HexUtils.bytesToHexString(data);
        System.out.println("hexString:"+ hex);
        System.out.println("bytes:" + Arrays.toString(HexUtils.hexStringToBytes("12345")));
    }

}