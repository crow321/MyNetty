package test.process;

import cn.qtec.access.auth.client.algorithm.impl.AlgorithmImpl;
import cn.qtec.access.auth.client.constant.EncryptionAlgorithm;
import cn.qtec.access.auth.client.constant.MessageType;
import cn.qtec.access.auth.client.constant.StatusEnum;
import cn.qtec.access.auth.client.entity.AccessAuthInfo;
import cn.qtec.access.auth.client.message.AuthenticationMessage;
import cn.qtec.access.auth.client.message.TransportMessage;
import cn.qtec.access.auth.client.process.impl.ProcessImpl;
import cn.qtec.access.auth.client.security.impl.SecurityImpl;
import cn.qtec.access.auth.client.utils.HexUtil;
import cn.qtec.access.auth.client.utils.JniUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import test.BaseJunit4;

import static org.junit.Assert.*;

/**
 * @author Created by zhangp
 *         2017/11/16
 */
public class ProcessImplTest extends BaseJunit4 {
    private final int headerOffsetLength = 4;
    /* DEFAULT length*/
    private final int accessAuthBodyLength = 41;
    private final int accessAuthLength = accessAuthBodyLength + headerOffsetLength;
    /* AES length*/
    private final int accessAuthBodyLength_AES = 48;
    private final int encryptionKeyIdLength = 16;
    private final int accessAuthHeaderLength_AES = headerOffsetLength + encryptionKeyIdLength;
    private final int accessAuthLength_AES = accessAuthBodyLength_AES + accessAuthHeaderLength_AES;
    /* message length*/
    private final int messageLength = accessAuthLength + headerOffsetLength;
    private final int messageLength_AES = accessAuthLength_AES + headerOffsetLength;

    private ProcessImpl process;
    private AlgorithmImpl algorithm;
    private SecurityImpl security;
    private byte[] deviceID;
    private byte[] quantumKey;
    private byte[] rootKeyId;
    private byte[] rootKeyValue;
    private byte[] salt;
    private int iterationCount;

    @Override
    @Before
    public void setUp() throws Exception {
        process = new ProcessImpl();
        algorithm = new AlgorithmImpl();
        security = new SecurityImpl();

        /* 初始化认证消息 */
        deviceID = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        quantumKey = HexUtil.hexStringToBytes("0202020202020202020202020605040102020202020202020202020206050401");
        rootKeyId = HexUtil.hexStringToBytes("01010101010101010101010101010103");
        rootKeyValue = HexUtil.hexStringToBytes("0202020202020202020202020605040102020202020202020202020206050401");
        salt = rootKeyValue;
        iterationCount = 10;
    }

    @Test
    public void startAuthRequest() throws Exception {
        /* 本地认证 AES加密*/
        initAccessAuthInfo(new AccessAuthInfo(EncryptionAlgorithm.DEFAULT, null, null, deviceID, quantumKey));

        TransportMessage transportMessage = process.generateStartAuthRequest();
        System.out.println("body: " + HexUtil.bytesToHexString(transportMessage.getBody()));

        assertEquals("transportMessage version is 1", 1, transportMessage.getVersion());
        assertEquals("transportMessage encryptionAlgorithm is 0", 0, transportMessage.getEncryptionAlgorithm());
        assertEquals("transportMessage messageLength is 72", messageLength, transportMessage.getMessageLength());
        assertEquals("transportMessage messageType is 80", MessageType.ACCESS_AUTH_REQ, transportMessage.getMessageType());
        //非必须 requestID
        assertEquals("transportMessage requestID is 120", 120, transportMessage.getRequestID());
        assertEquals("transportMessage reserved is 0", 1, transportMessage.getStatus());

        assertNotNull("transportMessage body isn't NULL", transportMessage.getBody());
        assertEquals("transportMessage body length is equals to messageLength.", accessAuthLength, transportMessage.getBody().length);

        //assert auth message body
        byte[] authBody = transportMessage.getBody();
        assertEquals("authentication version is 1",
                1, authBody[0]);
        int offset = 1;
        assertEquals("authentication encryption algorithm is 0",
                EncryptionAlgorithm.DEFAULT, authBody[offset]);
        offset += 1;
        assertEquals("authentication data length is 41",
                accessAuthBodyLength, HexUtil.parseLittleEndianBytes(ArrayUtils.subarray(authBody, offset, offset + 2)));
        offset += 2;

        assertEquals("authentication deviceIDLength is 8",
                8, authBody[offset]);
        offset += 1;
        assertArrayEquals("authentication deviceID is {1,2,3,4,5,6,7,8}",
                deviceID, ArrayUtils.subarray(authBody, offset, offset + 8));

        offset += 8;

        assertEquals("authentication clientNonce is not NULL and length is 32",
                32, ArrayUtils.subarray(authBody, offset, authBody.length).length);
    }

