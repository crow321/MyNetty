package cn.qtec.qkcl.access.auth.client.process.impl;

import cn.qtec.qkcl.access.auth.client.algorithm.impl.AlgorithmImpl;
import cn.qtec.qkcl.access.auth.client.constant.EncryptionAlgorithmEnum;
import cn.qtec.qkcl.access.auth.client.constant.StatusEnum;
import cn.qtec.qkcl.access.auth.client.entity.AccessAuthInfo;
import cn.qtec.qkcl.access.auth.client.entity.AuthAssistInfo;
import cn.qtec.qkcl.access.auth.client.entity.AuthClientSessionInfo;
import cn.qtec.qkcl.access.auth.client.message.AuthenticationMessage;
import cn.qtec.qkcl.access.auth.client.message.TransportMessage;
import cn.qtec.qkcl.access.auth.client.process.AbstractAuthClientProcessor;
import cn.qtec.qkcl.access.auth.client.utils.HexUtil;
import cn.qtec.qkcl.access.auth.client.utils.PacketUtil;
import cn.qtec.qkcl.message.enums.EMessageType;
import cn.qtec.qkcl.message.vo.AuthencationInfo;
import cn.qtec.qkcl.message.vo.MessageObject;
import cn.qtec.qkcl.message.vo.SessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by zhangp
 *         2017/12/2
 */
public class LocalAuthProcessor extends AbstractAuthClientProcessor {
    private static final Logger logger = LoggerFactory.getLogger(LocalAuthProcessor.class);

    private static final int RANDOM_BYTE_LENGTH = 0x20;
    /*deviceId minimum length and maximum length*/
    private static final int DEVICE_ID_MIN_LENGTH = 5;
    private static final int DEVICE_ID_MAX_LENGTH = 25;
    /*quantumKey minimum length and maximum length*/
    private static final int QUANTUM_KEY_MIN_LENGTH = 6;
    private static final int QUANTUM_KEY_MAX_LENGTH = 128;
    private static final int ROOT_KEY_ID_LENGTH = RANDOM_BYTE_LENGTH / 2;
    private static final int ROOT_KEY_VALUE_LENGTH = RANDOM_BYTE_LENGTH;

    private static final Map<String, AuthAssistInfo> AUTH_ASSIST_INFO_MAP = new ConcurrentHashMap<>(1);
    private AlgorithmImpl algorithm = new AlgorithmImpl();

    public LocalAuthProcessor() {
        super();
        registerMessageProcesser(EMessageType.ACCESS_AUTH_RESPONSE, this);
        registerMessageProcesser(EMessageType.ACCESS_AUTH_CLIENT_PROOF, this);
        registerMessageProcesser(EMessageType.ACCESS_AUTH_SERVER_PROOF, this);
    }

    @Override
    public TransportMessage generateStartAuthRequest(AccessAuthInfo initAuthInfo) {
        logger.info("The access authentication in Local init...");
        if (initAuthInfo == null) {
            throw new NullPointerException("client starts authentication need some AccessAuthInfo.");
        }

        byte[] deviceID = initAuthInfo.getDeviceID();
        byte[] quantumKey = initAuthInfo.getQuantumKeyValue();
        int encryptionAlgorithm = initAuthInfo.getEncryptionAlgorithm();
        byte[] rootKeyId = initAuthInfo.getRootKeyId();
        byte[] rootKeyValue = initAuthInfo.getRootKeyValue();

        /* 参数校验 */
        if (deviceID == null) {
            throw new NullPointerException("client authenticates need deviceId.");
        }
        if (deviceID.length < DEVICE_ID_MIN_LENGTH || deviceID.length > DEVICE_ID_MAX_LENGTH) {
            throw new IllegalArgumentException("the deviceID length should be >= 5 and <= 25");
        }
        if (quantumKey == null) {
            throw new NullPointerException("client authenticates need quantumKey.");
        }
        if (quantumKey.length < QUANTUM_KEY_MIN_LENGTH || quantumKey.length > QUANTUM_KEY_MAX_LENGTH) {
            throw new IllegalArgumentException("the quantumKey length should be >= 6 and <= 128");
        }

        AuthenticationMessage startAuthMessage = new AuthenticationMessage();
        switch (encryptionAlgorithm) {
            case EncryptionAlgorithmEnum.ENCRYPTION_ALGORITHM_AES:
                if (rootKeyId == null) {
                    throw new NullPointerException("client authenticates with AES need rootKeyId.");
                }
                if (rootKeyId.length != ROOT_KEY_ID_LENGTH) {
                    throw new IllegalArgumentException("the rootKeyId length should be 16");
                }
                if (rootKeyValue == null) {
                    throw new NullPointerException("client authenticates with AES need rootKeyValue.");
                }
                if (rootKeyValue.length != ROOT_KEY_VALUE_LENGTH) {
                    throw new IllegalArgumentException("the rootKeyValue length should be 32");
                }
                startAuthMessage.setEncryptionKeyID(rootKeyId);
                startAuthMessage.setRootKeyValue(rootKeyValue);
                break;

            default:
                break;
        }

        //初始化变量
        byte[] clientNonce = reset(quantumKey, deviceID);
        startAuthMessage.setClientNonce(clientNonce);
        startAuthMessage.setVersion(1);
        startAuthMessage.setEncryptionAlgorithm(encryptionAlgorithm);
        startAuthMessage.setDeviceIDLength(deviceID.length);
        startAuthMessage.setDeviceID(deviceID);
        startAuthMessage.setRequestID(120);
        startAuthMessage.setStatus(StatusEnum.SUCCESS.getValue());
        startAuthMessage.setMessageType(EMessageType.ACCESS_AUTH_REQUEST.getValue());

        byte[] startAuthMessageBody = PacketUtil.wrapAuthMessage(startAuthMessage);
        if (startAuthMessageBody == null) {
            logger.error("The client wrap StartAuthMessage failed!");
            return null;
        }

        return new TransportMessage(1, startAuthMessageBody, deviceID);
    }

