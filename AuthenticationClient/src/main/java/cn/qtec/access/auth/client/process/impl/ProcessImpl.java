package cn.qtec.access.auth.client.process.impl;

import cn.qtec.access.auth.client.algorithm.impl.AlgorithmImpl;
import cn.qtec.access.auth.client.constant.EncryptionAlgorithm;
import cn.qtec.access.auth.client.constant.KeyGenerateType;
import cn.qtec.access.auth.client.constant.MessageType;
import cn.qtec.access.auth.client.constant.StatusEnum;
import cn.qtec.access.auth.client.entity.SessionInfo;
import cn.qtec.access.auth.client.jni.QKeyService;
import cn.qtec.access.auth.client.message.TransportMessage;
import cn.qtec.access.auth.client.message.AuthenticationMessage;
import cn.qtec.access.auth.client.process.ProcessInterface;
import cn.qtec.access.auth.client.utils.HexUtil;
import cn.qtec.access.auth.client.utils.JniUtil;
import cn.qtec.access.auth.client.utils.PacketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author zhangp
 * @date 2017/9/30
 */
public class ProcessImpl implements ProcessInterface {
    private static final Logger logger = LoggerFactory.getLogger(ProcessImpl.class);

    private static final int RANDOM_BYTE_LENGTH = 0x20;
    private static final int START_ACCESS_AUTH_REQUEST_ID = 0x78;

    private static byte[] rootKeyValue;
    private static byte[] quantumKeyValue;
    private static int keyGenerateType;
    private static AuthenticationMessage startAuthMessage;
    private AlgorithmImpl algorithm;
    private QKeyService qKeyService;
    private SessionInfo sessionInfo;

    public ProcessImpl() {
        algorithm = new AlgorithmImpl();
        qKeyService = new QKeyService();
    }

    public static void initAuthMessage(AuthenticationMessage initAuthMessage, byte[] initRootKeyValue, byte[] initQuantumKey) {
        startAuthMessage = initAuthMessage;
        startAuthMessage.setClientNonce(AlgorithmImpl.generateRandom());
        keyGenerateType = KeyGenerateType.LOCAL;
        rootKeyValue = initRootKeyValue;
        quantumKeyValue = initQuantumKey;
    }

    @Override
    public TransportMessage generateStartAuthRequest() {
        byte[] res;
        switch (keyGenerateType) {
            case KeyGenerateType.LOCAL:
                res = PacketUtil.wrapAuthMessage(startAuthMessage, MessageType.ACCESS_AUTH_REQ, rootKeyValue);
                break;

            case KeyGenerateType.Q_SHIELD:
                try {
                    //加载动态链接库
                    JniUtil.addLibraryDir(JniUtil.LIBRARY_PATH_WINDOW_x64);
                    res = qKeyService.genStartInfo();
                    break;
                } catch (Exception e) {
                    logger.error("JNI load java.library.path error! e:{}", e.getMessage());
                    return null;
                }

            default:
                logger.error("The keyGenerateType value is error, error value: {}", keyGenerateType);
                return null;
        }

        if (res != null) {
            TransportMessage transportMessage = new TransportMessage();
            transportMessage.setVersion(1);
            transportMessage.setEncryptionAlgorithm(EncryptionAlgorithm.DEFAULT);
            transportMessage.setMessageLength(res.length + 4);
            //设置RequestID 、messageType
            transportMessage.setMessageType(MessageType.ACCESS_AUTH_REQ);
            transportMessage.setRequestID(START_ACCESS_AUTH_REQUEST_ID);
            transportMessage.setStatus(StatusEnum.SUCCESS);
            transportMessage.setBody(res);

            return transportMessage;
        }
        logger.error("The client generates StartAuthMessage FAILED!");
        return null;
    }

    @Override
    public TransportMessage processAuthMessage(TransportMessage transportMessage) {
        //返回报文
        byte[] responseBody = null;
        switch (keyGenerateType) {
            case KeyGenerateType.LOCAL:
                responseBody = processAuthMessageByLocal(transportMessage);
                break;
            case KeyGenerateType.Q_SHIELD:
                responseBody = processAuthMessageByQShield(transportMessage);
                break;
            default:
                logger.error("AuthenticationClient keyGenerateType error, error type is {}", keyGenerateType);
                break;
        }

        if (responseBody != null) {
            transportMessage.setMessageType(MessageType.CLIENT_PROOF);
            transportMessage.setBody(responseBody);
            transportMessage.setStatus(StatusEnum.SUCCESS);
            transportMessage.setMessageLength(responseBody.length + 4);
            transportMessage.setRequestID(transportMessage.getRequestID() + 1);
        } else {
            return null;
        }
        return transportMessage;
    }

