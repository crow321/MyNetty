package cn.qtec.qkcl.access.auth.client.utils;

import cn.qtec.qkcl.access.auth.client.constant.EncryptionAlgorithmEnum;
import cn.qtec.qkcl.access.auth.client.constant.StatusEnum;
import cn.qtec.qkcl.access.auth.client.message.AuthenticationMessage;
import cn.qtec.qkcl.access.auth.client.message.BaseMessage;
import cn.qtec.qkcl.access.auth.client.message.TransportMessage;
import cn.qtec.qkcl.access.auth.client.security.impl.SecurityImpl;
import cn.qtec.qkcl.message.enums.EMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangp
 * @date 2017/10/9
 */
public class PacketUtil {
    private static final Logger logger = LoggerFactory.getLogger(PacketUtil.class);

    private static final int HEADER_OR_CRC_LENGTH = 0x04;
    private static final int CRC_OFFSET = 0x05;
    private static final int ENCRYPTION_KEY_ID_LENGTH = 0x10;
    private static final int NONCE_LENGTH = 0x20;
    /**
     * key:     rootKeyID
     * value:   rootKeyValue
     */
    private static final Map<String, byte[]> ROOT_KEY_MAP = new ConcurrentHashMap<>();
    private static SecurityImpl security = new SecurityImpl();

    public static byte[] wrapAuthMessage(AuthenticationMessage authMessage) {
        byte[] deviceID = authMessage.getDeviceID();
        int deviceIDLength = deviceID.length;
        byte[] messageBody = new byte[1 + deviceIDLength + NONCE_LENGTH];
        messageBody[0] = (byte) deviceIDLength;
        int offset = 1;
        System.arraycopy(deviceID, 0, messageBody, offset, deviceIDLength);
        offset += deviceIDLength;

        int encryptionAlgorithm = authMessage.getEncryptionAlgorithm();
        int messageType = authMessage.getMessageType();
        logger.debug("Wrap message type {} and encryption mode {}", messageType, encryptionAlgorithm);
        if (messageType == EMessageType.ACCESS_AUTH_REQUEST.getValue()) {
            System.arraycopy(authMessage.getClientNonce(), 0, messageBody, offset, NONCE_LENGTH);

            if (encryptionAlgorithm == EncryptionAlgorithmEnum.ENCRYPTION_ALGORITHM_AES) {
                ROOT_KEY_MAP.put(HexUtil.bytesToHexString(authMessage.getEncryptionKeyID()), authMessage.getRootKeyValue());
            }
        } else if (messageType == EMessageType.ACCESS_AUTH_CLIENT_PROOF.getValue()) {
            System.arraycopy(authMessage.getClientProof(), 0, messageBody, offset, NONCE_LENGTH);
        } else {
            throw new IllegalArgumentException("message type is error, the type is " + messageType);
        }

        ByteBuf result;
        ByteBuf header;
        int dataLength;
        switch (encryptionAlgorithm) {
            case EncryptionAlgorithmEnum.ENCRYPTION_ALGORITHM_DEFAULT:
                authMessage.setMessageLength(messageBody.length);
                header = wrapMessageHeader(authMessage);
                if (header == null) {
                    logger.error("Wrap messageObject header Failed!");
                    return null;
                }
                dataLength = HEADER_OR_CRC_LENGTH + messageBody.length;
                result = Unpooled.buffer(CRC_OFFSET + dataLength + HEADER_OR_CRC_LENGTH);
                //message type  1字节
                result.writeByte(messageType);
                //requestID     2字节
                result.writeShort(authMessage.getRequestID());
                //status or reserved    1字节
                result.writeByte(authMessage.getStatus());
                //auth data length      1字节
                result.writeByte(dataLength);
                //header        4字节
                result.writeBytes(header);
                //auth message body     dataLength字节
                result.writeBytes(messageBody);
                break;

            case EncryptionAlgorithmEnum.ENCRYPTION_ALGORITHM_AES:
                try {
                    String rootKeyId = HexUtil.bytesToHexString(authMessage.getEncryptionKeyID());
                    byte[] encryptedBody = security.encrypt(messageBody, ROOT_KEY_MAP.get(rootKeyId));
                    authMessage.setMessageLength(encryptedBody.length);
                    header = wrapMessageHeader(authMessage);
                    if (header == null) {
                        logger.error("Wrap messageObject header Failed!");
                        return null;
                    }

                    dataLength = HEADER_OR_CRC_LENGTH + ENCRYPTION_KEY_ID_LENGTH + encryptedBody.length;
                    result = Unpooled.buffer(CRC_OFFSET + dataLength + HEADER_OR_CRC_LENGTH);
                    //message type  1字节
                    result.writeByte(messageType);
                    //requestID     2字节
                    result.writeShort(authMessage.getRequestID());
                    //status        1字节
                    result.writeByte(authMessage.getStatus());
                    //auth data length
                    result.writeByte(dataLength);
                    //auth header        1字节
                    result.writeBytes(header);
                    //auth message body
                    result.writeBytes(encryptedBody);
                } catch (Exception e) {
                    logger.error("Encrypt authMessage ERROR, e:{}", e.getMessage());
                    return null;
                }
                break;

            default:
                logger.error("The authMessage encryption algorithm ERROR, error:{}", authMessage.getEncryptionAlgorithm());
                return null;
        }
        offset = result.readableBytes();
        byte[] res = new byte[offset + HEADER_OR_CRC_LENGTH];
        result.readBytes(res, 0, offset);
        byte[] crcLittleEndianBytes = getCRCBigEndian(ArrayUtils.subarray(res, 0, offset));
        System.arraycopy(crcLittleEndianBytes, 0, res, offset, HEADER_OR_CRC_LENGTH);
        logger.debug("Send auth message, message type {}, deviceID {}, message body: {}", messageType, new String(deviceID), HexUtil.bytesToHexString(res));
        return res;
    }