    @Override
    public synchronized AuthencationInfo processAuthencaition(MessageObject message) {
        if (message == null) {
            throw new NullPointerException("processAuthencaition received MessageObject is null");
        }

        AuthenticationMessage recvAuthMessage = PacketUtil.unwrapBinaryToAuthMessage(message.getMessage());
        if (recvAuthMessage == null) {
            return new AuthencationInfo(null, null);
        }

        //打包返回的报文
        int messageType = recvAuthMessage.getMessageType();
        if (messageType == EMessageType.ACCESS_AUTH_RESPONSE.getValue()) {
            //计算clientProof
            calculateClientProof(recvAuthMessage);
            byte[] resBody = PacketUtil.wrapAuthMessage(recvAuthMessage);
            message.setMessageType(EMessageType.ACCESS_AUTH_CLIENT_PROOF.getValue());
            message.setMessage(resBody);
            return new AuthencationInfo(message, null);
        } else if (messageType == EMessageType.ACCESS_AUTH_SERVER_PROOF.getValue()) {
            SessionInfo sessionInfo = checkServerProof(recvAuthMessage.getDeviceID(), recvAuthMessage.getServerProof());
            return new AuthencationInfo(null, sessionInfo);
        } else {
            throw new IllegalArgumentException("message type is error, the type is " + messageType);
        }
    }

    @Override
    public AuthClientSessionInfo getSessionInfo(long timeout, byte[] deviceID) throws InterruptedException {
        String deviceIdString = HexUtil.bytesToHexString(deviceID);
        AuthAssistInfo assistInfo = AUTH_ASSIST_INFO_MAP.get(deviceIdString);
        CountDownLatch latch = assistInfo.getLatch();
        logger.debug("latch count is {}", latch.getCount());
        latch.await(timeout, TimeUnit.MILLISECONDS);

        //移除认证完成的信息
        AUTH_ASSIST_INFO_MAP.remove(deviceIdString);
        return assistInfo.isSuccess() ? assistInfo.getSessionInfo() : null;
    }

    private synchronized void calculateClientProof(AuthenticationMessage authMessage) {
        byte[] salt = authMessage.getSalt();
        int iterationCount = authMessage.getIterationCount();
        byte[] deviceID = authMessage.getDeviceID();
        byte[] serverNonce = authMessage.getServerNonce();

        AuthAssistInfo assistInfo = AUTH_ASSIST_INFO_MAP.get(HexUtil.bytesToHexString(deviceID));
        byte[] clientNonce = assistInfo.getClientNonce();
        byte[] quantumKey = assistInfo.getQuantumKey();

        //key
        byte[] key = algorithm.getPbkdf2SHA256(quantumKey, salt, iterationCount);
        //H(Key)
        byte[] hashedKey = algorithm.getSHA256(key);
        //append auth info
        byte[] authInfo = appendAuthInfo(deviceID, clientNonce, salt, iterationCount, serverNonce);
        //hmac
        byte[] hmac = algorithm.getHmacSHA256(hashedKey, authInfo);
        //clientProof
        byte[] clientProof = algorithm.getXor(key, hmac, hmac.length);

        //计算会话密钥
        generateSessionInfo(clientNonce, serverNonce, key, authInfo, deviceID);

        authMessage.setClientProof(clientProof);
        authMessage.setMessageType(EMessageType.ACCESS_AUTH_CLIENT_PROOF.getValue());
        authMessage.setStatus(StatusEnum.SUCCESS.getValue());
    }

