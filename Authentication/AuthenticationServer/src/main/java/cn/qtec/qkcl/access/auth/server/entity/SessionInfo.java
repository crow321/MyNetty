package cn.qtec.qkcl.access.auth.server.entity;

/**
 * @author zhangp
 * @date 2017/10/11
 */
public class SessionInfo {
    private String deviceId;
    private String sessionId;
    private byte[] sessionKey;

    public SessionInfo() {

    }

    public SessionInfo(String sessionId, byte[] sessionKey) {
        this.sessionId = sessionId;
        this.sessionKey = sessionKey;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
