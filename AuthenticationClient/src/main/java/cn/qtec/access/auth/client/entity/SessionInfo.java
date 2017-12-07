package cn.qtec.access.auth.client.entity;

import io.netty.channel.socket.SocketChannel;

/**
 * @author zhangp
 * @date 2017/10/11
 */
public class SessionInfo {

    private byte[] sessionID;

    private byte[] sessionKey;

    private SocketChannel socketChannel;

    public SessionInfo() {

    }

    public SessionInfo(byte[] sessionID, byte[] sessionKey) {
        this.sessionID = sessionID;
        this.sessionKey = sessionKey;
    }

    public byte[] getSessionID() {
        return sessionID;
    }

    public void setSessionID(byte[] sessionID) {
        this.sessionID = sessionID;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}
