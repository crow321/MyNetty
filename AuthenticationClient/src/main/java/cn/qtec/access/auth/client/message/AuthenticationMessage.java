package cn.qtec.access.auth.client.message;

import java.util.Arrays;

/**
 * @author zhangp
 * @date 2017/10/11
 */
public class AuthenticationMessage extends BaseMessage {
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

    private byte[] rootKeyValue;
    private byte[] quantumKeyValue;

    public AuthenticationMessage() {

    }

    public AuthenticationMessage(int version, int encryptionAlgorithm, int messageLength, byte[] encryptionKeyID, int deviceIDLength, byte[] deviceID) {
        super(version, encryptionAlgorithm, messageLength, encryptionKeyID);
        this.deviceIDLength = deviceIDLength;
        this.deviceID = deviceID;
    }

    public AuthenticationMessage(int version, int encryptionAlgorithm, int messageLength, byte[] encryptionKeyID, int deviceIDLength, byte[] deviceID, byte[] serverProof) {
        this(version, encryptionAlgorithm, messageLength, encryptionKeyID, deviceIDLength, deviceID);
        this.serverProof = serverProof;
    }

    public AuthenticationMessage(int version, int encryptionAlgorithm, int messageLength, byte[] encryptionKeyID, int iterationCount, int deviceIDLength, byte[] deviceID, byte[] salt, byte[] serverNonce) {
        this(version, encryptionAlgorithm, messageLength, encryptionKeyID, deviceIDLength, deviceID);
        this.iterationCount = iterationCount;
        this.salt = salt;
        this.serverNonce = serverNonce;
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

    @Override
    public String toString() {
        return "{" +

                "\n\t\tencryptedAlgorithm=" + getEncryptionAlgorithm() +
                "\n\t\tencryptedKeyID=" + Arrays.toString(getEncryptionKeyID()) +
                "\n\t\tmessageLength=" + getMessageLength() +
                "\n\t\titerationCount=" + iterationCount +
                ", \n\t\tdeviceIDLength=" + deviceIDLength +
                ", \n\t\tdeviceID=" + Arrays.toString(deviceID) +
                ", \n\t\tsalt=" + Arrays.toString(salt) +
                ", \n\t\tserverNonce=" + Arrays.toString(serverNonce) +
                ", \n\t\tserverProof=" + Arrays.toString(serverProof) +
                ", \n\t\tclientNonce=" + Arrays.toString(clientNonce) +
                ", \n\t\tclientProof=" + Arrays.toString(clientProof) +
                '}';
    }
}
