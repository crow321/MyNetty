package cn.qtec.qkcl.access.auth.client.entity;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangp
 * @date 2017/10/11
 */
public class AuthClientSessionInfo {
    private byte[] username;

    private byte[] sessionID;

    private byte[] sessionKey;

    private List<Channel> channels;

    public AuthClientSessionInfo() {

    }

    public AuthClientSessionInfo(byte[] username) {
        this.username = username;
    }

    public AuthClientSessionInfo(byte[] sessionID, byte[] sessionKey) {
        this.sessionID = sessionID;
        this.sessionKey = sessionKey;
    }

    public AuthClientSessionInfo(byte[] username, byte[] sessionID, byte[] sessionKey) {
        this.username = username;
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

    public byte[] getUsername() {
        return username;
    }

    public void setUsername(byte[] username) {
        this.username = username;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public void addChannel(Channel channel) {
        if (channels == null) {
            channels = new ArrayList<>();
        }
        channels.add(channel);
    }
}