    private static ByteBuf wrapMessageHeader(BaseMessage messageHeader) {
        ByteBuf result;
        switch (messageHeader.getEncryptionAlgorithm()) {
            case EncryptionAlgorithmEnum.ENCRYPTION_ALGORITHM_AES:
                result = Unpooled.buffer(HEADER_OR_CRC_LENGTH + ENCRYPTION_KEY_ID_LENGTH);
                //版本号   1字节
                result.writeByte(messageHeader.getVersion());
                //加密类型 1字节
                result.writeByte(messageHeader.getEncryptionAlgorithm());
                //报文长度 2字节
                result.writeShort(messageHeader.getMessageLength());
                //加密keyID   16字节
                result.writeBytes(messageHeader.getEncryptionKeyID());
                break;

            case EncryptionAlgorithmEnum.ENCRYPTION_ALGORITHM_DEFAULT:
            default:
                result = Unpooled.buffer(HEADER_OR_CRC_LENGTH);
                //版本号   1字节
                result.writeByte(messageHeader.getVersion());
                //加密类型 1字节
                result.writeByte(messageHeader.getEncryptionAlgorithm());
                //报文长度 2字节
                result.writeShort(messageHeader.getMessageLength());
                break;
        }

        return result;
    }

    public static AuthenticationMessage unwrapBinaryToAuthMessage(byte[] authResponse) {
        if (!checkMessage(authResponse)) {
            return null;
        }
        int offset = 0;
        int messageType = authResponse[0];
        offset++;
        int requestID = (int) HexUtil.parseBigEndianBytes(ArrayUtils.subarray(authResponse, offset, offset + 2));

        offset = CRC_OFFSET;
        int version = authResponse[offset];
        if (version != 1) {
            logger.error("The authenticationMessage's version isn't 1, actual version:{}", version);
            return null;
        }
        offset += 1;
        int encryptionAlgorithm = authResponse[offset];
        offset += 1;
        int messageLength = (int) HexUtil.parseLittleEndianBytes(ArrayUtils.subarray(authResponse, offset, offset + 2));
        offset += 2;

        byte[] encryptionKeyId;
        byte[] fixedBody;
        switch (encryptionAlgorithm) {
            case EncryptionAlgorithmEnum.ENCRYPTION_ALGORITHM_DEFAULT:
                encryptionKeyId = null;
                fixedBody = ArrayUtils.subarray(authResponse, offset, authResponse.length);
                break;

            case EncryptionAlgorithmEnum.ENCRYPTION_ALGORITHM_AES:
                encryptionKeyId = ArrayUtils.subarray(authResponse, offset, offset + ENCRYPTION_KEY_ID_LENGTH);
                offset += ENCRYPTION_KEY_ID_LENGTH;

                byte[] body = ArrayUtils.subarray(authResponse, offset, authResponse.length - HEADER_OR_CRC_LENGTH);
                try {
                    String rootKeyId = HexUtil.bytesToHexString(encryptionKeyId);
                    byte[] rootKeyValue = ROOT_KEY_MAP.get(rootKeyId);
                    fixedBody = security.decrypt(body, rootKeyValue);
                } catch (Exception e) {
                    logger.error("Decrypt data ERROR, e:{}", e.getMessage());
                    return null;
                }
                break;

            default:
                logger.error("The encryptionAlgorithm type is ERROR, error type is {}", encryptionAlgorithm);
                return null;
        }

        int deviceIDLength = fixedBody[0];
        offset = 1;
        byte[] deviceID = ArrayUtils.subarray(fixedBody, offset, offset + deviceIDLength);
        offset += deviceIDLength;

        //处理接入认证第二次收到的消息
        if (messageType == EMessageType.ACCESS_AUTH_RESPONSE.getValue()) {
            logger.info("The deviceID {} receives first response success.", HexUtil.bytesToHexString(deviceID));

            byte[] salt = ArrayUtils.subarray(fixedBody, offset, offset + NONCE_LENGTH);
            offset += NONCE_LENGTH;
            int iterationCount = (int) HexUtil.parseBigEndianBytes(ArrayUtils.subarray(fixedBody, offset, offset + 2));
            //reserved 2字节
            offset += 2 + 2;
            byte[] serverNonce = ArrayUtils.subarray(fixedBody, offset, fixedBody.length);
            return new AuthenticationMessage(messageType, requestID, StatusEnum.SUCCESS.getValue(), version, encryptionAlgorithm, messageLength, encryptionKeyId, iterationCount, deviceIDLength, deviceID, salt, serverNonce);
        } else if (messageType == EMessageType.ACCESS_AUTH_SERVER_PROOF.getValue()) {
            byte[] serverProof = ArrayUtils.subarray(fixedBody, offset, offset + NONCE_LENGTH);
            return new AuthenticationMessage(messageType, requestID, StatusEnum.SUCCESS.getValue(), version, encryptionAlgorithm, messageLength, encryptionKeyId, deviceIDLength, deviceID, serverProof);
        } else {
            throw new IllegalArgumentException("message type is error, the type is " + messageType);
        }
    }

    public static byte[] getCRCBigEndian(byte[] data) {
        return HexUtil.toBigEndianBytes(CRC32Util.getCRC32(data), HEADER_OR_CRC_LENGTH);
    }

    public static ByteBuf wrapTransportMessage(TransportMessage message) {
        ByteBuf byteBuf = wrapMessageHeader(message);
        byteBuf.writeBytes(message.getBody());
        return byteBuf;
    }

    public static boolean checkMessage(byte[] response) {
        if (response == null) {
            throw new NullPointerException("Receive null response data");
        }
        logger.debug("Receive auth message, message type {}, message body: {}", response[0], HexUtil.bytesToHexString(response));

        int offset = 0;
        byte messageType = response[offset];
        if (messageType != EMessageType.ACCESS_AUTH_RESPONSE.getValue() && messageType != EMessageType.ACCESS_AUTH_SERVER_PROOF.getValue()) {
            logger.error("Message type error, actual type is {}", messageType);
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
}
