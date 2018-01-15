package test.jni;

import cn.qtec.qkcl.access.auth.client.algorithm.impl.AlgorithmImpl;
import cn.qtec.qkcl.access.auth.client.jni.QKeyService;
import cn.qtec.qkcl.access.auth.client.security.impl.SecurityImpl;
import cn.qtec.qkcl.access.auth.client.utils.HexUtil;
import cn.qtec.qkcl.access.auth.client.utils.JniUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.BaseJunit4;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by zhangp on 2017/10/18.
 */
public class QKeyServiceTest extends BaseJunit4 {

    private QKeyService qKeyService;
    private SecurityImpl security;
    private AlgorithmImpl algorithm;
    private String rootKeyValue;
    private String rootKeyId;
    private String quantumKey;
    private String salt;
    private int iterationCount;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        qKeyService = QKeyService.getINSTANCE();
        security = new SecurityImpl();
        algorithm = new AlgorithmImpl();
        rootKeyId = "01010101010101010101010101010104";
        rootKeyValue = "0202020202020202020202020605040102020202020202020202020206050401";
        quantumKey = "02020202020202020202020206050401020202020202020202020202060504aa";
//        quantumKey = "91E84AF3BFAB4D5377CCEF39FD92A4FA27392942CF6BD2725C03ED4383AEC537";
        salt = "7621953663556225858664545381195145613985143332574529001772491743";
        iterationCount = 1;
        JniUtil.loadJavaLibraryDir();
    }
    @Test
    public void genStartInfo() throws Exception {
        QKeyService res = qKeyService.genStartInfo();
        byte[] startInfo = res.getStartAuthInfo();
        byte[] deviceID = res.getDeviceID();
        Assert.assertNotNull("StartInfo is not NULL", startInfo);
        Assert.assertNotNull("deviceID is not NULL", startInfo);

        System.out.println("startInfo   :" + HexUtil.bytesToHexString(startInfo));
        //System.out.println("deviceID    :" + HexUtil.bytesToHexString(deviceID));
        int version = startInfo[0];
        int offset = 1;
        int encryptionAlgorithm = startInfo[offset];
        offset += 1;
        int messsageLength = (int) HexUtil.parseLittleEndianBytes(ArrayUtils.subarray(startInfo, offset, offset + 2));
        offset += 2;

        System.out.println("encryptionAlgorithm :" + encryptionAlgorithm);
        byte[] fixedBody;
        switch (encryptionAlgorithm) {
            case 1:
                byte[] encryptionKeyId = ArrayUtils.subarray(startInfo, offset, offset + 16);
                offset += 16;
                byte[] body = ArrayUtils.subarray(startInfo, offset, startInfo.length);
                System.out.println("encryptionKeyId :" + HexUtil.bytesToHexString(encryptionKeyId));
                //System.out.println("encryptionBody  :" + HexUtil.bytesToHexString(body));

                fixedBody = security.decrypt(body, HexUtil.hexStringToBytes(rootKeyValue));
                break;

            default:
                fixedBody = ArrayUtils.subarray(startInfo, offset, startInfo.length);
                break;
        }

        //System.out.println("fixedBody     :" + HexUtil.bytesToHexString(fixedBody));
        int length = fixedBody[0];
        offset = 1;
        deviceID = ArrayUtils.subarray(fixedBody, offset, offset + length);
        offset += length;
        byte[] clientNonce = ArrayUtils.subarray(fixedBody, offset, fixedBody.length);

        System.out.println("deviceIdLength  :" + length);
        System.out.println("deviceID        :" + HexUtil.bytesToHexString(deviceID));
        System.out.println("clientNonce     :" + HexUtil.bytesToHexString(clientNonce));

        //计算hashKey
        byte[] key = algorithm.getPbkdf2SHA256(HexUtil.hexStringToBytes(quantumKey), HexUtil.hexStringToBytes(salt), iterationCount);
        byte[] hashKey = algorithm.getSHA256(key);
        System.out.println("hashKey         :" + HexUtil.bytesToHexString(hashKey));
    }
}