package cn.zhp.test.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Created by zhangp on 2017/9/25.
 * @version v1.0.0
 */
public class FileUtils {
    private static final String PATH = "my_algorithm/src/test/1.txt";

    private static FileOutputStream out;
    private static FileInputStream in;

    static {
        try {
            in = new FileInputStream(new File(PATH));
            out = new FileOutputStream(new File(PATH));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void read() {
        try {
            //可读总数
            int available = in.available();
            byte[] res = new byte[available];
            for (int i = 0; i < available; i++) {
                //read()方法读取的是int值
                int value = in.read();
                res[i] = (byte) value;
            }

            System.out.println(new String(res));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(byte[] data) {
        try {
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        write("test1".getBytes());
        read();
    }
}
