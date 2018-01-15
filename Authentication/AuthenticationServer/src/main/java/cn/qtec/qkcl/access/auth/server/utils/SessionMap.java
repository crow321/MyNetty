package cn.qtec.qkcl.access.auth.server.utils;

import cn.qtec.qkcl.access.auth.server.entity.SessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by zhangp
 *         2017/11/29
 */
public class SessionMap {
    private static final Logger logger = LoggerFactory.getLogger(SessionMap.class);

    private static ConcurrentHashMap<String, SessionInfo> sessionMap = new ConcurrentHashMap<>();

    public static void addSessionMap(String sessionIdOrDeviceId, SessionInfo sessionInfo) {
        if (sessionIdOrDeviceId == null || sessionInfo == null || "".equals(sessionIdOrDeviceId)) {
            logger.error("SessionId or SessionKeyValue is NULL");
            return;
        }
        sessionMap.put(sessionIdOrDeviceId, sessionInfo);
    }

    public static SessionInfo getSessionInfoBySessionId(String sessionIdOrDeviceId) {
        if (sessionIdOrDeviceId == null || "".equals(sessionIdOrDeviceId)) {
            logger.error("SessionId is NULL");
            return null;
        }
        return sessionMap.get(sessionIdOrDeviceId);
    }

    public static boolean removeBySessionIdOrDeviceId(String sessionIdOrDeviceId) {
        if (sessionIdOrDeviceId == null || "".equals(sessionIdOrDeviceId)) {
            logger.error("SessionId is NULL");
            return false;
        }
        SessionInfo sessionInfo = sessionMap.remove(sessionIdOrDeviceId);
        if (sessionInfo == null) {
            logger.error("There is no SessionInfo in map with key:{}", sessionIdOrDeviceId);
            return false;
        }

        SessionInfo res;
        if (sessionInfo.getSessionId().equals(sessionIdOrDeviceId)) {
            res = sessionMap.remove(sessionInfo.getDeviceId());
            if (res == null) {
                logger.error("There is no SessionInfo in map with deviceId:{}", sessionInfo.getDeviceId());
                return false;
            }
        } else if (sessionInfo.getDeviceId().equals(sessionIdOrDeviceId)) {
            res = sessionMap.remove(sessionInfo.getSessionId());
            if (res == null) {
                logger.error("There is no SessionInfo in map with sessionId:{}", sessionInfo.getSessionId());
                return false;
            }
        }

        return true;
    }
}
