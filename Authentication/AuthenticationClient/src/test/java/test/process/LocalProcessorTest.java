package test.process;

import cn.qtec.qkcl.access.auth.client.algorithm.impl.AlgorithmImpl;
import cn.qtec.qkcl.access.auth.client.entity.AccessAuthInfo;
import cn.qtec.qkcl.access.auth.client.message.TransportMessage;
import cn.qtec.qkcl.access.auth.client.process.AbstractAuthClientProcessor;
import cn.qtec.qkcl.access.auth.client.process.impl.LocalAuthProcessor;
import cn.qtec.qkcl.access.auth.client.security.ISecurity;
import cn.qtec.qkcl.access.auth.client.security.impl.SecurityImpl;
import cn.qtec.qkcl.access.auth.client.utils.CRC32Util;
import cn.qtec.qkcl.access.auth.client.utils.HexUtil;
import cn.qtec.qkcl.message.enums.EMessageType;
import cn.qtec.qkcl.message.vo.AuthencationInfo;
import cn.qtec.qkcl.message.vo.MessageObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import test.BaseJunit4;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Created by zhangp
 *         2017/11/16
 */
public class LocalProcessorTest extends BaseJunit4 {
    private final int CRC_OR_HEADER_LENGTH = 4;
    private byte[] serverFirstResponse;
    private byte[] serverFirstResponseAES;


    private AbstractAuthClientProcessor localProcessor;
    private AlgorithmImpl algorithm;
    private ISecurity security;
    private byte[] deviceID;
    private byte[] quantumKey;
    private byte[] rootKeyId;
    private byte[] rootKeyValue;
    private byte[] serverNonce;
    private byte[] salt;
    private int iterationCount;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        localProcessor = new LocalAuthProcessor();
        algorithm = new AlgorithmImpl();
        security = new SecurityImpl();
        deviceID = "Thread-1".getBytes();

