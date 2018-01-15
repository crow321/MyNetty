package cn.qtec.qkcl.access.auth.server.process;

import cn.qtec.qkcl.access.auth.server.algorithm.IAlgorithm;
import cn.qtec.qkcl.access.auth.server.constant.MessageType;
import cn.qtec.qkcl.access.auth.server.constant.StatusEnum;
import cn.qtec.qkcl.access.auth.server.dao.impl.AuthenticationInfoDaoImpl;
import cn.qtec.qkcl.access.auth.server.entity.AuthRespInfo;
import cn.qtec.qkcl.access.auth.server.message.AuthenticationMessage;
import cn.qtec.qkcl.access.auth.server.security.CrypUtils;
import cn.qtec.qkcl.access.auth.server.utils.HexUtil;
import cn.qtec.qkcl.access.auth.server.utils.PacketUtil;
import cn.qtec.qkcl.message.enums.EMessageType;
import cn.qtec.qkcl.message.process.AbstractAuthServerMessageProcesser;
import cn.qtec.qkcl.message.process.cache.CacheData;
import cn.qtec.qkcl.message.vo.AuthencationInfo;
import cn.qtec.qkcl.message.vo.MessageObject;
import cn.qtec.qkcl.message.vo.SessionInfo;
import io.netty.channel.Channel;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangp
 * @date 2017/10/12
 */