    private byte[] appendAuthInfo(byte[] deviceID, byte[] clientNonce, byte[] salt, int iterationCount, byte[] serverNonce) {
        int deviceIDLength = deviceID.length;
        byte[] authInfo = new byte[deviceIDLength + RANDOM_BYTE_LENGTH * 3 + 2];

        System.arraycopy(deviceID, 0, authInfo, 0, deviceIDLength);

        int offset = deviceIDLength;
        System.arraycopy(clientNonce, 0, authInfo, offset, RANDOM_BYTE_LENGTH);

        offset += RANDOM_BYTE_LENGTH;
        System.arraycopy(salt, 0, authInfo, offset, RANDOM_BYTE_LENGTH);

        offset += RANDOM_BYTE_LENGTH;
        System.arraycopy(HexUtil.toLittleEndianBytes(iterationCount, 2), 0, authInfo, offset, 2);

        offset += 2;
        System.arraycopy(serverNonce, 0, authInfo, offset, RANDOM_BYTE_LENGTH);
        return authInfo;
    }

    private void generateSessionInfo(byte[] clientNonce, byte[] serverNonce, byte[] key, byte[] authInfo, byte[] deviceID) {
        byte[] sessionID32 = algorithm.getXor(clientNonce, serverNonce, RANDOM_BYTE_LENGTH);
        //sessionID 16 Bytes
        byte[] sessionID = new byte[RANDOM_BYTE_LENGTH / 2];
        System.arraycopy(sessionID32, 0, sessionID, 0, sessionID.length);

        //sessionID 32 Bytes
        byte[] sessionKey = algorithm.getHmacSHA256(key, authInfo);

        updateSessionInfo(new AuthClientSessionInfo(deviceID, sessionID, sessionKey));
    }

    private synchronized SessionInfo checkServerProof(byte[] deviceID, byte[] receivedServerProof) {
        String deviceIdString = HexUtil.bytesToHexString(deviceID);
        AuthAssistInfo assistInfo = AUTH_ASSIST_INFO_MAP.get(deviceIdString);
        AuthClientSessionInfo clientSessionInfo = assistInfo.getSessionInfo();

        if (Arrays.equals(receivedServerProof, clientSessionInfo.getSessionKey())) {
            logger.info("The client verify the current server success!");

            SessionInfo sessionInfo = new SessionInfo();
            sessionInfo.setDeviceId(deviceIdString);
            sessionInfo.setSessionId(HexUtil.bytesToHexString(clientSessionInfo.getSessionID()));
            sessionInfo.setSessionKey(clientSessionInfo.getSessionKey());

            assistInfo.setSuccess(true);
            assistInfo.getLatch().countDown();

            return sessionInfo;
        } else {
            logger.error("The client verify the current server failed!");
        }
        return null;
    }

    private byte[] reset(byte[] quantumKey, byte[] deviceID) {
        String deviceIDString = HexUtil.bytesToHexString(deviceID);
        byte[] clientNonce = algorithm.generateRandom(RANDOM_BYTE_LENGTH);

        synchronized (this) {
            AuthAssistInfo assistInfo = new AuthAssistInfo(deviceIDString, clientNonce, quantumKey);
            assistInfo.setLatch(new CountDownLatch(1));
            AUTH_ASSIST_INFO_MAP.put(deviceIDString, assistInfo);
        }

        return clientNonce;
    }

    private void updateSessionInfo(AuthClientSessionInfo sessionInfo) {
        String deviceId = HexUtil.bytesToHexString(sessionInfo.getUsername());
        AuthAssistInfo assistInfo = AUTH_ASSIST_INFO_MAP.get(deviceId);
        assistInfo.setSessionInfo(sessionInfo);
        synchronized (this) {
            AUTH_ASSIST_INFO_MAP.put(deviceId, assistInfo);
        }
    }
}
