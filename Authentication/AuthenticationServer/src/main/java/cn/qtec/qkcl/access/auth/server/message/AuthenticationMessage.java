package cn.qtec.qkcl.access.auth.server.message;

/**
 * Created by zhangp on 2017/10/11.
 */
public class AuthenticationMessage {
    private int messageType;
    private int requestID;
    private int status;
    private int version;
    private int encryptionAlgorithm;
    private int messageLength;
    private byte[] encryptionKeyID;
    //  2字节
    private int iterationCount;
    //  1字节
    private int deviceIDLength;
    //  deviceIDLength字节 8
    private byte[] deviceID;
    //  32字节
    private byte[] salt;
    //  32字节
    private byte[] serverNonce;
    private byte[] serverProof;
    //  32字节
    private byte[] clientNonce;
    private byte[] clientProof;

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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(int encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

    public byte[] getEncryptionKeyID() {
        return encryptionKeyID;
    }

    public void setEncryptionKeyID(byte[] encryptionKeyID) {
        this.encryptionKeyID = encryptionKeyID;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public int getDeviceIDLength() {
        return deviceIDLength;
    }

    public void setDeviceIDLength(int deviceIDLength) {
        this.deviceIDLength = deviceIDLength;
    }

    public byte[] getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(byte[] deviceID) {
        this.deviceID = deviceID;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getServerNonce() {
        return serverNonce;
    }

    public void setServerNonce(byte[] serverNonce) {
        this.serverNonce = serverNonce;
    }

    public byte[] getServerProof() {
        return serverProof;
    }

    public void setServerProof(byte[] serverProof) {
        this.serverProof = serverProof;
    }

    public byte[] getClientNonce() {
        return clientNonce;
    }

    public void setClientNonce(byte[] clientNonce) {
        this.clientNonce = clientNonce;
    }

    public byte[] getClientProof() {
        return clientProof;
    }

    public void setClientProof(byte[] clientProof) {
        this.clientProof = clientProof;
    }
}
