package cn.qtec.qkcl.access.auth.client.demo;

import cn.qtec.qkcl.access.auth.client.constant.AuthModeEnum;
import cn.qtec.qkcl.access.auth.client.entity.AccessAuthInfo;
import cn.qtec.qkcl.access.auth.client.entity.AuthClientSessionInfo;
import cn.qtec.qkcl.access.auth.client.netty.AuthenticationClient;
import cn.qtec.qkcl.access.auth.client.utils.HexUtil;
import cn.qtec.qkcl.kmip.transport.transfer.TransEnum;
import io.netty.channel.Channel;

import java.util.List;

/**
 * @author Created by zhangp
 *         2017/11/16
 */
public class Client {
    private static int totalCount;
    private static int successCount;

    public static void main(String[] args) throws InterruptedException {
        //多线程
        //multiThread();

        //本地认证
        startAuthByLocal(1);

        //Q盾认证
        //starAuthByQShield();
        System.out.println("运行总次数:" + totalCount + "成功次数 " + successCount);
    }

    private static void multiThread() throws InterruptedException {
        for (int i = 0; i < 20; i++) {
                Thread thread = new MultiThreadClient();
                thread.start();

                totalCount++;
            }
        System.out.println("=========================================================================================================");
    }


    /**
     * Crow
     *
     * @param encryptionAlgorithm 0-不加密，1-AES加密
     */
    private static void startAuthByLocal(int encryptionAlgorithm) {
        String deviceID = "a0b0c0d0e00001";
        byte[] quantumKey = HexUtil.hexStringToBytes("e3879b3c98ce4a5386416c6a82afee0e7844656c7d8faa3c774455493067a101");
        byte[] rootKeyId = HexUtil.hexStringToBytes("01010101aa0101c1ab01110101111101");
        byte[] rootKeyValue = HexUtil.hexStringToBytes("F04EC04626C75E119B5A923C68D2A879D539F8739F41EE4120B1247209F8AC01");
        //初始化接入认证消息
        AccessAuthInfo accessAuthInfo = new AccessAuthInfo();
        accessAuthInfo.setRemoteIP("192.168.90.233");
        accessAuthInfo.setPort(7700);
        accessAuthInfo.setAuthModeEnum(AuthModeEnum.LOCAL);
        accessAuthInfo.setTransEnum(TransEnum.TCP);
        accessAuthInfo.setQuantumKeyValue(quantumKey);
        accessAuthInfo.setDeviceID(deviceID.getBytes());

        accessAuthInfo.setEncryptionAlgorithm(encryptionAlgorithm);
        if (encryptionAlgorithm == 1) {
            accessAuthInfo.setRootKeyId(rootKeyId);
            accessAuthInfo.setRootKeyValue(rootKeyValue);
        }

        AuthenticationClient client = new AuthenticationClient();
        AuthClientSessionInfo authClientSessionInfo = client.startAccessAuth(accessAuthInfo);

        if (authClientSessionInfo != null) {
            System.out.println("Authentication by Local and get deviceID  : " + new String(authClientSessionInfo.getUsername()));
            System.out.println("Authentication by Local and get sessionID : " + HexUtil.bytesToHexString(authClientSessionInfo.getSessionID()));
            System.out.println("Authentication by Local and get sessionKey: " + HexUtil.bytesToHexString(authClientSessionInfo.getSessionKey()));
            List<Channel> channels = authClientSessionInfo.getChannels();
            for (Channel channel : channels) {
                System.out.println("Authentication by Local and get channel   : " + channel.remoteAddress());
            }
            successCount++;
        } else {
            System.out.println("session is NULL");
        }

        totalCount++;
        //client.close();
    }

    /**
     * 使用Q盾进行接入认证
     */
    private static void starAuthByQShield() {
        AuthenticationClient client = new AuthenticationClient();

        AccessAuthInfo accessAuthInfo = new AccessAuthInfo();
        accessAuthInfo.setRemoteIP("192.168.90.233");
        accessAuthInfo.setPort(7700);
        accessAuthInfo.setAuthModeEnum(AuthModeEnum.Q_SHIELD);
        accessAuthInfo.setTransEnum(TransEnum.TCP);

        AuthClientSessionInfo authClientSessionInfo = client.startAccessAuth(accessAuthInfo);

        if (authClientSessionInfo != null) {
            System.out.println("Authentication by QShield and get deviceID  : " + authClientSessionInfo.getUsername());
            System.out.println("Authentication by QShield and get sessionID : " + HexUtil.bytesToHexString(authClientSessionInfo.getSessionID()));
            System.out.println("Authentication by QShield and get sessionKey: " + HexUtil.bytesToHexString(authClientSessionInfo.getSessionKey()));
            List<Channel> channels = authClientSessionInfo.getChannels();
            for (Channel channel : channels) {
                System.out.println("Authentication by QShield and get channel   : " + channel.remoteAddress());
            }
            successCount++;
        } else {
            System.out.println("session is NULL");
        }

        totalCount++;
        client.close();
    }
}
