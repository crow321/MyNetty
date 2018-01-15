package test.process;

import cn.qtec.qkcl.access.auth.server.algorithm.impl.AlgorithmImpl;
import cn.qtec.qkcl.access.auth.server.constant.EncryptionAlgorithm;
import cn.qtec.qkcl.access.auth.server.constant.StatusEnum;
import cn.qtec.qkcl.access.auth.server.process.AuthenticationProcessor;
import cn.qtec.qkcl.access.auth.server.security.impl.SecurityImpl;
import cn.qtec.qkcl.access.auth.server.utils.CRC32Util;
import cn.qtec.qkcl.access.auth.server.utils.HexUtil;
import cn.qtec.qkcl.message.enums.EMessageType;
import cn.qtec.qkcl.message.vo.MessageObject;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.BaseJunit4;

import static org.junit.Assert.*;


/**
 * @author Created by zhangp
 *         2017/11/17
 */
public class AuthenticationProcessorTest extends BaseJunit4 {
    @Autowired
    private AuthenticationProcessor process;
    @Autowired
    private AlgorithmImpl algorithm;
    @Autowired
    private SecurityImpl security;
    private String rootKey;
    private String rootKeyID;
    private String clientNonce;
    private byte[] deviceID;
    private byte[] quantumKey;
    private MessageObject messageObject;

    //报文组装
    private String type;
    private String authHeader;
    private String authDeviceID;
    private String authBodyString;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        deviceID = "Thread-1".getBytes();
        rootKeyID = "01010101aa0101c1ab01110101111101";
        rootKey = "F04EC04626C75E119B5A923C68D2A879D539F8739F41EE4120B1247209F8AC01";
        clientNonce = "11916A73F71FCC18B082CC6203708211C27657169E1820C151483DA02CE334A7";
        quantumKey = HexUtil.hexStringToBytes("e3879b3c98ce4a5386416c6a82afee0e7844656c7d8faa3c774455493067a101");

