package test.dao;

import cn.qtec.qkcl.access.auth.server.algorithm.IAlgorithm;
import cn.qtec.qkcl.access.auth.server.dao.impl.AuthenticationInfoDaoImpl;
import cn.qtec.qkcl.access.auth.server.entity.AuthRespInfo;
import cn.qtec.qkcl.access.auth.server.entity.AuthenticationInfo;
import cn.qtec.qkcl.access.auth.server.security.CrypUtils;
import cn.qtec.qkcl.access.auth.server.utils.HexUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.BaseJunit4;


/**
 * @author Created by zhangp on 2017/9/28.
 */
public class AuthenticationInfoDaoImplTest extends BaseJunit4 {
    @Autowired
    private AuthenticationInfoDaoImpl serverStoredAuthInfoDao;
    @Autowired
    private IAlgorithm algorithm;

    @Test
    public void insertMulti() throws Exception {
        byte[] rootKeyId = new byte[16];
        byte[] rootKeyValue = new byte[32];
        byte[] salt = new byte[32];
        byte[] quantumKey = new byte[32];

        for (int i = -1; i < 5; i++) {
            int iterationCount = 20 + i;
            byte[] quantumKeyPrefix = HexUtil.hexStringToBytes("e3879b3c98ce4a5386416c6a82afee0e7844656c7d8faa3c774455493067a1");
            System.arraycopy(quantumKeyPrefix, 0, quantumKey, 0, 31);
            quantumKey[31] = (byte) (i + 1);

            byte[] saltPrefix = HexUtil.hexStringToBytes("22daad37c72c7f63fd8b7d474198de5de56f842c57046b2b4c552d3698cdd6");
            System.arraycopy(saltPrefix, 0, salt, 0, 31);
            salt[31] = (byte) (i + 1);

            byte[] rootKeyValuePrefix = HexUtil.hexStringToBytes("F04EC04626C75E119B5A923C68D2A879D539F8739F41EE4120B1247209F8AC");
            System.arraycopy(rootKeyValuePrefix, 0, rootKeyValue, 0, 31);
            rootKeyValue[31] = (byte) (i + 1);

            byte[] rootKeyIDPrefix = HexUtil.hexStringToBytes("01011101aac101c1ab011101011111");
            System.arraycopy(rootKeyIDPrefix, 0, rootKeyId, 0, 15);
            rootKeyId[15] = (byte) (i + 1);
            byte[] deviceIDBytes = new byte[7];
            System.arraycopy(HexUtil.hexStringToBytes("010203040500"), 0, deviceIDBytes, 0, deviceIDBytes.length - 1);
            deviceIDBytes[6] = (byte) (i + 1);

            byte[] key = algorithm.getPbkdf2SHA256(quantumKey, salt, iterationCount);
            byte[] hashKey = algorithm.getSHA256(key);

            System.out.println("quantumKey  : " + HexUtil.bytesToHexString(quantumKey));
            System.out.println("hashKey     : " + HexUtil.bytesToHexString(hashKey));

            salt = CrypUtils.passCryp(salt);
            hashKey = CrypUtils.passCryp(hashKey);
            rootKeyValue = CrypUtils.passCryp(rootKeyValue);
            AuthRespInfo authRespInfo = new AuthRespInfo(salt, iterationCount);

            System.out.println("加密后salt         : " + HexUtil.bytesToHexString(authRespInfo.getSalt()));
            System.out.println("加密后hashKey      : " + HexUtil.bytesToHexString(hashKey));
            System.out.println("加密后rootKeyValue : " + HexUtil.bytesToHexString(rootKeyValue));


            AuthenticationInfo info = new AuthenticationInfo();
            info.setAuthRespInfo(authRespInfo);
            info.setRootKeyValue(rootKeyValue);
            info.setDeviceID(HexUtil.bytesToHexString(deviceIDBytes));
            info.setMobilePhone("test_mobilePhone6");
            info.setEmail("test-email6");
            info.setUserType(1);
            info.setRootKeyID(rootKeyId);
            info.setPassword(hashKey);
            info.setRfu2(2);
            info.setRfu1("rfu1");
            info.setPassType(1);
            info.setConfigVersion("1.0");
            info.setParentName("parent");
            info.setUserGUID("guid");
            info.setUserID(11001 + i + 1);


            //D5ADA46D233F4752663A3DC65329321F118149D15AD5205891C594FE689CC9B0
            System.out.println("delete by deviceID: " + HexUtil.bytesToHexString(deviceIDBytes) + " :" + serverStoredAuthInfoDao.deleteAuthenticationInfoByDeviceId(HexUtil.bytesToHexString(deviceIDBytes)));
            serverStoredAuthInfoDao.insert(info);
        }
    }

