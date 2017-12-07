package test.utils;

import cn.zhp.netty.custom.utils.TransportUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.BaseJunit4Test;

import java.util.Arrays;
import java.util.Random;

/**
 * @version v1.3
 *          Created by zhangp on 2017/9/12.
 */
public class ConvertUtilTest extends BaseJunit4Test {
    @Autowired
    private TransportUtil transportUtil;

    @Test
    public void arrayListToBytes() throws Exception {
        Random random = new Random();
        int count = 0;
//        while (count > 1) {
        byte[] bytes = new byte[1024];
        for (int i = 0; i < 1024; i++) {

            int value = random.nextInt(128)-127;
            System.out.println("==========value:" + value);
            if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {

                bytes[i] = (byte) value;
            }
        }
//        String hex = HexTools.byteArrayToHexString(bytes);
        System.out.println("random: " + Arrays.toString(bytes));
//            RandomDigit randomDigit = new RandomDigit();
//            randomDigit.setId(UUID.randomUUID().toString());
//            randomDigit.setValue(bytes);
//
//            randomDigitDao.insert(randomDigit);
//            count ++;
//            System.out.println("===============> count: " + count);
//        }

    }


}