        quantumKey = HexUtil.hexStringToBytes("e3879b3c98ce4a5386416c6a82afee0e7844656c7d8faa3c774455493067a101");
        rootKeyId = HexUtil.hexStringToBytes("01010101aa0101c1ab01110101111101");
        rootKeyValue = HexUtil.hexStringToBytes("F04EC04626C75E119B5A923C68D2A879D539F8739F41EE4120B1247209F8AC01");
        serverNonce = algorithm.generateRandom(32);
        salt = HexUtil.hexStringToBytes("22daad37c72c7f63fd8b7d474198de5de56f842c57046b2b4c552d3698cdd601");
        iterationCount = 20;

    }

    private AccessAuthInfo getInitAuthInfo() {
        AccessAuthInfo initAuthInfo = new AccessAuthInfo();
        initAuthInfo.setDeviceID(deviceID);
        initAuthInfo.setQuantumKeyValue(quantumKey);
        initAuthInfo.setEncryptionAlgorithm(1);
        initAuthInfo.setRootKeyId(rootKeyId);
        initAuthInfo.setRootKeyValue(rootKeyValue);

        return initAuthInfo;
    }

    @Test
    public void startAuthRequest() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        accessAuthInfo.setEncryptionAlgorithm(0);
        TransportMessage message = localProcessor.generateStartAuthRequest(accessAuthInfo);

        assertEquals("version is 1", 1, message.getVersion());
        assertArrayEquals("deviceID is Thread-1", deviceID, message.getDeviceID());

        //header
        byte[] authData = checkMessageBodyHeader(message.getBody());
        checkAuthData(authData);
    }

    @Test
    public void startAuthRequest_AES() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        accessAuthInfo.setEncryptionAlgorithm(1);
        TransportMessage message = localProcessor.generateStartAuthRequest(accessAuthInfo);

        assertEquals("version is 1", 1, message.getVersion());
        assertArrayEquals("deviceID is Thread-1", deviceID, message.getDeviceID());

        byte[] authData = checkMessageBodyHeader(message.getBody());
        checkAuthData(authData);
    }

    @Test
    public void processAuthentication() throws Exception {
        int algorithm = 0;
        byte[] clientNonce = getClientNonceFromStartMessage(algorithm);

        byte[] data = generateServerResponse(algorithm, 0, 82);

        MessageObject messageObject = new MessageObject((byte) 1, data.length, data);
        AuthencationInfo authencationInfo = localProcessor.processAuthencaition(messageObject);

        assertNotNull(authencationInfo);
        assertNotNull(authencationInfo.getMessageObject());
        assertNull(authencationInfo.getSessionInfo());

        byte[] serverProofBody = generateServerProof(clientNonce, 20);
        messageObject.setMessage(serverProofBody);
        authencationInfo = localProcessor.processAuthencaition(messageObject);

        assertNotNull(authencationInfo);
        assertNull(authencationInfo.getMessageObject());
        assertNotNull(authencationInfo.getSessionInfo());
    }

    @Test
    public void processAuthentication_AES() throws Exception {
        int algorithm = 1;
        byte[] clientNonce = getClientNonceFromStartMessage(algorithm);

        byte[] data = generateServerResponse(algorithm, 0, 82);

        MessageObject messageObject = new MessageObject((byte) 1, data.length, data);
        AuthencationInfo authencationInfo = localProcessor.processAuthencaition(messageObject);

        assertNotNull(authencationInfo);
        assertNotNull(authencationInfo.getMessageObject());
        assertNull(authencationInfo.getSessionInfo());

        byte[] serverProofBody = generateServerProof(clientNonce, 20);
        messageObject.setMessage(serverProofBody);
        authencationInfo = localProcessor.processAuthencaition(messageObject);

        assertNotNull(authencationInfo);
        assertNull(authencationInfo.getMessageObject());
        assertNotNull(authencationInfo.getSessionInfo());
        assertNotNull(localProcessor.getSessionInfo(5000, deviceID));
    }

    /**
     * 二次校验失败
     *
     * @throws Exception
     */
    @Test
    public void processAuthentication_AES_ErrorServerProof() throws Exception {
        int algorithm = 1;
        byte[] clientNonce = getClientNonceFromStartMessage(algorithm);

        byte[] data = generateServerResponse(algorithm, 0, 82);

        MessageObject messageObject = new MessageObject((byte) 1, data.length, data);
        AuthencationInfo authencationInfo = localProcessor.processAuthencaition(messageObject);

        assertNotNull(authencationInfo);
        assertNotNull(authencationInfo.getMessageObject());
        assertNull(authencationInfo.getSessionInfo());

        int errorIterationCount = 10;
        byte[] serverProofBody = generateServerProof(clientNonce, errorIterationCount);
        messageObject.setMessage(serverProofBody);
        authencationInfo = localProcessor.processAuthencaition(messageObject);

        assertNotNull(authencationInfo);
        assertNull(authencationInfo.getMessageObject());
        assertNull(authencationInfo.getSessionInfo());
        assertNull(localProcessor.getSessionInfo(5000, deviceID));
    }

    @Test(expected = NullPointerException.class)
    public void processAuthentication_AES_NullMessage() throws Exception {
        localProcessor.processAuthencaition(null);
    }

    @Test(expected = NullPointerException.class)
    public void processAuthentication_AES_NullBody() throws Exception {
        int algorithm = 1;

        byte[] data = generateServerResponse(algorithm, 0, 82);

        MessageObject messageObject = new MessageObject((byte) 1, data.length, null);
        localProcessor.processAuthencaition(messageObject);
    }

    @Test
    public void processAuthentication_AES_FailedStatues() throws Exception {
        int algorithm = 1;
        byte[] data = generateServerResponse(algorithm, 1, 82);

        MessageObject messageObject = new MessageObject((byte) 1, data.length, data);
        localProcessor.processAuthencaition(messageObject);
    }

    @Test
    public void processAuthentication_AES_ErrorMessageType() throws Exception {
        int algorithm = 1;
        byte[] data = generateServerResponse(algorithm, 1, 66);

        MessageObject messageObject = new MessageObject((byte) 1, data.length, data);
        localProcessor.processAuthencaition(messageObject);
    }


    @Test(expected = NullPointerException.class)
    public void startAuthRequest_AES_NullAccessAuthInfo() throws Exception {
        localProcessor.generateStartAuthRequest(null);
    }

    @Test(expected = NullPointerException.class)
    public void startAuthRequest_AES_NullDeviceID() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        accessAuthInfo.setDeviceID(null);
        localProcessor.generateStartAuthRequest(accessAuthInfo);
    }

    @Test(expected = NullPointerException.class)
    public void startAuthRequest_AES_NullQuantumKey() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        accessAuthInfo.setQuantumKeyValue(null);
        localProcessor.generateStartAuthRequest(accessAuthInfo);
    }

    @Test(expected = NullPointerException.class)
    public void startAuthRequest_AES_NullRootKeyID() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        accessAuthInfo.setRootKeyId(null);
        localProcessor.generateStartAuthRequest(accessAuthInfo);
    }

    @Test(expected = NullPointerException.class)
    public void startAuthRequest_AES_NullRootKeyValue() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        accessAuthInfo.setRootKeyValue(null);
        localProcessor.generateStartAuthRequest(accessAuthInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void startAuthRequest_AES_ErrorDeviceID_TooShort() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        accessAuthInfo.setDeviceID("test".getBytes());
        localProcessor.generateStartAuthRequest(accessAuthInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void startAuthRequest_AES_ErrorDeviceID_TooLong() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        accessAuthInfo.setDeviceID((uuid + "1234567890").getBytes());
        localProcessor.generateStartAuthRequest(accessAuthInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void startAuthRequest_AES_ErrorQuantumKey_TooShort() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        accessAuthInfo.setQuantumKeyValue("12345".getBytes());
        localProcessor.generateStartAuthRequest(accessAuthInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void startAuthRequest_AES_ErrorQuantumKey_TooLong() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        //129字节
        accessAuthInfo.setDeviceID((uuid + uuid + uuid + uuid + uuid + uuid + uuid + uuid + "1").getBytes());
        localProcessor.generateStartAuthRequest(accessAuthInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void startAuthRequest_AES_ErrorRootKeyId_Not16Bytes() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        //17字节
        accessAuthInfo.setRootKeyId((uuid + "1").getBytes());
        localProcessor.generateStartAuthRequest(accessAuthInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void startAuthRequest_AES_ErrorRootKeyValue_Not16Bytes() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        //17字节
        accessAuthInfo.setRootKeyValue((uuid + uuid + "1").getBytes());
        localProcessor.generateStartAuthRequest(accessAuthInfo);
    }

    @Test
    public void startAuthRequest_AES_ErrorEncryptionAlgorithm() throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        accessAuthInfo.setEncryptionAlgorithm(3);
        assertNull(localProcessor.generateStartAuthRequest(accessAuthInfo));
    }


    private byte[] checkAuthData(byte[] authData) throws Exception {
        ByteBuf byteBuf = Unpooled.copiedBuffer(authData);

        assertEquals("version is 1", 1, byteBuf.readByte());
        byte algorithm = byteBuf.readByte();

        short messageLength = byteBuf.readShort();

        byte[] decBody;
        if (algorithm == 1) {
            assertEquals("message length is 48", 48, messageLength);
            assertEquals("encryption algorithm is 1", 1, algorithm);
            byte[] rootKeyId = new byte[16];
            byteBuf.readBytes(rootKeyId);
            assertNotSame("rootKeyID is not 0", new byte[16], rootKeyId);

            byte[] encBody = new byte[messageLength];
            byteBuf.readBytes(encBody);
            decBody = security.decrypt(encBody, rootKeyValue);
        } else {
            assertEquals("message length is 41", 41, messageLength);
            decBody = new byte[messageLength];
            byteBuf.readBytes(decBody);
        }


        int offset = 0;
        byte deviceIdLength = decBody[offset];
        offset++;
        byte[] deviceID = ArrayUtils.subarray(decBody, offset, offset + deviceIdLength);
        offset += deviceIdLength;
        assertArrayEquals(this.deviceID, deviceID);

        byte[] clientNonce = ArrayUtils.subarray(decBody, offset, decBody.length);
        assertNotSame(new byte[32], clientNonce);

        return clientNonce;
    }

    private byte[] checkMessageBodyHeader(byte[] messageBody) {
        ByteBuf startByteBuf = Unpooled.copiedBuffer(messageBody);
        byte[] crcData = new byte[startByteBuf.readableBytes() - CRC_OR_HEADER_LENGTH];
        Unpooled.copiedBuffer(startByteBuf).readBytes(crcData);


        assertEquals("message type is 81", 81, startByteBuf.readByte());

        byte[] requestIDBytes = new byte[2];
        startByteBuf.readBytes(requestIDBytes);
        assertNotEquals("requestID is not equals 0", 0, HexUtil.parseBigEndianBytes(requestIDBytes));

        assertEquals("reserved bit default is 0", 0, startByteBuf.readByte());

        int dataLength = startByteBuf.readByte();
        assertEquals("check data length ", dataLength, startByteBuf.readableBytes() - CRC_OR_HEADER_LENGTH);
        byte[] startAuthData = new byte[dataLength];
        startByteBuf.readBytes(startAuthData);


        long crc = startByteBuf.readUnsignedInt();
        long calculateCrc = CRC32Util.getCRC32(crcData);
        assertEquals("check crc", crc, calculateCrc);

        startByteBuf.release();
        return startAuthData;
    }


    private byte[] generateServerResponse(int algorithm, int status, int messageType) throws Exception {
        ByteBuf dataBuf = Unpooled.buffer();
        dataBuf.writeByte(deviceID.length);
        dataBuf.writeBytes(deviceID);
        dataBuf.writeBytes(salt);
        dataBuf.writeBytes(HexUtil.toBigEndianBytes(iterationCount, 2));
        dataBuf.writeShort(0);
        dataBuf.writeBytes(serverNonce);
        byte[] dataBytes = new byte[dataBuf.readableBytes()];
        dataBuf.readBytes(dataBytes);

        ByteBuf dataHeaderBuf = Unpooled.buffer();
        dataHeaderBuf.writeByte(1);
        dataHeaderBuf.writeByte(algorithm);
        dataHeaderBuf.writeShort(dataBuf.readableBytes());
        byte[] decDataBytes;
        if (algorithm == 1) {
            dataHeaderBuf.writeBytes(rootKeyId);
            decDataBytes = security.encrypt(dataBytes, rootKeyValue);
        } else {
            decDataBytes = dataBytes;
        }
        dataHeaderBuf.writeBytes(decDataBytes);

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(messageType);
        buf.writeShort(120);
        buf.writeByte(status);
        buf.writeByte(dataHeaderBuf.readableBytes());
        buf.writeBytes(dataHeaderBuf);

        byte[] crcBytes = new byte[buf.readableBytes()];
        Unpooled.copiedBuffer(buf).readBytes(crcBytes);
        buf.writeInt((int) CRC32Util.getCRC32(crcBytes));

        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        return data;
    }

    private byte[] generateServerProof(byte[] clientNonce, int iterationCount) {
        assertNotSame(new byte[32], salt);
        assertNotEquals(0, this.iterationCount);

        byte[] key = algorithm.getPbkdf2SHA256(quantumKey, salt, this.iterationCount);
        byte[] hashKey = algorithm.getSHA256(key);

        int deviceIDLength = deviceID.length;
        byte[] appendInfo = new byte[deviceIDLength + 32 * 3 + 2];
        int offset = 0;
        System.arraycopy(deviceID, 0, appendInfo, offset, deviceIDLength);
        offset += deviceIDLength;
        System.arraycopy(clientNonce, 0, appendInfo, offset, 32);
        offset += 32;
        System.arraycopy(salt, 0, appendInfo, offset, 32);
        offset += 32;
        appendInfo[offset] = (byte) iterationCount;
        offset += 2;
        System.arraycopy(serverNonce, 0, appendInfo, offset, 32);

        System.out.println("---- appendInfo: \n" + HexUtil.bytesToHexString(appendInfo));
        byte[] serverProof = algorithm.getHmacSHA256(key, appendInfo);

        ByteBuf dataBuf = Unpooled.buffer();
        dataBuf.writeByte(deviceIDLength);
        dataBuf.writeBytes(deviceID);
        dataBuf.writeBytes(serverProof);

        ByteBuf proofBuf = Unpooled.buffer();
        proofBuf.writeByte(1);
        proofBuf.writeByte(0);
        proofBuf.writeShort(dataBuf.readableBytes());
        proofBuf.writeBytes(dataBuf);

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(EMessageType.ACCESS_AUTH_SERVER_PROOF.getValue());
        buf.writeShort(120);
        buf.writeByte(0);
        buf.writeByte(proofBuf.readableBytes());
        buf.writeBytes(proofBuf);

        byte[] crcBytes = new byte[buf.readableBytes()];
        Unpooled.copiedBuffer(buf).readBytes(crcBytes);

        long crc = CRC32Util.getCRC32(crcBytes);
        buf.writeInt((int) crc);

        byte[] proofBytes = new byte[buf.readableBytes()];
        buf.readBytes(proofBytes);

        return proofBytes;
    }

    private byte[] getClientNonceFromStartMessage(int algorithm) throws Exception {
        AccessAuthInfo accessAuthInfo = getInitAuthInfo();
        accessAuthInfo.setEncryptionAlgorithm(algorithm);
        TransportMessage message = localProcessor.generateStartAuthRequest(accessAuthInfo);

        assertEquals("version is 1", 1, message.getVersion());
        assertArrayEquals("deviceID is Thread-1", deviceID, message.getDeviceID());

        //header
        byte[] authData = checkMessageBodyHeader(message.getBody());
        return checkAuthData(authData);
    }

}