    @Test
    public void startAuthRequest_AES() throws Exception {
        /* 本地认证 AES加密*/
        initAccessAuthInfo(new AccessAuthInfo(EncryptionAlgorithm.AES, rootKeyId, rootKeyValue, deviceID, quantumKey));

        TransportMessage transportMessage = process.generateStartAuthRequest();
        System.out.println("AES-body: " + HexUtil.bytesToHexString(transportMessage.getBody()));

        assertEquals("transportMessage version is 1", 1, transportMessage.getVersion());
        assertEquals("transportMessage encryptionAlgorithm is 0", 0, transportMessage.getEncryptionAlgorithm());
        assertEquals("transportMessage messageLength is 72", messageLength_AES, transportMessage.getMessageLength());
        assertEquals("transportMessage messageType is 80", MessageType.ACCESS_AUTH_REQ, transportMessage.getMessageType());
        //非必须 requestID
        assertEquals("transportMessage requestID is 120", 120, transportMessage.getRequestID());
        assertEquals("transportMessage reserved is 0", 1, transportMessage.getStatus());

        assertNotNull("transportMessage body isn't NULL", transportMessage.getBody());
        assertEquals("transportMessage body length is equals to messageLength.", accessAuthLength_AES, transportMessage.getBody().length);

        //assert auth message body
        byte[] authBody = transportMessage.getBody();

        assertEquals("authentication version is 1",
                1, authBody[0]);
        int offset = 1;

        assertEquals("authentication encryption algorithm is 0",
                EncryptionAlgorithm.AES, authBody[offset]);
        offset += 1;

        assertEquals("authentication data length is 41",
                accessAuthBodyLength_AES, HexUtil.parseLittleEndianBytes(ArrayUtils.subarray(authBody, offset, offset + 2)));
        offset += 2;

        assertArrayEquals("authentication encryptKeyId is {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,}",
                rootKeyId, ArrayUtils.subarray(authBody, offset, offset + 16));
        offset += 16;

        byte[] encryptedBody = ArrayUtils.subarray(authBody, offset, authBody.length);
        byte[] decryptedBody = security.decrypt(encryptedBody, rootKeyValue);

        offset = 0;
        assertEquals("authentication deviceIDLength is 8",
                8, decryptedBody[offset]);
        offset += 1;
        assertArrayEquals("authentication deviceID is {1,2,3,4,5,6,7,8}",
                deviceID, ArrayUtils.subarray(decryptedBody, offset, offset + 8));

        offset += 8;

        assertEquals("authentication clientNonce is not NULL and length is 32",
                32, ArrayUtils.subarray(decryptedBody, offset, authBody.length).length);
    }

    @Test
    public void startAuthRequest_ErrorArguments_AES() throws Exception {
        /*本地使用AES时 EncryptionAlgorithm传错值 正确值为 0 或 1*/
        initAccessAuthInfo(new AccessAuthInfo(7, rootKeyId, rootKeyValue, deviceID, quantumKey));
        TransportMessage transportMessage = process.generateStartAuthRequest();
        assertNull(transportMessage);

        /*本地使用AES时 rootKeyValue传入为NULL*/
        initAccessAuthInfo(new AccessAuthInfo(EncryptionAlgorithm.AES, rootKeyId, null, deviceID, quantumKey));
        transportMessage = process.generateStartAuthRequest();
        assertNull(transportMessage);

        /*本地使用AES时 rootKeyId传入为NULL*/
        initAccessAuthInfo(new AccessAuthInfo(EncryptionAlgorithm.AES, null, rootKeyValue, deviceID, quantumKey));
        transportMessage = process.generateStartAuthRequest();
        assertNull(transportMessage);
    }

