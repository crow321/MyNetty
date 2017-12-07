package cn.qtec.access.auth.client.entity;

/**
 * @author Created by zhangp
 *         2017/11/30
 */
public class AccessAuthInfo {
    //加密算法 0-不加密 1-AES加密
    private int encryptionAlgorithm;
    //加密密钥ID
    private byte[] rootKeyId;
    //加密密钥值
    private byte[] rootKeyValue;
    //设备ID
    private byte[] deviceID;
    //密钥
    private byte[] quantumKeyValue;

    public AccessAuthInfo(byte[] deviceID, byte[] quantumKeyValue) {
        this.deviceID = deviceID;
        this.quantumKeyValue = quantumKeyValue;
    }

    public AccessAuthInfo(int encryptionAlgorithm, byte[] rootKeyId, byte[] rootKeyValue, byte[] deviceID, byte[] quantumKeyValue) {
        this(deviceID, quantumKeyValue);
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.rootKeyId = rootKeyId;
        this.rootKeyValue = rootKeyValue;
    }

    public int getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(int encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public byte[] getRootKeyId() {
        return rootKeyId;
    }

    public void setRootKeyId(byte[] rootKeyId) {
        this.rootKeyId = rootKeyId;
    }

    public byte[] getRootKeyValue() {
        return rootKeyValue;
    }

    public void setRootKeyValue(byte[] rootKeyValue) {
        this.rootKeyValue = rootKeyValue;
    }

    public byte[] getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(byte[] deviceID) {
        this.deviceID = deviceID;
    }

    public byte[] getQuantumKeyValue() {
        return quantumKeyValue;
    }

    public void setQuantumKeyValue(byte[] quantumKeyValue) {
        this.quantumKeyValue = quantumKeyValue;
    }
}
