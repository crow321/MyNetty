package cn.qtec.access.auth.client.utils;

import java.lang.reflect.Field;

/**
 * @author Created by zhangp
 *         2017/11/10
 */
public class JniUtil {
    public static final String LIBRARY_PATH_WINDOW_x64 = "D:\\IDEA\\Git\\RocDo\\AuthenticationClient\\bin\\lib\\windows\\x64";
    private static final String LIBRARY_NAME = "JNI_QKey";

    public static void addLibraryDir(String libraryPath) throws Exception {
        System.setProperty("java.library.path", System.getProperty("java.library.path") + ";" + libraryPath);
        Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
        fieldSysPath.setAccessible(true);
        fieldSysPath.set(null, null);

        System.loadLibrary(LIBRARY_NAME);
    }
}
