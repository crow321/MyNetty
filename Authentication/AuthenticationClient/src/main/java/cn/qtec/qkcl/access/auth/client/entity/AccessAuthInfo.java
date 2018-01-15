package cn.qtec.qkcl.access.auth.client.entity;

import cn.qtec.qkcl.access.auth.client.constant.AuthModeEnum;
import cn.qtec.qkcl.kmip.transport.transfer.TransEnum;

/**
 * @author Created by zhangp
 *         2017/11/30
 */
public class AccessAuthInfo {
    /**
     * IP地址     必填
     */
    private String remoteIP;
    /**
     * 端口       必填
     */
    private int port;
    /**
     * 传输层方式    默认为TCP
     */
    private TransEnum transEnum;
    /**
     * 接入认证模式   默认使用本地方式
     * Q_SHIELD: Q盾用户认证使用
     * LOCAL:   普通用户认证使用
     */
    private AuthModeEnum authModeEnum;
    /**
     * username     使用LOCAL认证时必填
     */
    private byte[] deviceID;
    /**
     * 原始密钥     使用LOCAL认证时必填
     * 32字节
     */
    private byte[] quantumKeyValue;
    /**
     * 加密方式     使用LOCAL认证时必填，默认为不加密
     * 0-不加密 1-AES加密
     */
    private int encryptionAlgorithm;
    /**
     * 加密密钥ID   使用LOCAL认证时且AES加密时必填
     * 16字节
     */
    private byte[] rootKeyId;
    /**
     * 加密密钥值    使用LOCAL认证时且AES加密时必填
     * 32字节   AES加密时必填
     */
    private byte[] rootKeyValue;

    public AccessAuthInfo() {

    }

    public AccessAuthInfo(String remoteIP, int port, TransEnum transEnum, AuthModeEnum authModeEnum) {
        this.remoteIP = remoteIP;
        this.port = port;
        this.transEnum = transEnum;
        this.authModeEnum = authModeEnum;
    }

    public AccessAuthInfo(String remoteIP, int port, TransEnum transEnum, AuthModeEnum authModeEnum, byte[] deviceID, byte[] quantumKeyValue, int encryptionAlgorithm, byte[] rootKeyId, byte[] rootKeyValue) {
        this.remoteIP = remoteIP;
        this.port = port;
        this.transEnum = transEnum;
        this.authModeEnum = authModeEnum;
        this.deviceID = deviceID;
        this.quantumKeyValue = quantumKeyValue;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.rootKeyId = rootKeyId;
        this.rootKeyValue = rootKeyValue;
    }

    public String getRemoteIP() {
        return remoteIP;
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public TransEnum getTransEnum() {
        return transEnum;
    }

    public void setTransEnum(TransEnum transEnum) {
        this.transEnum = transEnum;
    }

    public AuthModeEnum getAuthModeEnum() {
        return authModeEnum;
    }

    public void setAuthModeEnum(AuthModeEnum authModeEnum) {
        this.authModeEnum = authModeEnum;
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
}