    @Override
    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    private byte[] processAuthMessageByLocal(TransportMessage transportMessage) {
        AuthenticationMessage authenticationMessage = PacketUtil.unwrapBinaryToAuthMessage(transportMessage.getBody(), transportMessage.getMessageType());
        if (authenticationMessage != null) {
            //打包返回的报文
            switch (transportMessage.getMessageType()) {
                case MessageType.ACCESS_AUTH_RESP:
                    //计算clientProof
                    calculateClientProof(authenticationMessage);
                    return PacketUtil.wrapAuthMessage(authenticationMessage, MessageType.CLIENT_PROOF, rootKeyValue);

                case MessageType.SERVER_PROOF:
                    if (checkServerProof(authenticationMessage.getServerProof(), sessionInfo.getSessionKey())) {
                        logger.info("The client verify the current server SUCCESS!");
                        return new byte[0];
                    }
                    logger.error("The client verify the current server FAILED!");
                    break;

                default:
                    logger.error("The transportMessage type is ERROR, error type is {}", transportMessage.getMessageType());
                    break;
            }
        }
        return null;
    }

    private byte[] processAuthMessageByQShield(TransportMessage transportMessage) {
        byte[] qShieldResponse = null;
        int messageType = transportMessage.getMessageType();

        switch (messageType) {
            case MessageType.ACCESS_AUTH_RESP:
                qShieldResponse = qKeyService.genAuthInfo(transportMessage.getBody());
                break;

            case MessageType.SERVER_PROOF:
                SessionInfo sessionInfo = qKeyService.checkAuthInfo(transportMessage.getBody());
                if (sessionInfo != null) {
                    this.sessionInfo = sessionInfo;
                    logger.info("客户端接入认证成功！");
                    return new byte[0];
                }
                logger.warn("客户端接入认证失败！");
                break;

            default:
                logger.error("The transportMessage type is ERROR, error type is {}", messageType);
                break;
        }

        return qShieldResponse;
    }

    private void calculateClientProof(AuthenticationMessage authenticationMessage) {
        byte[] salt = authenticationMessage.getSalt();
        int iterationCount = authenticationMessage.getIterationCount();

        //key
        byte[] key = algorithm.getPbkdf2SHA256(quantumKeyValue, salt, iterationCount);

        //H(Key)
        byte[] hashedKey = algorithm.getSHA256(key);

        //hmac
        byte[] authInfo = appendAuthInfo(authenticationMessage);
        byte[] hmac = algorithm.getHmacSHA256(hashedKey, authInfo);
        //clientProof
        byte[] clientProof = algorithm.getXor(key, hmac, hmac.length);

        //计算会话密钥
        generateSessionInfo(authenticationMessage, key, authInfo);

        authenticationMessage.setClientProof(clientProof);
    }

    private byte[] appendAuthInfo(AuthenticationMessage qMessage) {
        int deviceIDLength = qMessage.getDeviceIDLength();
        byte[] authInfo = new byte[deviceIDLength + RANDOM_BYTE_LENGTH * 3 + 2];

        System.arraycopy(qMessage.getDeviceID(), 0, authInfo, 0, deviceIDLength);

        int offset = deviceIDLength;
        System.arraycopy(startAuthMessage.getClientNonce(), 0, authInfo, offset, RANDOM_BYTE_LENGTH);

        offset += RANDOM_BYTE_LENGTH;
        System.arraycopy(qMessage.getSalt(), 0, authInfo, offset, RANDOM_BYTE_LENGTH);

        offset += RANDOM_BYTE_LENGTH;
        byte[] iterationCountBytes = HexUtil.littleEndianAddShortToBytes(qMessage.getIterationCount());
        System.arraycopy(iterationCountBytes, 0, authInfo, offset, iterationCountBytes.length);

        offset += iterationCountBytes.length;
        System.arraycopy(qMessage.getServerNonce(), 0, authInfo, offset, RANDOM_BYTE_LENGTH);
        return authInfo;
    }

    private void generateSessionInfo(AuthenticationMessage authenticationMessage, byte[] key, byte[] authInfo) {
        byte[] sessionID32 = algorithm.getXor(startAuthMessage.getClientNonce(), authenticationMessage.getServerNonce(), RANDOM_BYTE_LENGTH);
        //sessionID 16 Bytes
        byte[] sessionID = new byte[RANDOM_BYTE_LENGTH / 2];
        System.arraycopy(sessionID32, 0, sessionID, 0, sessionID.length);

        //sessionID 32 Bytes
        byte[] sessionKey = algorithm.getHmacSHA256(key, authInfo);

        sessionInfo = new SessionInfo(sessionID, sessionKey);
    }

    private boolean checkServerProof(byte[] receivedServerProof, byte[] calculateServerProof) {
        return Arrays.equals(receivedServerProof, calculateServerProof);
    }
}
