package cn.qtec.access.auth.client.utils;

import cn.qtec.access.auth.client.constant.EncryptionAlgorithm;
import cn.qtec.access.auth.client.constant.MessageType;
import cn.qtec.access.auth.client.message.BaseMessage;
import cn.qtec.access.auth.client.message.TransportMessage;
import cn.qtec.access.auth.client.message.AuthenticationMessage;
import cn.qtec.access.auth.client.security.impl.SecurityImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhangp
 * @date 2017/10/9
 */
public class PacketUtil {
    private static final Logger logger = LoggerFactory.getLogger(PacketUtil.class);

    private static final int HEADER_OFFSET = 0x04;
    private static final int ENCRYPTION_KEY_ID_LENGTH = 0x10;
    private static final int NONCE_LENGTH = 0x20;

    private static SecurityImpl security = new SecurityImpl();
    private static byte[] rootKeyValue;

    public static byte[] wrapAuthMessage(AuthenticationMessage authMessage, int messageType, byte[] initRootKeyValue) {
        //append transportMessage body
        int deviceLength = authMessage.getDeviceIDLength();
        byte[] messageBody = new byte[1 + deviceLength + NONCE_LENGTH];
        //deviceLength  1字节
        messageBody[0] = (byte) deviceLength;
        int offset = 1;
        //deviceID      deviceLength字节
        System.arraycopy(authMessage.getDeviceID(), 0, messageBody, offset, deviceLength);
        offset += deviceLength;

        switch (messageType) {
            case MessageType.ACCESS_AUTH_REQ:
                //clientNonce   32字节
                System.arraycopy(authMessage.getClientNonce(), 0, messageBody, offset, NONCE_LENGTH);
                break;

            case MessageType.CLIENT_PROOF:
                //clientProof   32字节
                System.arraycopy(authMessage.getClientProof(), 0, messageBody, offset, NONCE_LENGTH);
                break;

            default:
                logger.error("The transportMessage type is ERROR, error:{}", messageType);
                return null;
        }

        ByteBuf result;
        ByteBuf header;
        switch (authMessage.getEncryptionAlgorithm()) {
            case EncryptionAlgorithm.DEFAULT:
                //消息体长度
                authMessage.setMessageLength(messageBody.length);
                result = Unpooled.buffer(authMessage.getMessageLength() + HEADER_OFFSET);
                header = wrapMessageHeader(authMessage);
                if (header == null) {
                    logger.error("Wrap transportMessage header Failed!");
                    return null;
                }
                result.writeBytes(header);
                result.writeBytes(messageBody);
                break;

            case EncryptionAlgorithm.AES:
                try {
                    rootKeyValue = initRootKeyValue;
                    byte[] encryptedBody = security.encrypt(messageBody, initRootKeyValue);
                    authMessage.setMessageLength(encryptedBody.length);
                    result = Unpooled.buffer(authMessage.getMessageLength() + HEADER_OFFSET);
                    header = wrapMessageHeader(authMessage);
                    if (header == null) {
                        logger.error("Wrap transportMessage header Failed!");
                        return null;
                    }
                    result.writeBytes(header);
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

        byte[] res = new byte[result.readableBytes()];
        result.readBytes(res);
        return res;
    }

    private static ByteBuf wrapMessageHeader(BaseMessage messageHeader) {
        ByteBuf result;
        switch (messageHeader.getEncryptionAlgorithm()) {
            case EncryptionAlgorithm.DEFAULT:
                result = Unpooled.buffer(HEADER_OFFSET);
                //版本号   1字节
                result.writeByte(messageHeader.getVersion());
                //加密类型 1字节
                result.writeByte(messageHeader.getEncryptionAlgorithm());
                //报文长度 2字节
                result.writeShort(messageHeader.getMessageLength());
                break;

            case EncryptionAlgorithm.AES:
                result = Unpooled.buffer(HEADER_OFFSET + ENCRYPTION_KEY_ID_LENGTH);
                //版本号   1字节
                result.writeByte(messageHeader.getVersion());
                //加密类型 1字节
                result.writeByte(messageHeader.getEncryptionAlgorithm());
                //报文长度 2字节
                result.writeShort(messageHeader.getMessageLength());
                //加密keyID   16字节
                result.writeBytes(messageHeader.getEncryptionKeyID());
                break;

            default:
                logger.error("The encryption algorithm ERROR, error:{}", messageHeader.getEncryptionAlgorithm());
                return null;
        }

        return result;
    }

    public static ByteBuf wrapMessage(TransportMessage transportMessage) {
        if (transportMessage != null) {
            ByteBuf result = Unpooled.buffer(transportMessage.getMessageLength() + HEADER_OFFSET);

            //消息头
            ByteBuf header = wrapMessageHeader(transportMessage);
            if (header == null) {
                logger.error("Wrap transportMessage header Failed!");
                return null;
            }
            result.writeBytes(header);

            //transportMessage type  1字节
            result.writeByte(transportMessage.getMessageType());
            //request ID    2字节
            result.writeShort(transportMessage.getRequestID());
            //status        1字节
            result.writeByte(transportMessage.getStatus());

            byte[] messageBody = transportMessage.getBody();
            switch (transportMessage.getEncryptionAlgorithm()) {
                case EncryptionAlgorithm.DEFAULT:
                    result.writeBytes(messageBody);
                    break;

                case EncryptionAlgorithm.AES:
                    try {
                        byte[] encryptedBody = security.encrypt(messageBody, rootKeyValue);
                        result.writeBytes(encryptedBody);
                    } catch (Exception e) {
                        logger.error("Encrypt authMessage ERROR, e:{}", e.getMessage());
                        return null;
                    }
                    break;

                default:
                    logger.error("The encryption algorithm ERROR, error:{}", transportMessage.getEncryptionAlgorithm());
                    return null;
            }

            return result;
        }

        logger.error("Received Message is NULL!");
        return null;

    }

    public static TransportMessage unwrapBinaryToMessage(ByteBuf response) {
        if (response.readableBytes() < HEADER_OFFSET + HEADER_OFFSET) {
            logger.error("Received binary transportMessage is too short!");
            return null;
        }
        int version;
        int encryptionAlgorithm;
        int messageLength;
        int messageType;
        int requestID;
        int status;
        byte[] encryptionKeyID;
        byte[] body;

        version = response.readByte();
        encryptionAlgorithm = response.readByte();
        messageLength = response.readUnsignedShort();

        //logger.info("Message version        : {}", version);
        //logger.info("encryptionAlgorithm    : {}", encryptionAlgorithm);
        //logger.info("messageLength          : {}", messageLength);

        switch (encryptionAlgorithm) {
            case EncryptionAlgorithm.DEFAULT:
                messageType = response.readUnsignedByte();
                requestID = response.readUnsignedShort();
                status = response.readByte();
                body = new byte[messageLength - 4];
                response.readBytes(body);
                encryptionKeyID = null;
                break;

            case EncryptionAlgorithm.AES:
                encryptionKeyID = new byte[ENCRYPTION_KEY_ID_LENGTH];
                response.readBytes(encryptionKeyID);

                messageType = response.readByte();
                requestID = response.readUnsignedShort();
                status = response.readByte();

                byte[] encryptedBody = new byte[messageLength - 20];
                response.readBytes(encryptedBody);

                try {
                    body = security.decrypt(encryptedBody, rootKeyValue);
                } catch (Exception e) {
                    logger.error("Decrypt data ERROR, e:{}", e.getMessage());
                    return null;
                }
                break;

            default:
                logger.error("The encryptionAlgorithm type is ERROR, error type is {}", encryptionAlgorithm);
                return null;
        }

        return new TransportMessage(version, encryptionAlgorithm, messageLength, encryptionKeyID, messageType, requestID, status, body);
    }

    public static AuthenticationMessage unwrapBinaryToAuthMessage(byte[] authResponse, int messageType) {
        int version = authResponse[0];
        if (version != 1) {
            logger.error("The authenticationMessage's version isn't 1, actual version:{}", version);
            return null;
        }
        int offset = 1;
        int encryptionAlgorithm = authResponse[offset];
        offset += 1;
        int messageLength = HexUtil.parseLittleEndianBytes(ArrayUtils.subarray(authResponse, offset, offset + 2));
        offset += 2;

        //logger.info("version            : {}", version);
        //logger.info("messageLength      : {}", messageLength);
        //logger.info("encryptionAlgorithm: {}", encryptionAlgorithm);

        byte[] encryptionKeyId;
        byte[] fixedBody;
        switch (encryptionAlgorithm) {
            case EncryptionAlgorithm.DEFAULT:
                encryptionKeyId = null;
                fixedBody = ArrayUtils.subarray(authResponse, offset, authResponse.length);
                break;

            case EncryptionAlgorithm.AES:
                encryptionKeyId = ArrayUtils.subarray(authResponse, offset, offset + ENCRYPTION_KEY_ID_LENGTH);
                offset += ENCRYPTION_KEY_ID_LENGTH;

                byte[] body = ArrayUtils.subarray(authResponse, offset, authResponse.length);
                try {
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
        switch (messageType) {
            case MessageType.ACCESS_AUTH_RESP:
                byte[] salt = ArrayUtils.subarray(fixedBody, offset, offset + NONCE_LENGTH);
                offset += NONCE_LENGTH;
                int iterationCount = HexUtil.bigEndianAddBytesToShort(ArrayUtils.subarray(fixedBody, offset, offset + 2));
                //reserved 2字节
                offset += 2 + 2;
                byte[] serverNonce = ArrayUtils.subarray(fixedBody, offset, fixedBody.length);
                return new AuthenticationMessage(version, encryptionAlgorithm, messageLength, encryptionKeyId, iterationCount, deviceIDLength, deviceID, salt, serverNonce);

            case MessageType.SERVER_PROOF:
                byte[] serverProof = ArrayUtils.subarray(fixedBody, offset, offset + NONCE_LENGTH);
                return new AuthenticationMessage(version, encryptionAlgorithm, messageLength, encryptionKeyId, deviceIDLength, deviceID, serverProof);

            default:
                logger.error("The transportMessage type is ERROR, error type is {}", messageType);
                return null;
        }
    }
}
