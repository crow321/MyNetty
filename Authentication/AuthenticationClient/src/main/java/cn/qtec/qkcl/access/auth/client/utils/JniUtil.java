package cn.qtec.qkcl.access.auth.client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Created by zhangp
 *         2017/11/10
 */
public class JniUtil {
    private static final Logger logger = LoggerFactory.getLogger(JniUtil.class);
    private static final String DLL_PATH = "/lib/libjni_qkey_win_x64.dll";
    private static final String OS_NAME_WINDOWS = "Windows";
    private static final String OS_ARCH_X64 = "amd64";

    public static void loadJavaLibraryDir() {
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        logger.info("os name: {}, os arch: {}", osName, osArch);

        String dllRealPath;
        if (osName.startsWith(OS_NAME_WINDOWS)) {
            /* windows 环境下加载路径 */
            switch (osArch) {
                case OS_ARCH_X64:
                    dllRealPath = DLL_PATH;
                    break;
                //x86系统
                default:
                    dllRealPath = DLL_PATH.replace("64", "86");
                    break;
            }
        } else {
            /* linux 环境下加载路径 */
            dllRealPath = DLL_PATH.replace("win_x64.dll", "linux_x64.so");
        }

        try {
            loadLibraryFromJar(dllRealPath);
        } catch (IOException e) {
            //jar包中加载dll出现异常时尝试从classpath进行第二次加载
            loadFromClassPath();
        }

        logger.info("Load JNI Dynamic Link Library success");
    }

    /**
     * 打jar包后从jar包内进行加载动态库
     *
     * @param dllAbsolutePath dll文件的绝对路径
     * @throws IOException IO流异常
     */
    private static void loadLibraryFromJar(String dllAbsolutePath) throws IOException {
        InputStream in = null;
        FileOutputStream out = null;
        File dllFile = null;
        try {
            Class clazz = JniUtil.class;
            in = clazz.getResourceAsStream(dllAbsolutePath);

            File file = new File("");
            String filePath = file.getAbsolutePath() + File.separator + "lib" + File.separator + dllAbsolutePath.substring(5);

            dllFile = new File(filePath);

            if (!dllFile.exists()) {
                out = new FileOutputStream(dllFile, false);

                int i;
                byte[] buf = new byte[1024];

                while ((i = in.read(buf)) != -1) {
                    out.write(buf, 0, i);
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }

        System.load(dllFile.getAbsolutePath());
        dllFile.deleteOnExit();
    }

    /**
     * 从 classpath:/lib/libjni_qkey_win_x64.dll 尝试加载动态库
     */
    private static void loadFromClassPath() {
        URL url = JniUtil.class.getClassLoader().getResource("");
        String path = null;
        if (url != null) {
            path = url.getPath().substring(1) + DLL_PATH;
        }
        System.load(path);
    }

}
