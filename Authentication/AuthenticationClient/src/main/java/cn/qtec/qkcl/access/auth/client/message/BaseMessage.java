package cn.qtec.qkcl.access.auth.client.message;

/**
 * Created by zhangp on 2017/10/11.
 */
public class BaseMessage {
    private int version;
    private int encryptionAlgorithm;
    private int messageLength;
    private byte[] encryptionKeyID;

    public BaseMessage() {

    }

    public BaseMessage(int version, int encryptionAlgorithm, int messageLength, byte[] encryptionKeyID) {
        this.version = version;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.messageLength = messageLength;
        this.encryptionKeyID = encryptionKeyID;
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

}
