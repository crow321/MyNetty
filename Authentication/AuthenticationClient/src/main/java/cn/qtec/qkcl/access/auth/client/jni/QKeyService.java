package cn.qtec.qkcl.access.auth.client.jni;

public class QKeyService {
    private static volatile QKeyService INSTANCE;
    /**
     * 设备ID
     */
    private byte[] deviceID;
    /**
     * Q盾生成的开始接入认证报文
     */
    private byte[] startAuthInfo;
    /**
     * 会话ID       16字节
     */
    private byte[] sessionID;
    /**
     * 会话密钥     32字节
     */
    private byte[] sessionKey;

    private QKeyService() {

    }

    public static QKeyService getINSTANCE() {
        if (INSTANCE == null) {
            synchronized (QKeyService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new QKeyService();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Q盾获取启动认证信息接口 GenStartInfo
     * 功能：用于生成客户端的认证信息
     *
     * @return QKeyService
     *      startAuthInfo 客户端开始接入认证信息;
     *      deviceID      设备ID
     */
    public native QKeyService genStartInfo();

    /**
     * Q盾生成认证信息接口 GenAuthInfo
     * 功能：用于生成客户端的认证信息
     *
     * @param inChallengeInfo 服务端返回的挑战报文信息
     * @return OutReqAuthInfo      用于客户端的接收认证信息
     */
    public native byte[] genAuthInfo(byte[] inChallengeInfo);

    /**
     * 调用Q盾验证认证信息接口 CheckAuthInfo
     * 功能：用于生成客户端的认证信息
     *
     * @param inAuthInfo 认证信息，由服务器端通过挑战报文带回
     * @return QKeyService, 包含信息如下：
     * 1) 会话密钥ID   sessionID        16字节
     * 2) 会话密钥Value sessionKey      32字节
     */
    public native QKeyService checkAuthInfo(byte[] inAuthInfo);

    /**
     * Q盾加密数据接口（保留接口）EncryptQuantumKeys
     * 功能：用于加密输入的数据
     *
     * @param inputData 输入的数据
     * @return 加密后的数据
     */
    public native byte[] encryptQuantumKeys(byte[] inputData);

    /**
     * Q盾解密数据接口：DecryptQuantumKeys
     * 功能：用于解密输入的数据
     *
     * @param inEncryptedData 输入的加密数据
     * @return 解密密钥信息，排列方式为KeyID+Key,KeyID+Key…(由外部解密)
     */
    public native byte[] decryptQuantumKeys(byte[] inEncryptedData);

    /**
     * Q盾更新RootKey接口：UpdateRootKey
     * 功能：用于更新RootKey，但是只有在调用更新确认接口之后才能真正更新Q盾中的 RootKey
     *
     * @return 更新后的 rootKey
     */
    public native byte[] updateRootKey();

    /**
     * Q盾RootKey更新确认接口：ConfirmRootKey
     * 功能：用于加密输入的数据
     *
     * @param rootKey 要确认的 RootKey
     * @return 成功 true, 失败 false
     */
    public native boolean confirmRootKey(byte[] rootKey);

    public byte[] getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(byte[] deviceID) {
        this.deviceID = deviceID;
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

    public byte[] getStartAuthInfo() {
        return startAuthInfo;
    }

    public void setStartAuthInfo(byte[] startAuthInfo) {
        this.startAuthInfo = startAuthInfo;
    }
}