    @Test
    public void insert() throws Exception {
        int iterationCount = 10;
        byte[] quantumKey = HexUtil.hexStringToBytes("9FE38E186CF8F14AC55E8235258F79320A29E80ECEFC671FE9F6684CE9189D5A");
        byte[] salt = HexUtil.hexStringToBytes("22daad37c72c7f63fd8b7d474198de5de56f842c57046b2b4c552d3698cdd603");
        byte[] rootKeyValue = HexUtil.hexStringToBytes("43B126111AF3B22C19F6B24BC4B7285B5FEE582D6ECAE376C60F8D34A3A70221");
        byte[] rootKeyId = HexUtil.hexStringToBytes("B4A71D3E905E6B6FFA0A8D23DEBA9425");
        byte[] deviceIDBytes = HexUtil.hexStringToBytes("6E6F64655F61745F736C617665");
//        byte[] deviceIDBytes = "12345".getBytes();

        String parentName = "node_at_slave";
        byte[] key = algorithm.getPbkdf2SHA256(quantumKey, salt, iterationCount);
        byte[] hashKey = algorithm.getSHA256(key);

        AuthRespInfo authRespInfo = new AuthRespInfo(CrypUtils.passCryp(salt), iterationCount);
        AuthenticationInfo info = new AuthenticationInfo();
        info.setUserID(9000103);
        info.setAuthRespInfo(authRespInfo);
        info.setRootKeyValue(CrypUtils.passCryp(rootKeyValue));
        info.setPassword(CrypUtils.passCryp(hashKey));
        info.setDeviceID(HexUtil.bytesToHexString(deviceIDBytes));
//        info.setMobilePhone("node_at_master");
//        info.setEmail("node_at_master");
        info.setMobilePhone(parentName);
        info.setEmail(parentName);
        info.setUserType(2);
        info.setRootKeyID(rootKeyId);
        info.setRfu2(2);
        info.setRfu1("rfu1");
        info.setPassType(1);
        info.setConfigVersion("1.0");
        info.setParentName("6e6f64655f61745f6d6173746572");
        info.setUserGUID("guid");

        System.out.println("delete by deviceID: " + HexUtil.bytesToHexString(deviceIDBytes) + " :" + serverStoredAuthInfoDao.deleteAuthenticationInfoByDeviceId(HexUtil.bytesToHexString(deviceIDBytes)));
        serverStoredAuthInfoDao.insert(info);
    }


    @Test
    public void searchAuthInfoByDeviceID() throws Exception {
        String deviceID = "333133373706261c ";

        AuthRespInfo info = serverStoredAuthInfoDao.getAuthRespInfoByDeviceID(deviceID);

        byte[] decSalt = CrypUtils.passCryp(info.getSalt());

        System.out.println("salt    :" + HexUtil.bytesToHexString(decSalt));
        System.out.println("iteration:" + info.getIterationCount());

        byte[] decPassword = CrypUtils.passCryp(serverStoredAuthInfoDao.queryPasswordByDeviceID(deviceID));
        System.out.println("password:" + HexUtil.bytesToHexString(decPassword));
    }

    @Test
    public void queryRootKeyByRootKeyID() throws Exception {
        String salt = "45AC8C52A3494B079BAD0E246BA7EA398143E01F3E650E4A120B4C52B2B8FC07";
        System.out.println(HexUtil.bytesToHexString(CrypUtils.passCryp(HexUtil.hexStringToBytes(salt))));
//        serverStoredAuthInfoDao.queryRootKeyByRootKeyID(HexUtil.hexStringToBytes(rootKeyId));
       /* System.out.println("查询结果：\ndeviceID: " + info.getDeviceID());
        System.out.println("salt    :" + HexUtil.bytesToHexString(info.getSalt()));
        System.out.println("hashKey :" + HexUtil.bytesToHexString(info.getPassword()));
        System.out.println("iteration:" + info.getIterationCount());
        System.out.println("rootKeyValue:" + HexUtil.bytesToHexString(info.getRootKeyValue()));*/
    }


}