package cn.qtec.qkcl.access.auth.client.entity;

import java.util.concurrent.CountDownLatch;

/**
 * @author Created by zhangp on 2017/12/21
 */
public class AuthAssistInfo {
    private String deviceId;
    private byte[] clientNonce;
    private byte[] quantumKey;
    private boolean isSuccess;
    private AuthClientSessionInfo sessionInfo;
    private CountDownLatch latch;

    public AuthAssistInfo() {
    }

    public AuthAssistInfo(String deviceId, byte[] clientNonce, byte[] quantumKey) {
        this.deviceId = deviceId;
        this.clientNonce = clientNonce;
        this.quantumKey = quantumKey;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public byte[] getClientNonce() {
        return clientNonce;
    }

    public void setClientNonce(byte[] clientNonce) {
        this.clientNonce = clientNonce;
    }

    public byte[] getQuantumKey() {
        return quantumKey;
    }

    public void setQuantumKey(byte[] quantumKey) {
        this.quantumKey = quantumKey;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public AuthClientSessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(AuthClientSessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }
}
