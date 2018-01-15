package cn.qtec.qkcl.access.auth.server.process;

import cn.qtec.qkcl.access.auth.server.utils.SessionMap;
import cn.qtec.qkcl.message.process.Disconnect;

/**
 * @author Created by zhangp
 *         2017/11/29
 */
public class AuthServerDisconnect implements Disconnect {

    @Override
    public void doDisconnect(String s) {
        SessionMap.removeBySessionIdOrDeviceId(s);
    }
}