    @Test
    public void processAuthMessage_TestCase1() throws Exception {
        initAccessAuthInfo(new AccessAuthInfo(EncryptionAlgorithm.DEFAULT, null, null, deviceID, quantumKey));

        TransportMessage transportMessage = process.generateStartAuthRequest();
        byte[] startAuthMessageBody = transportMessage.getBody();
        byte[] clientNonce = ArrayUtils.subarray(startAuthMessageBody, startAuthMessageBody.length - 32, startAuthMessageBody.length);
        assertNotNull(clientNonce);
        System.out.println("clientNonce:" + HexUtil.bytesToHexString(clientNonce));

        /* simulate server first response */
        byte[] serverNonce = AlgorithmImpl.generateRandom();
        assertNotNull(serverNonce);
        int dataLength = 77;
        byte[] serverAuthResponseBody = generateServerResponse(dataLength, serverNonce, 0);

        /*计算服务端校验信息*/
        byte[] key = algorithm.getPbkdf2SHA256(quantumKey, salt, iterationCount);
        byte[] hashKey = algorithm.getSHA256(key);
        //append authInfo
        byte[] appendAuthInfo = new byte[deviceID.length + clientNonce.length + salt.length + 2 + serverNonce.length];

        System.arraycopy(deviceID, 0, appendAuthInfo, 0, deviceID.length);
        int offset = deviceID.length;

        System.arraycopy(clientNonce, 0, appendAuthInfo, offset, clientNonce.length);
        offset += clientNonce.length;
        System.arraycopy(salt, 0, appendAuthInfo, offset, salt.length);
        offset += salt.length;
        System.arraycopy(HexUtil.littleEndianAddShortToBytes(iterationCount), 0, appendAuthInfo, offset, 2);
        offset += 2;
        System.arraycopy(serverNonce, 0, appendAuthInfo, offset, serverNonce.length);

        byte[] hmac = algorithm.getHmacSHA256(hashKey, appendAuthInfo);

        //调用接口processAuthMessage()
        transportMessage = process.processAuthMessage(
                new TransportMessage(1,
                        EncryptionAlgorithm.DEFAULT,
                        dataLength + 4,
                        null,
                        MessageType.ACCESS_AUTH_RESP,
                        transportMessage.getRequestID() + 1,
                        StatusEnum.SUCCESS,
                        serverAuthResponseBody
                ));
        //assert
        this.assertTransportMessage(transportMessage, 122, MessageType.CLIENT_PROOF, messageLength, accessAuthLength);

        byte[] clientProofBody = transportMessage.getBody();
        //assert auth message body
        assertEquals("authentication version is 1",
                1, clientProofBody[0]);
        offset = 1;
        assertEquals("authentication encryption algorithm is 0",
                EncryptionAlgorithm.DEFAULT, clientProofBody[offset]);
        offset += 1;
        assertEquals("authentication data length is 41",
                accessAuthBodyLength, HexUtil.parseLittleEndianBytes(ArrayUtils.subarray(clientProofBody, offset, offset + 2)));
        offset += 2;

        assertEquals("authentication deviceIDLength is 8",
                8, clientProofBody[offset]);
        offset += 1;
        assertArrayEquals("authentication deviceID is {1,2,3,4,5,6,7,8}",
                deviceID, ArrayUtils.subarray(clientProofBody, offset, offset + 8));

        offset += 8;

        byte[] clientProof = ArrayUtils.subarray(clientProofBody, offset, clientProofBody.length);
        assertEquals("authentication clientProof is not NULL and length is 32",
                32, clientProof.length);

        byte[] key_ = algorithm.getXor(clientProof, hmac, hmac.length);

        System.out.println("appendAuthInfo:" + HexUtil.bytesToHexString(appendAuthInfo));
        System.out.println("key:" + HexUtil.bytesToHexString(key));
        assertArrayEquals(key, key_);
        byte[] serverProof = algorithm.getHmacSHA256(key, appendAuthInfo);

        // simulate server send proof message
        ByteBuf serverProofByteBuf = Unpooled.buffer();
        //version
        serverProofByteBuf.writeByte(1);
        //encryptionAlgorithm
        serverProofByteBuf.writeByte(EncryptionAlgorithm.DEFAULT);
        //bodyLength
        serverProofByteBuf.writeShort(accessAuthBodyLength);
        //deviceID length
        serverProofByteBuf.writeByte(deviceID.length);
        //deviceID
        serverProofByteBuf.writeBytes(deviceID);
        //serverProof
        serverProofByteBuf.writeBytes(serverProof);
        byte[] serverProofBody = new byte[serverProofByteBuf.readableBytes()];
        serverProofByteBuf.readBytes(serverProofBody);

        //调用接口processAuthMessage()
        transportMessage = process.processAuthMessage(
                new TransportMessage(1,
                        EncryptionAlgorithm.DEFAULT,
                        dataLength + 4,
                        null,
                        MessageType.SERVER_PROOF,
                        transportMessage.getRequestID() + 1,
                        StatusEnum.SUCCESS,
                        serverProofBody
                ));

        assertEquals("transportMessage length is 0", 4, transportMessage.getMessageLength());
        assertNotNull("sessionInfo is not Null", process.getSessionInfo());
        assertNotNull("sessionID is not Null", process.getSessionInfo().getSessionID());
        assertNotNull("sessionKey is not Null", process.getSessionInfo().getSessionKey());
    }

