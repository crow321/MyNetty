package cn.qtec.qkcl.access.auth.server.utils;

import cn.qtec.qkcl.access.auth.server.constant.MessageType;
import cn.qtec.qkcl.access.auth.server.constant.StatusEnum;
import cn.qtec.qkcl.access.auth.server.dao.impl.AuthenticationInfoDaoImpl;
import cn.qtec.qkcl.access.auth.server.message.AuthenticationMessage;
import cn.qtec.qkcl.access.auth.server.security.CrypUtils;
import cn.qtec.qkcl.access.auth.server.security.impl.SecurityImpl;
import cn.qtec.qkcl.message.enums.EMessageType;
import cn.qtec.qkcl.message.vo.MessageObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhangp
 * @date 2017/10/9
 */
@Component
public class PacketUtil {
    private static final Logger logger = LoggerFactory.getLogger(PacketUtil.class);

    private static final int DEFAULT_ENCRYPTION_KEY_ID_LENGTH = 0x10;
    private static final int CRC_OFFSET = 0x05;
    private static final int HEADER_OR_CRC_LENGTH = 0x04;

    @Autowired
    private SecurityImpl security;
    @Autowired
    private AuthenticationInfoDaoImpl authenticationInfoDao;

    public static MessageObject unwrapBinaryToMessageObject(ByteBuf byteBuf) {
        byte version = byteBuf.readByte();
        byte encryptionAlgorithm = byteBuf.readByte();
        int messageLength = byteBuf.readUnsignedShort();

        logger.debug("version(1)   :{}", version);
        logger.debug("encryption(0):{}", encryptionAlgorithm);
        if (messageLength != byteBuf.readableBytes()) {
            logger.error("The message length is ERROR, length value:{}, actual value:{}", messageLength, byteBuf.readableBytes());
            return null;
        }
        byte[] messageBody = new byte[messageLength];
        return new MessageObject(version, messageLength, messageBody);
    }

    public static ByteBuf wrapMessageObject(MessageObject messageObject) {
        ByteBuf res = Unpooled.buffer(messageObject.getLength() + 4);
        //version
        res.writeByte(messageObject.getVersion());
        //encryptionAlgorithm
        res.writeByte(messageObject.getEncryptionAlgorithm());
        //message length
        res.writeShort(messageObject.getMessage().length);
        //message body
        res.writeBytes(messageObject.getMessage());

        return res;
    }

    public static boolean checkMessage(byte[] response) {
        if (response == null) {
            throw new NullPointerException("Receive null response data");
        }
        int offset = 0;
        byte messageType = response[offset];
        if (messageType < EMessageType.ACCESS_AUTH_REQUEST.getValue() || messageType > EMessageType.ACCESS_AUTH_SERVER_PROOF.getValue()) {
            logger.error("Message type Error, actual type is {}", messageType);
            return false;
        }
        offset++;

        short requestID = (short) HexUtil.parseBigEndianBytes(ArrayUtils.subarray(response, offset, offset + 2));
        offset += 2;
        logger.debug("requestID: {}", requestID);

        byte status = response[offset];
        if (status != StatusEnum.SUCCESS.getValue()) {
            logger.error("Status error, status: {}", status);
            return false;
        }
        offset++;

        byte dataLength = response[offset];
        int actualDataLength = response.length - CRC_OFFSET - HEADER_OR_CRC_LENGTH;
        if (dataLength != actualDataLength) {
            logger.error("DataLength error, receive data length: {}, actual data length: {}", dataLength, actualDataLength);
            return false;
        }
        offset++;
        byte[] crcBytes = ArrayUtils.subarray(response, offset + dataLength, response.length);
        long receivedCrc = HexUtil.parseBigEndianBytes(crcBytes);
        long calculateCrc = CRC32Util.getCRC32(ArrayUtils.subarray(response, 0, response.length - 4));
        if (receivedCrc != calculateCrc) {
            logger.error("CRC error, receive crc: {}, calculate crc: {}", receivedCrc, calculateCrc);
            return false;
        }

        return true;
    }