@Service
public class AuthenticationProcessor extends AbstractAuthServerMessageProcesser {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationProcessor.class);

    private static final int RANDOM_LENGTH = 32;
    private static final int ITERATION_COUNT_OR_RESERVE_LENGTH = 2;
    private static final Map<String, byte[]> AUTH_ASSIST_MAP = new ConcurrentHashMap<>();
    @Autowired
    private AuthenticationInfoDaoImpl authenticationInfoDao;
    @Autowired
    private IAlgorithm algorithm;
    @Autowired
    private PacketUtil packetUtil;

    public AuthenticationProcessor() {
        super(EMessageType.ACCESS_AUTH_REQUEST);
        registerMessageProcesser(EMessageType.ACCESS_AUTH_RESPONSE, this);
        registerMessageProcesser(EMessageType.ACCESS_AUTH_CLIENT_PROOF, this);
        registerMessageProcesser(EMessageType.ACCESS_AUTH_SERVER_PROOF, this);
    }

    @Override
    public synchronized AuthencationInfo processAuthencaition(MessageObject messageObject) {
        AuthenticationMessage recvAuthMessage = packetUtil.unwrapAuthMessage(messageObject.getMessage());
        AuthencationInfo result;
        int messageType = recvAuthMessage.getMessageType();
        switch (messageType) {
            case MessageType.ACCESS_AUTH_REQ:
                byte[] responseBody = generateAuthResponseMessage(recvAuthMessage);
                messageObject.setMessageType(EMessageType.ACCESS_AUTH_RESPONSE.getValue());
                messageObject.setMessage(responseBody);
                result = new AuthencationInfo(messageObject, null);
                break;

            case MessageType.CLIENT_PROOF:
            default:
                result = generateServerProofMessage(messageObject, recvAuthMessage);
                break;
        }
        return result;
    }

    private synchronized byte[] generateAuthResponseMessage(AuthenticationMessage recvAuthMessage) {
        if (!checkStatus(recvAuthMessage)) {
            recvAuthMessage.setMessageType(EMessageType.ACCESS_AUTH_RESPONSE.getValue());
            return packetUtil.wrapMessageStatus(recvAuthMessage);
        }

        String deviceID = HexUtil.bytesToHexString(recvAuthMessage.getDeviceID());
        isExistSession(deviceID);
        AuthRespInfo authRespInfo = authenticationInfoDao.getAuthRespInfoByDeviceID(deviceID);
        if (authRespInfo == null) {
            logger.error("Query authRespInfo from DB but is NULL, deviceID:{}", deviceID);
            recvAuthMessage.setStatus(StatusEnum.FAILURE.getValue());
            recvAuthMessage.setMessageType(EMessageType.ACCESS_AUTH_RESPONSE.getValue());
            return packetUtil.wrapMessageStatus(recvAuthMessage);
        }

        byte[] decSalt = CrypUtils.passCryp(authRespInfo.getSalt());
        String serverNonce = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        recvAuthMessage.setSalt(decSalt);
        recvAuthMessage.setServerNonce(HexUtil.hexStringToBytes(serverNonce));
        recvAuthMessage.setIterationCount(authRespInfo.getIterationCount());
        recvAuthMessage.setMessageType(MessageType.ACCESS_AUTH_RESP);
        recvAuthMessage.setStatus(StatusEnum.SUCCESS.getValue());
        appendAuthInfo(recvAuthMessage, deviceID);

        int deviceIDLength = recvAuthMessage.getDeviceIDLength();
        int serverChallengeDataLength = 69;
        byte[] responseBody = new byte[serverChallengeDataLength + deviceIDLength];
        responseBody[0] = (byte) deviceIDLength;
        int offset = 1;
        System.arraycopy(recvAuthMessage.getDeviceID(), 0, responseBody, offset, deviceIDLength);
        offset += deviceIDLength;
        System.arraycopy(recvAuthMessage.getSalt(), 0, responseBody, offset, RANDOM_LENGTH);
        offset += RANDOM_LENGTH;
        System.arraycopy(HexUtil.bigEndianAddShortToBytes(recvAuthMessage.getIterationCount()), 0, responseBody, offset, ITERATION_COUNT_OR_RESERVE_LENGTH);
        //保留位2字节
        offset += ITERATION_COUNT_OR_RESERVE_LENGTH + ITERATION_COUNT_OR_RESERVE_LENGTH;
        //Server Nonce
        System.arraycopy(recvAuthMessage.getServerNonce(), 0, responseBody, offset, RANDOM_LENGTH);

        return packetUtil.wrapAuthMessage(recvAuthMessage, responseBody);
    }

    private AuthencationInfo generateServerProofMessage(MessageObject messageObject, AuthenticationMessage authMessage) {
        if (!checkStatus(authMessage)) {
            authMessage.setMessageType(EMessageType.ACCESS_AUTH_SERVER_PROOF.getValue());
            messageObject.setMessage(packetUtil.wrapMessageStatus(authMessage));
            return new AuthencationInfo(messageObject, null);
        }

        //获取deviceID
        String deviceID = HexUtil.bytesToHexString(authMessage.getDeviceID());

        byte[] clientProof = authMessage.getClientProof();
        SessionInfo sessionInfo = checkClientAuthentication(clientProof, deviceID);
        if (sessionInfo != null) {
            byte[] serverProof = sessionInfo.getSessionKey();
            int deviceIDLength = authMessage.getDeviceIDLength();
            byte[] serverProofMessage = new byte[deviceIDLength + 33];
            serverProofMessage[0] = (byte) deviceIDLength;
            int offset = 1;
            System.arraycopy(authMessage.getDeviceID(), 0, serverProofMessage, offset, deviceIDLength);
            offset += deviceIDLength;
            System.arraycopy(serverProof, 0, serverProofMessage, offset, serverProof.length);

            authMessage.setMessageType(MessageType.SERVER_PROOF);
            byte[] serverProofResponse = packetUtil.wrapAuthMessage(authMessage, serverProofMessage);

            messageObject.setMessageType(EMessageType.ACCESS_AUTH_SERVER_PROOF.getValue());
            messageObject.setSessionId(HexUtil.hexStringToBytes(sessionInfo.getSessionId()));
            messageObject.setMessage(serverProofResponse);
        } else {
            authMessage.setStatus(StatusEnum.FAILURE.getValue());
            authMessage.setMessageType(EMessageType.ACCESS_AUTH_SERVER_PROOF.getValue());
            messageObject.setMessage(packetUtil.wrapMessageStatus(authMessage));
        }

        AUTH_ASSIST_MAP.remove(deviceID);
        return new AuthencationInfo(messageObject, sessionInfo);
    }

    private SessionInfo checkClientAuthentication(byte[] clientProof, String deviceID) {
        byte[] authInfo = AUTH_ASSIST_MAP.get(deviceID);
        if (authInfo == null) {
            logger.error("Can't find any info from AUTH_ASSIST_MAP by deviceID: {}", deviceID);
            return null;
        }

        byte[] password = authenticationInfoDao.queryPasswordByDeviceID(deviceID);
        if (password == null) {
            logger.error("Can't find password in DB by deviceID: {}", deviceID);
            return null;
        }
        password = CrypUtils.passCryp(password);
        byte[] hmac = algorithm.getHmacSHA256(password, authInfo);
        byte[] calculateKey = algorithm.getXor(clientProof, hmac, clientProof.length);
        byte[] calculatePassword = algorithm.getSHA256(calculateKey);

        boolean isEqual = Arrays.equals(calculatePassword, password);
        if (isEqual) {
            //计算sessionKey
            SessionInfo sessionInfo = generateSessionInfo(calculateKey, authInfo, deviceID.length() / 2);
            sessionInfo.setDeviceId(deviceID);
            logger.info("Server authenticates with client is SUCCESS! ");
            return sessionInfo;
        } else {
            logger.debug("append authInfo   : {}", HexUtil.bytesToHexString(authInfo));
            logger.debug("calculate password: {}", HexUtil.bytesToHexString(calculatePassword));
            logger.debug("password in DB    : {}", HexUtil.bytesToHexString(password));
            logger.error("Server authenticates with client is FAILURE!");
            return null;
        }
    }

    private void appendAuthInfo(AuthenticationMessage authMessage, String deviceID) {
        int deviceIDLength = authMessage.getDeviceIDLength();
        byte[] appendAuthInfo = new byte[deviceIDLength + ITERATION_COUNT_OR_RESERVE_LENGTH + RANDOM_LENGTH * 3];
        System.arraycopy(authMessage.getDeviceID(), 0, appendAuthInfo, 0, deviceIDLength);

        int offset = deviceIDLength;
        byte[] clientNonce = authMessage.getClientNonce();
        System.arraycopy(clientNonce, 0, appendAuthInfo, offset, RANDOM_LENGTH);

        offset += RANDOM_LENGTH;
        System.arraycopy(authMessage.getSalt(), 0, appendAuthInfo, offset, RANDOM_LENGTH);

        offset += RANDOM_LENGTH;
        byte[] iterationCountBytes = HexUtil.littleEndianToBytes(authMessage.getIterationCount());
        System.arraycopy(iterationCountBytes, 0, appendAuthInfo, offset, ITERATION_COUNT_OR_RESERVE_LENGTH);

        offset += ITERATION_COUNT_OR_RESERVE_LENGTH;
        byte[] serverNonce = authMessage.getServerNonce();
        System.arraycopy(serverNonce, 0, appendAuthInfo, offset, RANDOM_LENGTH);

        AUTH_ASSIST_MAP.put(deviceID, appendAuthInfo);
    }

    private SessionInfo generateSessionInfo(byte[] calculateKey, byte[] authInfo, int deviceIDLength) {
        byte[] clientNonce = ArrayUtils.subarray(authInfo, deviceIDLength, deviceIDLength + RANDOM_LENGTH);
        byte[] serverNonce = ArrayUtils.subarray(authInfo, authInfo.length - RANDOM_LENGTH, authInfo.length);
        byte[] sessionID = algorithm.getXor(clientNonce, serverNonce, RANDOM_LENGTH / 2);
        byte[] sessionKey = algorithm.getHmacSHA256(calculateKey, authInfo);
        return new SessionInfo(HexUtil.bytesToHexString(sessionID), sessionKey);
    }

    private void isExistSession(String deviceID) {
        Object obj = CacheData.getConnectionByDeviceId(deviceID.toLowerCase());
        if (obj != null) {
            Channel channel = (Channel) obj;
            logger.info("The client is updating session, deviceID is {}", deviceID);
            channel.close();
            CacheData.removeByConnection(obj);
        } else {
            obj = CacheData.getConnectionByDeviceId(deviceID.toUpperCase());
            if (obj != null) {
                Channel channel = (Channel) obj;
                logger.info("The client is updating session, deviceID is {}", deviceID);
                channel.close();
                CacheData.removeByConnection(obj);
            }
        }
    }

    private boolean checkStatus(AuthenticationMessage recvAuthMessage) {
        return recvAuthMessage.getStatus() == StatusEnum.SUCCESS.getValue();
    }
}