    @Test
    public void processAuthMessage_TestCase2_serverProofError() throws Exception {
        initAccessAuthInfo(new AccessAuthInfo(EncryptionAlgorithm.DEFAULT, null, null, deviceID, quantumKey));

        TransportMessage transportMessage = process.generateStartAuthRequest();
        byte[] startAuthMessageBody = transportMessage.getBody();
        byte[] clientNonce = ArrayUtils.subarray(startAuthMessageBody, startAuthMessageBody.length - 32, startAuthMessageBody.length);
        assertNotNull(clientNonce);
        System.out.println("clientNonce:" + HexUtil.bytesToHexString(clientNonce));

        /* simulate server first response */
        byte[] serverNonce = AlgorithmImpl.generateRandom();
        assertNotNull(serverNonce);
        int dataLength = 77;
        byte[] serverAuthResponseBody = generateServerResponse(dataLength, serverNonce, 0);

        /*计算服务端校验信息*/
        byte[] key = algorithm.getPbkdf2SHA256(quantumKey, salt, iterationCount);
        //append authInfo
        byte[] appendAuthInfo = appendAuthInfo(clientNonce, serverNonce);
        byte[] serverProofError = algorithm.getHmacSHA256(salt, appendAuthInfo);

        //调用接口processAuthMessage()
        transportMessage = process.processAuthMessage(
                new TransportMessage(1,
                        EncryptionAlgorithm.DEFAULT,
                        dataLength + 4,
                        null,
                        MessageType.ACCESS_AUTH_RESP,
                        transportMessage.getRequestID() + 1,
                        StatusEnum.SUCCESS,
                        serverAuthResponseBody
                ));

        //assert
        assertTransportMessage(transportMessage, 122, MessageType.CLIENT_PROOF, messageLength, accessAuthLength);

        byte[] clientProofBody = transportMessage.getBody();
        //assert auth message body
        assertEquals("authentication version is 1",
                1, clientProofBody[0]);
        int offset = 1;
        assertEquals("authentication encryption algorithm is 0",
                EncryptionAlgorithm.DEFAULT, clientProofBody[offset]);
        offset += 1;
        assertEquals("authentication data length is 41",
                accessAuthBodyLength, HexUtil.parseLittleEndianBytes(ArrayUtils.subarray(clientProofBody, offset, offset + 2)));
        offset += 2;

        assertEquals("authentication deviceIDLength is 8",
                8, clientProofBody[offset]);
        offset += 1;
        assertArrayEquals("authentication deviceID is {1,2,3,4,5,6,7,8}",
                deviceID, ArrayUtils.subarray(clientProofBody, offset, offset + 8));

        offset += 8;

        byte[] clientProof = ArrayUtils.subarray(clientProofBody, offset, clientProofBody.length);
        assertEquals("authentication clientProof is not NULL and length is 32",
                32, clientProof.length);

        // simulate server send proof message
        ByteBuf serverProofByteBuf = Unpooled.buffer();
        //version
        serverProofByteBuf.writeByte(1);
        //encryptionAlgorithm
        serverProofByteBuf.writeByte(EncryptionAlgorithm.DEFAULT);
        //bodyLength
        serverProofByteBuf.writeShort(accessAuthBodyLength);
        //deviceID length
        serverProofByteBuf.writeByte(deviceID.length);
        //deviceID
        serverProofByteBuf.writeBytes(deviceID);
        //serverProof
        serverProofByteBuf.writeBytes(serverProofError);
        byte[] serverProofBody = new byte[serverProofByteBuf.readableBytes()];
        serverProofByteBuf.readBytes(serverProofBody);

        //调用接口processAuthMessage()
        transportMessage = process.processAuthMessage(
                new TransportMessage(1,
                        EncryptionAlgorithm.DEFAULT,
                        dataLength + 4,
                        null,
                        MessageType.SERVER_PROOF,
                        transportMessage.getRequestID() + 1,
                        StatusEnum.SUCCESS,
                        serverProofBody
                ));

        assertNull("client verify server failed ", transportMessage);
    }

