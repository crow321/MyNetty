package test.process;

import cn.qtec.qkcl.access.auth.client.algorithm.impl.AlgorithmImpl;
import cn.qtec.qkcl.access.auth.client.entity.AccessAuthInfo;
import cn.qtec.qkcl.access.auth.client.message.TransportMessage;
import cn.qtec.qkcl.access.auth.client.process.AbstractAuthClientProcessor;
import cn.qtec.qkcl.access.auth.client.process.impl.QShieldAuthProcessor;
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

import static org.junit.Assert.*;

/**
 * @author Created by zhangp
 *         2017/11/16
 */
public class QShiledProcessorTest extends BaseJunit4 {

    private AbstractAuthClientProcessor qShieldProcessor;
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
        qShieldProcessor = new QShieldAuthProcessor();
        algorithm = new AlgorithmImpl();
        security = new SecurityImpl();

        deviceID = HexUtil.hexStringToBytes("3331333737042e21");
        quantumKey = HexUtil.hexStringToBytes("e3879b3c98ce4a5386416c6a82afee0e7844656c7d8faa3c774455493067a136");
        rootKeyId = HexUtil.hexStringToBytes("01010101010101f1ab01110101111105");
        rootKeyValue = HexUtil.hexStringToBytes("F04EC04626C75E119B5A923C68D2A879D539F8739F41EE4120B1247209F8AC54");
        serverNonce = algorithm.generateRandom(32);
        salt = HexUtil.hexStringToBytes("22daad37c72c7f63fd8b7d474198de5de56f842c57046b2b4c552d3698cdd66e");
        iterationCount = 20;

    }

    private AccessAuthInfo getInitAuthInfo() {
        return null;
    }

    @Test
    public void startAuthRequest() throws Exception {
        TransportMessage message = qShieldProcessor.generateStartAuthRequest(null);

        assertEquals("version is 1", 1, message.getVersion());
        assertArrayEquals("deviceID is 3331333737042e21", deviceID, message.getDeviceID());

        //header
        byte[] authData = checkMessageBodyHeader(message.getBody());
        checkAuthData(authData);
    }

    @Test
    public void processAuthentication() throws Exception {
        TransportMessage message = qShieldProcessor.generateStartAuthRequest(null);

        assertEquals("version is 1", 1, message.getVersion());
        assertArrayEquals("deviceID is 3331333737042e21", deviceID, message.getDeviceID());

        //header
        byte[] authData = checkMessageBodyHeader(message.getBody());
        checkAuthData(authData);
        int algorithm = 1;
        byte[] clientNonce = checkAuthData(authData);

        byte[] data = generateServerResponse(algorithm, 0);

        MessageObject messageObject = new MessageObject((byte) 1, data.length, data);
        AuthencationInfo authencationInfo = qShieldProcessor.processAuthencaition(messageObject);

        assertNotNull(authencationInfo);
        assertNotNull(authencationInfo.getMessageObject());
        assertNull(authencationInfo.getSessionInfo());

        byte[] serverProofBody = generateServerProof(clientNonce, 20);
        messageObject.setMessage(serverProofBody);
        authencationInfo = qShieldProcessor.processAuthencaition(messageObject);

        assertNotNull(authencationInfo);
        assertNull(authencationInfo.getMessageObject());
        assertNotNull(authencationInfo.getSessionInfo());
        assertNotNull(qShieldProcessor.getSessionInfo(5000, deviceID));
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

        byte[] data = generateServerResponse(algorithm, 0);

        MessageObject messageObject = new MessageObject((byte) 1, data.length, data);
        AuthencationInfo authencationInfo = qShieldProcessor.processAuthencaition(messageObject);

        assertNotNull(authencationInfo);
        assertNotNull(authencationInfo.getMessageObject());
        assertNull(authencationInfo.getSessionInfo());

        int errorIterationCount = 10;
        byte[] serverProofBody = generateServerProof(clientNonce, errorIterationCount);
        messageObject.setMessage(serverProofBody);
        authencationInfo = qShieldProcessor.processAuthencaition(messageObject);

        assertNotNull(authencationInfo);
        assertNull(authencationInfo.getMessageObject());
        assertNull(authencationInfo.getSessionInfo());
    }

    @Test
    public void processAuthentication_AES_ErrorStatus() throws Exception {
        deviceID = "1234".getBytes();
        byte[] data = generateServerResponse(1, 0);

        MessageObject messageObject = new MessageObject((byte) 1, data.length, data);
        AuthencationInfo authencationInfo = qShieldProcessor.processAuthencaition(messageObject);

        assertNotNull(authencationInfo);
        assertNull(authencationInfo.getMessageObject());
        assertNull(authencationInfo.getSessionInfo());
    }

    @Test(expected = NullPointerException.class)
    public void processAuthentication_AES_NullMessageObject() throws Exception {
        qShieldProcessor.processAuthencaition(null);
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
        int CRC_OR_HEADER_LENGTH = 4;
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


    private byte[] generateServerResponse(int algorithm, int status) throws Exception {
        ByteBuf dataBuf = Unpooled.buffer();
        dataBuf.writeByte(deviceID.length);
        dataBuf.writeBytes(deviceID);
        dataBuf.writeBytes(salt);
        dataBuf.writeBytes(HexUtil.toBigEndianBytes(iterationCount, 2));
        dataBuf.writeShort(0);
        dataBuf.writeBytes(serverNonce);
        byte[] dataBytes = new byte[dataBuf.readableBytes()];
        dataBuf.readBytes(dataBytes);
        byte[] encData;
        encData = security.encrypt(dataBytes, rootKeyValue);


        ByteBuf dataHeaderBuf = Unpooled.buffer();
        dataHeaderBuf.writeByte(1);
        dataHeaderBuf.writeByte(1);
        dataHeaderBuf.writeShort(encData.length);
        dataHeaderBuf.writeBytes(rootKeyId);

        dataHeaderBuf.writeBytes(encData);

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(82);
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

    private byte[] generateServerProof(byte[] clientNonce, int iterationCount) throws Exception {
        assertNotSame(new byte[32], salt);

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

        System.out.println("appendInfo: " + HexUtil.bytesToHexString(appendInfo));
        byte[] serverProof = algorithm.getHmacSHA256(key, appendInfo);

        ByteBuf dataBuf = Unpooled.buffer();
        dataBuf.writeByte(deviceIDLength);
        dataBuf.writeBytes(deviceID);
        dataBuf.writeBytes(serverProof);

        byte[] dataBytes = new byte[dataBuf.readableBytes()];
        dataBuf.readBytes(dataBytes);
        byte[] encData = security.encrypt(dataBytes, rootKeyValue);

        ByteBuf proofBuf = Unpooled.buffer();
        proofBuf.writeByte(1);
        proofBuf.writeByte(1);
        proofBuf.writeShort(encData.length);
        proofBuf.writeBytes(rootKeyId);
        proofBuf.writeBytes(encData);

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

        TransportMessage message = qShieldProcessor.generateStartAuthRequest(null);

        assertEquals("version is 1", 1, message.getVersion());
        assertArrayEquals("deviceID is Thread-1", deviceID, message.getDeviceID());

        //header
        byte[] authData = checkMessageBodyHeader(message.getBody());
        return checkAuthData(authData);
    }

}