    /**
     * 封装认证报文接口
     *
     * @param authenticationMessage
     * @param messageBody
     * @return
     */
    public byte[] wrapAuthMessage(AuthenticationMessage authenticationMessage, byte[] messageBody) {
        //fix server challenge data
        ByteBuf resultBuf = Unpooled.buffer();
        //messageType, requestID, status
        resultBuf.writeBytes(wrapMessageStatus(authenticationMessage));

        ByteBuf authDataBuf = Unpooled.buffer();
        //version 1字节
        authDataBuf.writeByte(authenticationMessage.getVersion());
        int algorithm = authenticationMessage.getEncryptionAlgorithm();
        //encryption algorithm 1字节
        authDataBuf.writeByte(algorithm);
        if (algorithm == 1) {
            byte[] encryptedKeyID = authenticationMessage.getEncryptionKeyID();
            byte[] encRootKey = authenticationInfoDao.queryRootKeyByRootKeyID(encryptedKeyID);
            if (encRootKey == null) {
                logger.error("query rootKey result is NULL by rootKeyID:{}", HexUtil.bytesToHexString(encryptedKeyID));
                return wrapMessageStatus(authenticationMessage.getMessageType() + 1, authenticationMessage.getRequestID(), StatusEnum.FAILURE.getValue());
            }
            byte[] rootKeyValue = CrypUtils.passCryp(encRootKey);
            byte[] fixedBody;
            try {
                fixedBody = security.encrypt(messageBody, rootKeyValue);
            } catch (Exception e) {
                logger.error("encrypt authentication body error, e:{}", e.getMessage());
                return wrapMessageStatus(authenticationMessage.getMessageType() + 1, authenticationMessage.getRequestID(), StatusEnum.FAILURE.getValue());
            }
            //message length
            authDataBuf.writeShort(fixedBody.length);
            //encryption KeyID 16字节
            authDataBuf.writeBytes(encryptedKeyID);
            authDataBuf.writeBytes(fixedBody);
        } else {
            authDataBuf.writeShort(messageBody.length);
            authDataBuf.writeBytes(messageBody);
        }

        int dataLength = authDataBuf.readableBytes();
        //auth data length  1字节
        resultBuf.writeByte(dataLength);
        //auth data body
        resultBuf.writeBytes(authDataBuf);
        int offset = resultBuf.readableBytes();
        byte[] response = new byte[offset + HEADER_OR_CRC_LENGTH];
        resultBuf.readBytes(response, 0, offset);
        //add crc
        byte[] crcData = ArrayUtils.subarray(response, 0, offset);
        byte[] crcBytes = getCRCBigEndian(crcData);
        System.arraycopy(crcBytes, 0, response, offset, HEADER_OR_CRC_LENGTH);
        logger.debug("Response authenticationMessage :\n{}", HexUtil.bytesToHexString(response));
        return response;
    }