    @Test
    public void processAuthMessage_TestCase2_MessageTypeError() throws Exception {
        initAccessAuthInfo(new AccessAuthInfo(EncryptionAlgorithm.DEFAULT, null, null, deviceID, quantumKey));

        TransportMessage transportMessage = process.generateStartAuthRequest();
        byte[] startAuthMessageBody = transportMessage.getBody();
        byte[] clientNonce = ArrayUtils.subarray(startAuthMessageBody, startAuthMessageBody.length - 32, startAuthMessageBody.length);
        assertNotNull(clientNonce);
        System.out.println("clientNonce:" + HexUtil.bytesToHexString(clientNonce));

        /* simulate server first response */
        byte[] serverNonce = AlgorithmImpl.generateRandom();
        assertNotNull(serverNonce);
        int dataLength = 77;
        byte[] serverAuthResponseBody = generateServerResponse(dataLength, serverNonce, 0);

        //调用接口processAuthMessage()
        int errorMessageType = 110;
        transportMessage = process.processAuthMessage(
                new TransportMessage(1,
                        EncryptionAlgorithm.DEFAULT,
                        dataLength + 4,
                        null,
                        errorMessageType,
                        transportMessage.getRequestID() + 1,
                        StatusEnum.SUCCESS,
                        serverAuthResponseBody
                ));

        assertNull("message type error ", transportMessage);
    }

