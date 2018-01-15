package cn.qtec.qkcl.access.auth.client.process.impl;

import cn.qtec.qkcl.access.auth.client.constant.StatusEnum;
import cn.qtec.qkcl.access.auth.client.entity.AccessAuthInfo;
import cn.qtec.qkcl.access.auth.client.entity.AuthClientSessionInfo;
import cn.qtec.qkcl.access.auth.client.jni.QKeyService;
import cn.qtec.qkcl.access.auth.client.message.TransportMessage;
import cn.qtec.qkcl.access.auth.client.process.AbstractAuthClientProcessor;
import cn.qtec.qkcl.access.auth.client.utils.HexUtil;
import cn.qtec.qkcl.access.auth.client.utils.JniUtil;
import cn.qtec.qkcl.access.auth.client.utils.PacketUtil;
import cn.qtec.qkcl.message.enums.EMessageType;
import cn.qtec.qkcl.message.vo.AuthencationInfo;
import cn.qtec.qkcl.message.vo.MessageObject;
import cn.qtec.qkcl.message.vo.SessionInfo;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by zhangp
 *         2017/12/2
 */
public class QShieldAuthProcessor extends AbstractAuthClientProcessor {
    private static final Logger logger = LoggerFactory.getLogger(QShieldAuthProcessor.class);

    private static final int CRC_LENGTH = 4;
    private static final int CRC_OFFSET = 5;

    private static final Map<String, AuthClientSessionInfo> SESSION_INFO_MAP = new ConcurrentHashMap<>(1);
    private static final Map<String, CountDownLatch> COUNT_DOWN_LATCH_MAP = new ConcurrentHashMap<>(1);

    public QShieldAuthProcessor() {
        super();
        registerMessageProcesser(EMessageType.ACCESS_AUTH_RESPONSE, this);
        registerMessageProcesser(EMessageType.ACCESS_AUTH_CLIENT_PROOF, this);
        registerMessageProcesser(EMessageType.ACCESS_AUTH_SERVER_PROOF, this);
    }

    @Override
    public TransportMessage generateStartAuthRequest(AccessAuthInfo initAuthInfo) {
        logger.info("The access authentication in QShield init...");
        //加载动态链接库
        JniUtil.loadJavaLibraryDir();

        QKeyService qKeyResult = QKeyService.getINSTANCE().genStartInfo();
        if (qKeyResult != null) {
            byte[] startAuthInfo = qKeyResult.getStartAuthInfo();
            int dataLength = startAuthInfo.length;

            byte[] res = new byte[startAuthInfo.length + CRC_LENGTH + CRC_OFFSET];
            int offset = 0;
            int messageType = EMessageType.ACCESS_AUTH_REQUEST.getValue();
            res[offset] = (byte) messageType;
            offset += 1;
            byte[] requestID = HexUtil.toBigEndianBytes(120, 2);
            System.arraycopy(requestID, 0, res, offset, 2);
            offset += 2;
            res[offset] = (byte) StatusEnum.SUCCESS.getValue();
            offset += 1;
            res[offset] = (byte) dataLength;
            offset++;
            System.arraycopy(startAuthInfo, 0, res, offset, dataLength);
            offset += dataLength;
            byte[] crcBytes = PacketUtil.getCRCBigEndian(ArrayUtils.subarray(res, 0, offset));
            System.arraycopy(crcBytes, 0, res, offset, CRC_LENGTH);

            //初始化认证消息
            reset(qKeyResult.getDeviceID());

            logger.debug("Send auth message, message type {}, message body: {}", messageType, HexUtil.bytesToHexString(res));
            //return new MessageObject((byte) 1, res.length, res);
            return new TransportMessage(1, res, qKeyResult.getDeviceID());
        }

        throw new NullPointerException("The QShield generates null.");
    }

