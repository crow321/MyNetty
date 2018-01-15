package cn.qtec.qkcl.access.auth.server.utils;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by zhangp
 *         2017/11/29
 */
public class SocketChannelMap {
    private static ConcurrentHashMap<String, Channel> sessionIdAndChannelMap = new ConcurrentHashMap<String, Channel>();

    public static void registerChannel(String sessionID, Channel channel) {
        sessionIdAndChannelMap.put(sessionID, channel);
    }

    public static Channel getChannelBySessionId(String sessionId) {
        return sessionIdAndChannelMap.get(sessionId);
    }

    public static void destroySessionInfoByChannel(Channel channel) {
        for (Map.Entry entry : sessionIdAndChannelMap.entrySet()) {
            if (entry.getValue() == channel) {
                String sessionId = (String) entry.getKey();
                sessionIdAndChannelMap.remove(sessionId);
            }
        }
    }
}