    /**
     * 解析认证报文接口
     *
     * @param authRequest
     * @return
     */
    public AuthenticationMessage unwrapAuthMessage(byte[] authRequest) {
        logger.debug("Received auth message:\n{}", HexUtil.bytesToHexString(authRequest));
        //返回对象
        AuthenticationMessage result = new AuthenticationMessage();
        result.setStatus(StatusEnum.FAILURE.getValue());

        int offset = 0;
        int messageType = authRequest[offset];
        if (messageType != EMessageType.ACCESS_AUTH_REQUEST.getValue() && messageType != EMessageType.ACCESS_AUTH_CLIENT_PROOF.getValue()) {
            logger.error("The authentication message type error, actual type: {}", messageType);
            return result;
        }
        offset += 1;
        int requestID = (int) HexUtil.parseBigEndianBytes(ArrayUtils.subarray(authRequest, offset, offset + 2));
        offset += 2;
        int status = authRequest[offset];
        offset += 1;

        result.setMessageType(messageType);
        result.setRequestID(requestID);
        if (status != StatusEnum.SUCCESS.getValue()) {
            logger.error("The authentication message status error, actual status:{}", status);
            return result;
        }

        int dataLength = authRequest[offset];
        offset++;
        int actualDataLength = authRequest.length - offset - HEADER_OR_CRC_LENGTH;
        if (dataLength != actualDataLength) {
            logger.error("The authentication data length error, receive length: {}, actual length: {}", dataLength, actualDataLength);
            return result;
        }

        byte[] crcBytes = ArrayUtils.subarray(authRequest, authRequest.length - HEADER_OR_CRC_LENGTH, authRequest.length);
        long receivedCRC = HexUtil.parseBigEndianBytes(crcBytes);
        byte[] crcBody = ArrayUtils.subarray(authRequest, 0, authRequest.length - HEADER_OR_CRC_LENGTH);
        long calculateCRC = CRC32Util.getCRC32(crcBody);
        if (receivedCRC != calculateCRC) {
            logger.error("The authenticationMessage CRC error, receive CRC:{}, calculate CRC:{}", receivedCRC, calculateCRC);
            return result;
        }

        int version = authRequest[offset];
        if (version != 1) {
            logger.error("The authenticationMessage version isn't 1, error version:{}", version);
            return result;
        }

        offset += 1;
        int encryptionAlgorithm = authRequest[offset];
        offset += 1;
        int messageLength = (int) HexUtil.parseBigEndianBytes(ArrayUtils.subarray(authRequest, offset, offset + 2));
        offset += 2;
        int actualLength = authRequest.length - HEADER_OR_CRC_LENGTH * 2 - CRC_OFFSET;

        byte[] fixedBody;
        if (encryptionAlgorithm == 1) {
            actualLength -= DEFAULT_ENCRYPTION_KEY_ID_LENGTH;
            if (messageLength != actualLength) {
                logger.error("The authenticationMessage length is error, value:{}, actual:{}", messageLength, actualLength);
                return result;
            }
            byte[] encryptionKeyID = ArrayUtils.subarray(authRequest, offset, offset + DEFAULT_ENCRYPTION_KEY_ID_LENGTH);
            offset += DEFAULT_ENCRYPTION_KEY_ID_LENGTH;

            byte[] encRootKey = authenticationInfoDao.queryRootKeyByRootKeyID(encryptionKeyID);
            if (encRootKey == null) {
                logger.error("queryRootKeyByRootKeyID result is NULL, keyID:{}", HexUtil.bytesToHexString(encryptionKeyID));
                return result;
            }

            byte[] rootKeyValue = CrypUtils.passCryp(encRootKey);
            try {
                byte[] body = ArrayUtils.subarray(authRequest, offset, authRequest.length - HEADER_OR_CRC_LENGTH);
                System.out.println(HexUtil.bytesToHexString(body));
                fixedBody = security.decrypt(body, rootKeyValue);
            } catch (Exception e) {
                logger.error("decrypt authentication body error, e:{}", e.getMessage());
                return result;
            }

            result.setEncryptionKeyID(encryptionKeyID);
        } else {
            if (messageLength != actualLength) {
                logger.error("The authenticationMessage length is error, value:{}, actual:{}", messageLength, actualLength);
                return result;
            }
            fixedBody = ArrayUtils.subarray(authRequest, offset, authRequest.length - HEADER_OR_CRC_LENGTH);
        }

        int deviceIDLength = fixedBody[0];
        offset = 1;
        byte[] deviceID = ArrayUtils.subarray(fixedBody, offset, offset + deviceIDLength);
        offset += deviceIDLength;

        switch (messageType) {
            case MessageType.ACCESS_AUTH_REQ:
                byte[] clientNonce = ArrayUtils.subarray(fixedBody, offset, fixedBody.length);
                result.setClientNonce(clientNonce);
                break;

            case MessageType.CLIENT_PROOF:
                byte[] clientProof = ArrayUtils.subarray(fixedBody, offset, fixedBody.length);
                result.setClientProof(clientProof);
                break;

            default:
                logger.error("The authenticationMessage type is Error! error type is {}", messageType);
                return result;
        }

        result.setEncryptionAlgorithm(encryptionAlgorithm);
        result.setDeviceIDLength(deviceIDLength);
        result.setDeviceID(deviceID);
        result.setVersion(version);
        result.setStatus(status);

        logger.info("The deviceID {} is authentication.", HexUtil.bytesToHexString(deviceID));
        return result;
    }

    public byte[] wrapMessageStatus(AuthenticationMessage authenticationMessage) {
        return wrapMessageStatus(authenticationMessage.getMessageType(), authenticationMessage.getRequestID(), authenticationMessage.getStatus());
    }

    private byte[] wrapMessageStatus(int messageType, int requestID, int status) {
        byte[] statusBytes = new byte[4];
        //authenticationMessage type
        statusBytes[0] = (byte) messageType;
        //requestID 2字节
        System.arraycopy(HexUtil.bigEndianAddShortToBytes(requestID), 0, statusBytes, 1, 2);
        //status 1字节
        statusBytes[3] = (byte) status;
        return statusBytes;
    }

    private byte[] getCRCBigEndian(byte[] data) {
        return HexUtil.toBigEndianBytes(CRC32Util.getCRC32(data), HEADER_OR_CRC_LENGTH);
    }

}
