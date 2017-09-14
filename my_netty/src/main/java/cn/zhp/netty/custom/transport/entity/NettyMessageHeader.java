package cn.zhp.netty.custom.transport.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息头
 * @author Created by zhangp on 2017/9/12.
 * @version v1.0.0
 */
public final class NettyMessageHeader {
    //消息校验码
    private int crcCode = 0xABEF01;
    //版本
    private byte version;
    //会话ID
    private long sessionID;
    //消息类型
    private byte messageType;
    //消息优先级
    private byte priority;
    //消息长度
    private short messageLength;
    //附件
    private Map<String, Object> attachment = new HashMap<>();

    public final int getCrcCode() {
        return crcCode;
    }

    public final void setCrcCode(int crcCode) {
        this.crcCode = crcCode;
    }

    public final byte getVersion() {
        return version;
    }

    public final void setVersion(byte version) {
        this.version = version;
    }

    public final long getSessionID() {
        return sessionID;
    }

    public final void setSessionID(long sessionID) {
        this.sessionID = sessionID;
    }

    public final byte getMessageType() {
        return messageType;
    }

    public final void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public final byte getPriority() {
        return priority;
    }

    public final void setPriority(byte priority) {
        this.priority = priority;
    }

    public final short getMessageLength() {
        return messageLength;
    }

    public final void setMessageLength(short messageLength) {
        this.messageLength = messageLength;
    }

    @Override
    public String toString() {
        return "MessageHeader{" +
                "crcCode=" + crcCode +
                ", version=" + version +
                ", sessionID=" + sessionID +
                ", messageType=" + messageType +
                ", priority=" + priority +
                ", messageLength=" + messageLength +
//                ", attachment=" + attachment +
                '}';
    }

    public Map<String, Object> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, Object> attachment) {
        this.attachment = attachment;
    }
}