        type = "510078002D";
        authHeader = "01000029";
        authDeviceID = "08" + HexUtil.bytesToHexString(deviceID);
        authBodyString = type + authHeader + authDeviceID + clientNonce;

    }

    @Test
    public void processAndGetSessionKey_WithoutEncryption() throws Exception {
        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));

        MessageObject resultMessageObject = process.process(messageObject);
        //assert messageObject
        assertTransportMessage(resultMessageObject);


        byte[] fixedBody = assertAuthBodyHeader(
                EMessageType.ACCESS_AUTH_RESPONSE.getValue(),
                resultMessageObject.getMessage(),
                0, 81);
        int offset = 1;

        byte[] deviceID = ArrayUtils.subarray(fixedBody, offset, offset + fixedBody[0]);
        offset += fixedBody[0];
        byte[] salt = ArrayUtils.subarray(fixedBody, offset, offset + 32);
        offset += 32;
        byte[] iterationCountBytes = ArrayUtils.subarray(fixedBody, offset, offset + 2);
        offset += 2;
        int iterationCount = (int) HexUtil.parseBigEndianBytes(iterationCountBytes);
        //reserved
        offset += 2;

        byte[] serverNonce = ArrayUtils.subarray(fixedBody, offset, fixedBody.length - 4);

        //calculate clientProof
        byte[] appendAuthInfo = appendAuthInfo(salt, iterationCount, serverNonce);
        byte[] key = algorithm.getPbkdf2SHA256(quantumKey, salt, iterationCount);
        byte[] hashKey = algorithm.getSHA256(key);
        byte[] hmac = algorithm.getHmacSHA256(hashKey, appendAuthInfo);
        System.out.println("appendInfo: \n" + HexUtil.bytesToHexString(appendAuthInfo));
        System.out.println("key: \n" + HexUtil.bytesToHexString(key));

        byte[] clientProof = algorithm.getXor(key, hmac, key.length);
        byte[] sessionKey = algorithm.getHmacSHA256(key, appendAuthInfo);

        //send second
        authBodyString = type.replace("51", "53") + authHeader + authDeviceID + HexUtil.bytesToHexString(clientProof);
        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));
        resultMessageObject = process.process(messageObject);
        assertNotNull(resultMessageObject);

        assertTransportMessage(resultMessageObject);

        fixedBody = assertAuthBodyHeader(EMessageType.ACCESS_AUTH_SERVER_PROOF.getValue(), resultMessageObject.getMessage(), 0, 45);

        byte[] serverProof = ArrayUtils.subarray(fixedBody, fixedBody.length - 32 - 4, fixedBody.length - 4);
        assertArrayEquals("verify server proof", sessionKey, serverProof);

    }

    /**
     * 使用AES加密
     *
     * @throws Exception
     */
    @Test
    public void processAuthMessage_AES() throws Exception {
        authHeader = "01010030";
        type = "5100780044";
        authBodyString = type + authHeader + rootKeyID;
        String authBody = authDeviceID + clientNonce;
        byte[] encryptedBody = security.encrypt(HexUtil.hexStringToBytes(authBody), HexUtil.hexStringToBytes(rootKey));
        authBodyString = authBodyString + HexUtil.bytesToHexString(encryptedBody);
        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        System.out.println("authBodyString:" + authBodyString);
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));

        MessageObject resultMessageObject = process.process(messageObject);

        assertNotNull(resultMessageObject);
        //assert messageObject
        assertTransportMessage(resultMessageObject);

        byte[] body = resultMessageObject.getMessage();
        System.out.println("server response body:\n" + HexUtil.bytesToHexString(body));

        byte[] fixedBody = assertAuthBodyHeader(EMessageType.ACCESS_AUTH_RESPONSE.getValue(), body, 1, 100);
        int offset = 1;

        byte[] deviceID = ArrayUtils.subarray(fixedBody, offset, offset + fixedBody[0]);
        offset += fixedBody[0];
        byte[] salt = ArrayUtils.subarray(fixedBody, offset, offset + 32);
        offset += 32;
        byte[] iterationCountBytes = ArrayUtils.subarray(fixedBody, offset, offset + 2);
        offset += 2;
        int iterationCount = (int) HexUtil.parseBigEndianBytes(iterationCountBytes);
        //reserved
        offset += 2;

        byte[] serverNonce = ArrayUtils.subarray(fixedBody, offset, fixedBody.length);

        //calculate clientProof
        byte[] appendAuthInfo = appendAuthInfo(salt, iterationCount, serverNonce);
        byte[] key = algorithm.getPbkdf2SHA256(quantumKey, salt, iterationCount);
        byte[] hashKey = algorithm.getSHA256(key);
        byte[] hmac = algorithm.getHmacSHA256(hashKey, appendAuthInfo);
        byte[] clientProof = algorithm.getXor(key, hmac, key.length);
        byte[] sessionKey = algorithm.getHmacSHA256(key, appendAuthInfo);

        //send second
        authBodyString = type.replace("51", "53") +
                authHeader +
                rootKeyID +
                HexUtil.bytesToHexString(
                        security.encrypt(
                                HexUtil.hexStringToBytes(authDeviceID + HexUtil.bytesToHexString(clientProof)),
                                HexUtil.hexStringToBytes(rootKey)));

        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));
        resultMessageObject = process.process(messageObject);
        assertNotNull(resultMessageObject);

        assertTransportMessage(resultMessageObject);

        body = resultMessageObject.getMessage();
        fixedBody = assertAuthBodyHeader(EMessageType.ACCESS_AUTH_SERVER_PROOF.getValue(), body, 1, 68);

        byte[] serverProof = ArrayUtils.subarray(fixedBody, fixedBody.length - 32, fixedBody.length);
        assertArrayEquals("verify server proof", sessionKey, serverProof);

    }

    /**
     * server authentication without encryption algorithm, but with error Version
     *
     * @throws Exception
     */
    @Test
    public void processAuthRequestWithErrorVersion() throws Exception {
        authHeader = "02000029";
        authBodyString = type + authHeader + authDeviceID + clientNonce;
        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));
        MessageObject resultMessageObject = process.process(messageObject);
        assertNotNull("auth message version isn't 1", resultMessageObject);
        assertNotEquals("resturn status is not 0", 0, resultMessageObject.getMessage()[3]);
    }

    /**
     * server authentication without encryption algorithm, but with ErrorAuthMessageLength
     *
     * @throws Exception
     */
    @Test
    public void processAuthRequestWithErrorAuthMessageLength() throws Exception {
        authHeader = "0100" + "0009" + authDeviceID;
        authBodyString = type + authHeader + clientNonce;

        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));
        MessageObject resultMessageObject = process.process(messageObject);
        assertNotNull("auth message version isn't 1", resultMessageObject);
        assertNotEquals("resturn status is not 0", 0, resultMessageObject.getMessage()[3]);
    }

    /**
     * server authentication without encryption algorithm, but with error Version
     *
     * @throws Exception
     */
    @Test
    public void processAuthRequestWithErrorMessageType() throws Exception {
        authHeader = type.replaceFirst("51", "11") + authHeader + authDeviceID;
        String authBodyString = authHeader + clientNonce;

        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));
        MessageObject resultMessageObject = process.process(messageObject);
        assertNotNull("auth message version isn't 1", resultMessageObject);
        assertNotEquals("resturn status is not 0", 0, resultMessageObject.getMessage()[3]);
    }

    /**
     * server authentication without encryption algorithm, but with error Version
     *
     * @throws Exception
     */
    @Test
    public void processAuthRequestWithErrorRootKeyId() throws Exception {
        authHeader = "01010030";
        type = "5100780044";
        authBodyString = type + authHeader + rootKeyID;
        String authBody = authDeviceID + clientNonce;
        byte[] encryptedBody = security.encrypt(HexUtil.hexStringToBytes(authBody), HexUtil.hexStringToBytes(rootKey));
        authBodyString = authBodyString + HexUtil.bytesToHexString(encryptedBody);
        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        System.out.println("authBodyString:" + authBodyString);
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));

        MessageObject resultMessageObject = process.process(messageObject);

        assertNotNull(resultMessageObject);
        //assert messageObject
        assertTransportMessage(resultMessageObject);

        byte[] body = resultMessageObject.getMessage();
        System.out.println("server response body:\n" + HexUtil.bytesToHexString(body));

        byte[] fixedBody = assertAuthBodyHeader(EMessageType.ACCESS_AUTH_RESPONSE.getValue(), body, 1, 100);
        int offset = 1;

        byte[] deviceID = ArrayUtils.subarray(fixedBody, offset, offset + fixedBody[0]);
        offset += fixedBody[0];
        byte[] salt = ArrayUtils.subarray(fixedBody, offset, offset + 32);
        offset += 32;
        byte[] iterationCountBytes = ArrayUtils.subarray(fixedBody, offset, offset + 2);
        offset += 2;
        int iterationCount = (int) HexUtil.parseBigEndianBytes(iterationCountBytes);
        //reserved
        offset += 2;

        byte[] serverNonce = ArrayUtils.subarray(fixedBody, offset, fixedBody.length);

        //calculate clientProof
        byte[] appendAuthInfo = appendAuthInfo(salt, iterationCount, serverNonce);
        byte[] key = algorithm.getPbkdf2SHA256(quantumKey, salt, iterationCount);
        byte[] hashKey = algorithm.getSHA256(key);
        byte[] hmac = algorithm.getHmacSHA256(hashKey, appendAuthInfo);
        byte[] clientProof = algorithm.getXor(key, hmac, key.length);
        byte[] sessionKey = algorithm.getHmacSHA256(key, appendAuthInfo);

        //send second
        byte[] errorRootKeyId = HexUtil.hexStringToBytes("01010101aa0101c1ab01110101110000");
        authBodyString = type.replace("51", "53") +
                authHeader +
                HexUtil.bytesToHexString(errorRootKeyId) +
                HexUtil.bytesToHexString(
                        security.encrypt(HexUtil.hexStringToBytes(authDeviceID + HexUtil.bytesToHexString(clientProof)), HexUtil.hexStringToBytes(rootKey)));

        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));
        resultMessageObject = process.process(messageObject);

        assertNotNull("client second message with error rootKeyID ", resultMessageObject);
        assertNotEquals("return status is not 0", 0, resultMessageObject.getMessage()[3]);
    }

    @Test
    public void processAuthRequestWithErrorDeviceID2() throws Exception {
        authHeader = "01010030";
        type = "5100780044";
        authBodyString = type + authHeader + rootKeyID;
        String authBody = authDeviceID + clientNonce;
        byte[] encryptedBody = security.encrypt(HexUtil.hexStringToBytes(authBody), HexUtil.hexStringToBytes(rootKey));
        authBodyString = authBodyString + HexUtil.bytesToHexString(encryptedBody);
        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        System.out.println("authBodyString:" + authBodyString);
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));

        MessageObject resultMessageObject = process.process(messageObject);

        assertNotNull(resultMessageObject);
        //assert messageObject
        assertTransportMessage(resultMessageObject);

        byte[] body = resultMessageObject.getMessage();
        System.out.println("server response body:\n" + HexUtil.bytesToHexString(body));

        byte[] fixedBody = assertAuthBodyHeader(EMessageType.ACCESS_AUTH_RESPONSE.getValue(), body, 1, 100);
        int offset = 1;

        byte[] deviceID = ArrayUtils.subarray(fixedBody, offset, offset + fixedBody[0]);
        offset += fixedBody[0];
        byte[] salt = ArrayUtils.subarray(fixedBody, offset, offset + 32);
        offset += 32;
        byte[] iterationCountBytes = ArrayUtils.subarray(fixedBody, offset, offset + 2);
        offset += 2;
        int iterationCount = (int) HexUtil.parseBigEndianBytes(iterationCountBytes);
        //reserved
        offset += 2;

        byte[] serverNonce = ArrayUtils.subarray(fixedBody, offset, fixedBody.length);

        //calculate clientProof
        byte[] appendAuthInfo = appendAuthInfo(salt, iterationCount, serverNonce);
        byte[] key = algorithm.getPbkdf2SHA256(quantumKey, salt, iterationCount);
        byte[] hashKey = algorithm.getSHA256(key);
        byte[] hmac = algorithm.getHmacSHA256(hashKey, appendAuthInfo);
        byte[] clientProof = algorithm.getXor(key, hmac, key.length);
        byte[] sessionKey = algorithm.getHmacSHA256(key, appendAuthInfo);

        //send second
        String errorDeviceID = "08010101aa0101c1ab";
        authBodyString = type.replace("51", "53") +
                authHeader +
                rootKeyID +
                HexUtil.bytesToHexString(
                        security.encrypt(HexUtil.hexStringToBytes(errorDeviceID + HexUtil.bytesToHexString(clientProof)), HexUtil.hexStringToBytes(rootKey)));

        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));
        resultMessageObject = process.process(messageObject);

        assertNotNull("client second message with error rootKeyID ", resultMessageObject);
        assertNotEquals("return status is not 0", 0, resultMessageObject.getMessage()[3]);
    }

    /**
     * server authentication without encryption algorithm, but verify client proof error
     *
     * @throws Exception
     */
    @Test
    public void processClientProofWithErrorClientProof() throws Exception {
        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));

        MessageObject resultMessageObject = process.process(messageObject);

        assertNotNull(resultMessageObject);
        //assert messageObject
        assertTransportMessage(resultMessageObject);

        byte[] body = resultMessageObject.getMessage();
        //System.out.println("server response body:\n" + HexUtil.bytesToHexString(body));

        byte[] fixedBody = assertAuthBodyHeader(EMessageType.ACCESS_AUTH_RESPONSE.getValue(), body, EncryptionAlgorithm.DEFAULT, 81);
        int offset = 1;

        byte[] deviceID = ArrayUtils.subarray(fixedBody, offset, offset + fixedBody[0]);
        offset += fixedBody[0];
        byte[] salt = ArrayUtils.subarray(fixedBody, offset, offset + 32);
        offset += 32;
        byte[] iterationCountBytes = ArrayUtils.subarray(fixedBody, offset, offset + 2);
        offset += 2;
        int iterationCount = (int) HexUtil.parseBigEndianBytes(iterationCountBytes);
        //reserved
        offset += 2;

        byte[] serverNonce = ArrayUtils.subarray(fixedBody, offset, fixedBody.length);

        //calculate clientProof
        byte[] appendAuthInfo = appendAuthInfo(salt, iterationCount, serverNonce);
        byte[] key = algorithm.getPbkdf2SHA256(quantumKey, salt, iterationCount);
        byte[] hashKey = algorithm.getSHA256(key);
        byte[] hmac = algorithm.getHmacSHA256(hashKey, appendAuthInfo);
        byte[] clientProof = algorithm.getXor(key, hmac, key.length);
        byte[] sessionKey = algorithm.getHmacSHA256(key, appendAuthInfo);


        type = "530078002D";
        String authBodyStringWithErrorClientProof = type + authHeader + authDeviceID + HexUtil.bytesToHexString(sessionKey);
        authBodyString = authBodyStringWithErrorClientProof + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyStringWithErrorClientProof), 4));
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));
        resultMessageObject = process.process(messageObject);
        assertNotNull("auth message version isn't 1", resultMessageObject);
        assertNotEquals("resturn status is not 0", 0, resultMessageObject.getMessage()[3]);
    }

    /**
     * server authentication without encryption algorithm, but with error Version
     *
     * @throws Exception
     */
    @Test
    public void processAuthRequestWithErrorDeviceID() throws Exception {
        authHeader = type + authHeader + "080101001002031231";
        String authBodyString = authHeader + clientNonce;

        authBodyString = authBodyString + HexUtil.bytesToHexString(HexUtil.toBigEndianBytes(getCrc(authBodyString), 4));
        messageObject = new MessageObject((byte) 1, authBodyString.length() / 2, HexUtil.hexStringToBytes(authBodyString));
        MessageObject resultMessageObject = process.process(messageObject);
        assertNotNull("auth message version isn't 1", resultMessageObject);
        assertNotEquals("resturn status is not 0", 0, resultMessageObject.getMessage()[3]);
    }

    private void assertTransportMessage(MessageObject messageObject) {
        assertNotNull(messageObject);
        assertEquals("MessageObject version is 1", 1, messageObject.getVersion());
        assertEquals("MessageObject encryptionAlgorithm is 0", 0, messageObject.getEncryptionAlgorithm());
        assertNotNull("MessageObject body isn't NULL", messageObject.getMessage());
    }

    private byte[] appendAuthInfo(byte[] salt, int iterationCount, byte[] serverNonce) {
        byte[] appendAuthInfo = new byte[deviceID.length + clientNonce.length() / 2 + salt.length + 2 + serverNonce.length];

        System.arraycopy(deviceID, 0, appendAuthInfo, 0, deviceID.length);
        int offset = deviceID.length;
        System.arraycopy(HexUtil.hexStringToBytes(clientNonce), 0, appendAuthInfo, offset, clientNonce.length() / 2);
        offset += clientNonce.length() / 2;
        System.arraycopy(salt, 0, appendAuthInfo, offset, salt.length);
        offset += salt.length;
        System.arraycopy(HexUtil.littleEndianToBytes(iterationCount), 0, appendAuthInfo, offset, 2);
        offset += 2;
        System.arraycopy(serverNonce, 0, appendAuthInfo, offset, serverNonce.length);

        return appendAuthInfo;
    }

    private byte[] assertAuthBodyHeader(int messageType, byte[] authBody, int encryptionAlgorithm, int dataLength) throws Exception {
        int offset = 0;

        assertEquals("assert message type",
                messageType, authBody[offset]);
        offset += 1;
        assertArrayEquals("assert requestID",
                new byte[]{0, 120}, ArrayUtils.subarray(authBody, offset, offset + 2));
        offset += 2;

        assertEquals("status is 0", StatusEnum.SUCCESS.getValue(), authBody[offset]);
        offset += 1;

        assertEquals("assert data length", dataLength, authBody[offset]);
        offset++;

        assertEquals("version is 1", 1, authBody[offset]);
        offset += 1;

        assertEquals("encryption algorithm :", encryptionAlgorithm, authBody[offset]);
        offset += 1;

        int length = authBody[offset] < 0 ? (authBody[offset] + 256) : authBody[offset] << 8;
        length += authBody[offset + 1] < 0 ? (authBody[offset + 1] + 256) : authBody[offset + 1];
        offset += 2;

        byte[] res;
        int crcLength = 4;
        switch (encryptionAlgorithm) {
            case 1:
                byte[] encryptionKeyId = ArrayUtils.subarray(authBody, offset, offset + 16);
                offset += 16;
                assertEquals("message length", length, authBody.length - offset - crcLength);
                assertNotEquals("encryptionKeyId is not equal 00000000000000000000000000000000", "00000000000000000000000000000000", HexUtil.bytesToHexString(encryptionKeyId));
                byte[] encryptedData = ArrayUtils.subarray(authBody, offset, authBody.length - 4);
                res = security.decrypt(encryptedData, HexUtil.hexStringToBytes(rootKey));
                break;

            default:
                assertEquals("message length", length, authBody.length - offset - crcLength);
                res = ArrayUtils.subarray(authBody, offset, authBody.length);
                break;
        }
        return res;
    }


    private long getCrc(String crcData) {
        byte[] crcBytes = HexUtil.hexStringToBytes(crcData);
        return CRC32Util.getCRC32(crcBytes);
    }

}