    @Override
    public AuthencationInfo processAuthencaition(MessageObject messageObject) {
        if (messageObject == null) {
            throw new NullPointerException("MessageObject is null");
        }

        byte[] messageBody = messageObject.getMessage();
        if (PacketUtil.checkMessage(messageBody)) {
            int messageType = messageBody[0];

            byte[] requestID = ArrayUtils.subarray(messageBody, 1, 3);
            byte[] authMessage = ArrayUtils.subarray(messageBody, CRC_OFFSET, messageBody.length - CRC_LENGTH);
            if (messageType == EMessageType.ACCESS_AUTH_RESPONSE.getValue()) {
                byte[] qClientProofBody = QKeyService.getINSTANCE().genAuthInfo(authMessage);
                if (qClientProofBody != null) {
                    int dataLength = qClientProofBody.length;
                    byte[] res = new byte[dataLength + CRC_LENGTH + CRC_OFFSET];
                    int offset = 0;
                    res[offset] = (byte) EMessageType.ACCESS_AUTH_CLIENT_PROOF.getValue();
                    offset += 1;
                    System.arraycopy(requestID, 0, res, offset, 2);
                    offset += 2;
                    res[offset] = (byte) StatusEnum.SUCCESS.getValue();
                    offset += 1;
                    res[offset] = (byte) dataLength;
                    offset++;
                    System.arraycopy(qClientProofBody, 0, res, offset, dataLength);
                    offset += dataLength;
                    byte[] crcBytes = PacketUtil.getCRCBigEndian(ArrayUtils.subarray(res, 0, offset));
                    System.arraycopy(crcBytes, 0, res, offset, CRC_LENGTH);

                    messageObject.setMessageType(EMessageType.ACCESS_AUTH_CLIENT_PROOF.getValue());
                    messageObject.setMessage(res);

                    logger.debug("Response auth message, type {} message body {}", EMessageType.ACCESS_AUTH_CLIENT_PROOF.getValue(), HexUtil.bytesToHexString(res));
                    return new AuthencationInfo(messageObject, null);
                }
                logger.error("The client generate clientProof failed!");
            } else if (messageType == EMessageType.ACCESS_AUTH_SERVER_PROOF.getValue()) {
                QKeyService qKeySessionInfo = QKeyService.getINSTANCE().checkAuthInfo(authMessage);
                if (qKeySessionInfo != null) {
                    byte[] sessionID = qKeySessionInfo.getSessionID();
                    byte[] sessionKey = qKeySessionInfo.getSessionKey();
                    String deviceIdString = HexUtil.bytesToHexString(QKeyService.getINSTANCE().genStartInfo().getDeviceID());

                    //set auth client sessionInfo
                    AuthClientSessionInfo authClientSessionInfo = new AuthClientSessionInfo(HexUtil.hexStringToBytes(deviceIdString), sessionID, sessionKey);
                    SESSION_INFO_MAP.put(deviceIdString, authClientSessionInfo);

                    //set transport sessionInfo
                    SessionInfo sessionInfo = new SessionInfo(HexUtil.bytesToHexString(sessionID), sessionKey);
                    sessionInfo.setDeviceId(deviceIdString);
                    COUNT_DOWN_LATCH_MAP.get(deviceIdString).countDown();

                    logger.info("The client verify the current server success!");
                    return new AuthencationInfo(null, sessionInfo);
                }
                logger.error("The client verify the current server failed!");
            }
        }
        return new AuthencationInfo(null, null);
    }

    @Override
    public AuthClientSessionInfo getSessionInfo(long timeout, byte[] deviceID) throws InterruptedException {
        String deviceIDString = HexUtil.bytesToHexString(deviceID);
        COUNT_DOWN_LATCH_MAP.get(deviceIDString).await(timeout, TimeUnit.MILLISECONDS);
        AuthClientSessionInfo sessionInfo = SESSION_INFO_MAP.get(deviceIDString);
        return sessionInfo.getSessionID() != null ? sessionInfo : null;
    }

    private void reset(byte[] deviceId) {
        String deviceIDString = HexUtil.bytesToHexString(deviceId);
        SESSION_INFO_MAP.put(deviceIDString, new AuthClientSessionInfo(deviceId));
        COUNT_DOWN_LATCH_MAP.put(deviceIDString, new CountDownLatch(1));
    }
}