    @Test
    public void processAuthMessage_TestCase_AES() throws Exception {
        initAccessAuthInfo(new AccessAuthInfo(EncryptionAlgorithm.AES, rootKeyId, rootKeyValue, deviceID, quantumKey));

        TransportMessage transportMessage = process.generateStartAuthRequest();
        byte[] startAuthMessageBody = transportMessage.getBody();
        byte[] decryptedStartBody = security.decrypt(ArrayUtils.subarray(startAuthMessageBody, 20, startAuthMessageBody.length), rootKeyValue);
        assertNotNull(decryptedStartBody);

        byte[] clientNonce = ArrayUtils.subarray(decryptedStartBody, decryptedStartBody.length - 32, decryptedStartBody.length);
        assertNotNull(clientNonce);
        System.out.println("clientNonce:" + HexUtil.bytesToHexString(clientNonce));

        /* simulate server first response */
        byte[] serverNonce = AlgorithmImpl.generateRandom();
        assertNotNull(serverNonce);
        int dataLength = 77;
        byte[] serverAuthResponseBody = generateServerResponse(dataLength, serverNonce, EncryptionAlgorithm.AES);

        /*计算服务端校验信息*/
        byte[] key = algorithm.getPbkdf2SHA256(quantumKey, salt, iterationCount);
        //append authInfo
        byte[] appendAuthInfo = appendAuthInfo(clientNonce, serverNonce);

        //调用接口processAuthMessage()
        transportMessage = process.processAuthMessage(
                new TransportMessage(1,
                        EncryptionAlgorithm.DEFAULT,
                        dataLength + 4,
                        null,
                        MessageType.ACCESS_AUTH_RESP,
                        transportMessage.getRequestID() + 1,
                        StatusEnum.SUCCESS,
                        serverAuthResponseBody
                ));
        //assert
        this.assertTransportMessage(transportMessage, 122, MessageType.CLIENT_PROOF, messageLength_AES, accessAuthLength_AES);

        byte[] clientProofBody = transportMessage.getBody();
        //assert auth message body
        assertEquals("authentication version is 1",
                1, clientProofBody[0]);
        int offset = 1;
        assertEquals("authentication encryption algorithm is 0",
                EncryptionAlgorithm.AES, clientProofBody[offset]);
        offset += 1;
        assertEquals("authentication data length is 48",
                accessAuthBodyLength_AES, HexUtil.parseLittleEndianBytes(ArrayUtils.subarray(clientProofBody, offset, offset + 2)));
        offset += 2;

        assertArrayEquals("authentication encryptionKeyId",
                rootKeyId, ArrayUtils.subarray(clientProofBody, offset, offset + 16));
        offset += 16;

        byte[] fixedBody = security.decrypt(ArrayUtils.subarray(clientProofBody, offset, clientProofBody.length), rootKeyValue);
        offset = 0;
        assertEquals("authentication deviceIDLength is 8",
                8, fixedBody[offset]);
        offset += 1;
        assertArrayEquals("authentication deviceID is {1,2,3,4,5,6,7,8}",
                deviceID, ArrayUtils.subarray(fixedBody, offset, offset + 8));

        offset += 8;

        byte[] clientProof = ArrayUtils.subarray(fixedBody, offset, fixedBody.length);
        assertEquals("authentication clientProof is not NULL and length is 32",
                32, clientProof.length);


        System.out.println("appendAuthInfo: \n" + HexUtil.bytesToHexString(appendAuthInfo));
        System.out.println("key:" + HexUtil.bytesToHexString(key));
        byte[] serverProof = algorithm.getHmacSHA256(key, appendAuthInfo);
        System.out.println("serverProof:" + HexUtil.bytesToHexString(serverProof));

        // simulate server send proof message
        ByteBuf serverProofByteBuf = Unpooled.buffer();
        //version
        serverProofByteBuf.writeByte(1);
        //encryptionAlgorithm
        serverProofByteBuf.writeByte(EncryptionAlgorithm.DEFAULT);
        //bodyLength
        serverProofByteBuf.writeShort(accessAuthBodyLength);
        //deviceID length
        serverProofByteBuf.writeByte(deviceID.length);
        //deviceID
        serverProofByteBuf.writeBytes(deviceID);
        //serverProof
        serverProofByteBuf.writeBytes(serverProof);
        byte[] serverProofBody = new byte[serverProofByteBuf.readableBytes()];
        serverProofByteBuf.readBytes(serverProofBody);

        //调用接口processAuthMessage()
        transportMessage = process.processAuthMessage(
                new TransportMessage(1,
                        EncryptionAlgorithm.DEFAULT,
                        dataLength + 4,
                        null,
                        MessageType.SERVER_PROOF,
                        transportMessage.getRequestID() + 1,
                        StatusEnum.SUCCESS,
                        serverProofBody
                ));

        assertEquals("transportMessage length is 0", 4, transportMessage.getMessageLength());
        assertNotNull("sessionInfo is not Null", process.getSessionInfo());
        assertNotNull("sessionID is not Null", process.getSessionInfo().getSessionID());
        assertNotNull("sessionKey is not Null", process.getSessionInfo().getSessionKey());
    }

