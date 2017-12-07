package cn.qtec.access.auth.client.message;

/**
 * @author Created by zhangp on 2017/9/28.
 */
public class TransportMessage extends BaseMessage {
    private int messageType;
    private int requestID;
    private int status;
    private byte[] body;

    public TransportMessage() {

    }

    public TransportMessage(int version, int encryptionAlgorithm, int messageLength, byte[] encryptionKeyID, int messageType, int requestID, int status, byte[] body) {
        super(version, encryptionAlgorithm, messageLength, encryptionKeyID);
        this.messageType = messageType;
        this.requestID = requestID;
        this.status = status;
        this.body = body;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
