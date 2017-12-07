package test.jni;

import cn.qtec.access.auth.client.jni.QKeyService;
import cn.qtec.access.auth.client.security.impl.SecurityImpl;
import cn.qtec.access.auth.client.utils.HexUtil;
import cn.qtec.access.auth.client.utils.JniUtil;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.BaseJunit4;

/**
 * Created by zhangp on 2017/10/18.
 */
public class QKeyServiceTest extends BaseJunit4 {

    private QKeyService qKeyService;
    private SecurityImpl security;
    private String rootKeyValue;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        JniUtil.addLibraryDir(JniUtil.LIBRARY_PATH_WINDOW_x64);
    }

    @Test
    public void genStartInfo() throws Exception {

        byte[] startInfo = qKeyService.genStartInfo();
        Assert.assertNotNull("StartInfo is not NULL", startInfo);

        System.out.println("startInfo   :" + HexUtil.bytesToHexString(startInfo));
        int version = startInfo[0];
        int offset = 1;
        int encryptionAlgorithm = startInfo[offset];
        offset += 1;
        int messsageLength = HexUtil.parseLittleEndianBytes(ArrayUtils.subarray(startInfo, offset, offset + 2));
        offset += 2;

        System.out.println("encryptionAlgorithm :" + encryptionAlgorithm);
        byte[] fixedBody;
        switch (encryptionAlgorithm) {
            case 1:
                byte[] encryptionKeyId = ArrayUtils.subarray(startInfo, offset, offset + 16);
                offset += 16;
                byte[] body = ArrayUtils.subarray(startInfo, offset, startInfo.length);
                System.out.println("encryptionKeyId :" + HexUtil.bytesToHexString(encryptionKeyId));
                System.out.println("encryptionBody  :" + HexUtil.bytesToHexString(body));

                fixedBody = security.decrypt(body, HexUtil.hexStringToBytes(rootKeyValue));
                break;

            default:
                fixedBody = ArrayUtils.subarray(startInfo, offset, startInfo.length);
                break;
        }

        System.out.println("fixedBody     :" + HexUtil.bytesToHexString(fixedBody));
        int length = fixedBody[0];
        offset = 1;
        byte[] deviceID = ArrayUtils.subarray(fixedBody, offset, offset + length);
        offset += length;
        byte[] clientNonce = ArrayUtils.subarray(fixedBody, offset, fixedBody.length);

        System.out.println("deviceIdLength  :" + length);
        System.out.println("deviceId        :" + HexUtil.bytesToHexString(deviceID));
        System.out.println("clientNonce     :" + HexUtil.bytesToHexString(clientNonce));
    }

    @Test
    public void genAuthInfo() throws Exception {
    }

    @Test
    public void checkAuthInfo() throws Exception {
    }

    @Test
    public void encryptQuantumKeys() throws Exception {
    }

    @Test
    public void decryptQuantumKeys() throws Exception {

    }

    @Test
    public void updateRootKey() throws Exception {
    }

    @Test
    public void confirmRootKey() throws Exception {

    }

}