    private void initAccessAuthInfo(AccessAuthInfo accessAuthInfo) {
        byte[] deviceID = accessAuthInfo.getDeviceID();
        byte[] quantumKey = accessAuthInfo.getQuantumKeyValue();

        byte[] rootKeyId = accessAuthInfo.getRootKeyId();
        byte[] rootKeyValue = accessAuthInfo.getRootKeyValue();
        int encryptionAlgorithm = accessAuthInfo.getEncryptionAlgorithm();

        AuthenticationMessage authenticationMessage = new AuthenticationMessage();

        //int length = deviceID.length + 32;
        switch (encryptionAlgorithm) {
            case EncryptionAlgorithm.AES:


                authenticationMessage.setEncryptionKeyID(rootKeyId);
                //length = length % 16 == 0 ? length : (length / 16 + 1) * 16;
                //authenticationMessage.setMessageLength(length);
                break;

            default:
                //authenticationMessage.setMessageLength(length);
                break;
        }

        authenticationMessage.setVersion(1);
        authenticationMessage.setEncryptionAlgorithm(encryptionAlgorithm);
        authenticationMessage.setDeviceIDLength(8);
        authenticationMessage.setDeviceID(deviceID);

        ProcessImpl.initAuthMessage(authenticationMessage, accessAuthInfo.getRootKeyValue(), quantumKey);
    }

    private byte[] generateServerResponse(int dataLength, byte[] serverNonce, int encryptionAlgorithm) throws Exception {
        /* simulate server first response */
        ByteBuf serverResponseByteBuf = Unpooled.buffer();
        //version
        serverResponseByteBuf.writeByte(1);
        //encryptionAlgorithm
        serverResponseByteBuf.writeByte(encryptionAlgorithm);
        //length
        serverResponseByteBuf.writeShort(dataLength);

        //body
        ByteBuf body = Unpooled.buffer();
        //deviceIdLength
        body.writeByte(8);
        //deviceID
        body.writeBytes(deviceID);
        //salt
        body.writeBytes(salt);
        //iterationCount
        body.writeBytes(HexUtil.bigEndianAddShortToBytes(iterationCount));
        //reserved 2bytes
        body.writeShort(0);
        //serverNonce
        body.writeBytes(serverNonce);
        byte[] bodyBytes = new byte[body.readableBytes()];
        body.readBytes(bodyBytes);

        byte[] fixedBody;
        if (encryptionAlgorithm == EncryptionAlgorithm.AES) {
            serverResponseByteBuf.writeBytes(rootKeyId);
            fixedBody = security.encrypt(bodyBytes, rootKeyValue);
        } else {
            fixedBody = bodyBytes;
        }

        serverResponseByteBuf.writeBytes(fixedBody);
        byte[] serverAuthResponseBody = new byte[serverResponseByteBuf.readableBytes()];
        serverResponseByteBuf.readBytes(serverAuthResponseBody);
        System.out.println("simulate server response message \n" + HexUtil.bytesToHexString(serverAuthResponseBody));
        return serverAuthResponseBody;
    }

    private byte[] appendAuthInfo(byte[] clientNonce, byte[] serverNonce) {
        byte[] appendAuthInfo = new byte[deviceID.length + clientNonce.length + salt.length + 2 + serverNonce.length];

        System.arraycopy(deviceID, 0, appendAuthInfo, 0, deviceID.length);
        int offset = deviceID.length;
        System.arraycopy(clientNonce, 0, appendAuthInfo, offset, clientNonce.length);
        offset += clientNonce.length;
        System.arraycopy(salt, 0, appendAuthInfo, offset, salt.length);
        offset += salt.length;
        System.arraycopy(HexUtil.littleEndianAddShortToBytes(iterationCount), 0, appendAuthInfo, offset, 2);
        offset += 2;
        System.arraycopy(serverNonce, 0, appendAuthInfo, offset, serverNonce.length);

        return appendAuthInfo;
    }

    private void assertTransportMessage(TransportMessage transportMessage, int requestID, int messageType, int messageLength, int authDataLength) {
        assertEquals("transportMessage version is 1", 1, transportMessage.getVersion());
        assertEquals("transportMessage encryptionAlgorithm is 0", 0, transportMessage.getEncryptionAlgorithm());
        assertEquals("transportMessage messageLength is 72", messageLength, transportMessage.getMessageLength());
        assertEquals("transportMessage messageType is 82", messageType, transportMessage.getMessageType());
        //非必须 requestID
        assertEquals("transportMessage requestID is ", requestID, transportMessage.getRequestID());
        assertEquals("transportMessage reserved is 0", 1, transportMessage.getStatus());

        assertNotNull("transportMessage body isn't NULL", transportMessage.getBody());
        assertEquals("transportMessage body length is equals to messageLength.", authDataLength, transportMessage.getBody().length);

    }
}