package cn.qtec.qkcl.access.auth.client.message;

import java.util.Arrays;

/**
 * @author zhangp
 * @date 2017/10/11
 */
public class AuthenticationMessage extends BaseMessage {
    private int messageType;
    private int requestID;
    private int status;
    //  2字节
    private int iterationCount;
    //  1字节
    private int deviceIDLength;
    //  deviceIDLength字节
    private byte[] deviceID;
    //  32字节
    private byte[] salt;
    //  32字节
    private byte[] serverNonce;
    //  32字节
    private byte[] serverProof;
    //  32字节
    private byte[] clientNonce;
    //  32字节
    private byte[] clientProof;
    //  32字节
    private byte[] rootKeyValue;
    //  32字节
    private byte[] quantumKeyValue;

    public AuthenticationMessage() {

    }

    public AuthenticationMessage(int version, int encryptionAlgorithm, int messageLength, byte[] encryptionKeyID, int deviceIDLength, byte[] deviceID) {
        super(version, encryptionAlgorithm, messageLength, encryptionKeyID);
        this.deviceIDLength = deviceIDLength;
        this.deviceID = deviceID;
    }

    public AuthenticationMessage(int messageType, int requestID, int status, int version, int encryptionAlgorithm, int messageLength, byte[] encryptionKeyID, int deviceIDLength, byte[] deviceID, byte[] serverProof) {
        this(version, encryptionAlgorithm, messageLength, encryptionKeyID, deviceIDLength, deviceID);
        this.messageType = messageType;
        this.requestID = requestID;
        this.status = status;
        this.serverProof = serverProof;
    }

    public AuthenticationMessage(int messageType, int requestID, int status, int version, int encryptionAlgorithm, int messageLength, byte[] encryptionKeyID, int iterationCount, int deviceIDLength, byte[] deviceID, byte[] salt, byte[] serverNonce) {
        this(version, encryptionAlgorithm, messageLength, encryptionKeyID, deviceIDLength, deviceID);
        this.messageType = messageType;
        this.requestID = requestID;
        this.status = status;
        this.iterationCount = iterationCount;
        this.salt = salt;
        this.serverNonce = serverNonce;
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

    public byte[] getRootKeyValue() {
        return rootKeyValue;
    }

    public void setRootKeyValue(byte[] rootKeyValue) {
        this.rootKeyValue = rootKeyValue;
    }

    public byte[] getQuantumKeyValue() {
        return quantumKeyValue;
    }

    public void setQuantumKeyValue(byte[] quantumKeyValue) {
        this.quantumKeyValue = quantumKeyValue;
    }

    @Override
    public String toString() {
        return "AuthenticationMessage{" +
                "deviceID=" + Arrays.toString(deviceID) +
                ", rootKeyId=" + Arrays.toString(super.getEncryptionKeyID()) +
                ", rootKeyValue=" + Arrays.toString(rootKeyValue) +
                '}';
    }
}
