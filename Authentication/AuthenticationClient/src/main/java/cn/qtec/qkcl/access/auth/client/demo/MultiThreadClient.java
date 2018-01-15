package cn.qtec.qkcl.access.auth.client.demo;

import cn.qtec.qkcl.access.auth.client.constant.AuthModeEnum;
import cn.qtec.qkcl.access.auth.client.entity.AccessAuthInfo;
import cn.qtec.qkcl.access.auth.client.entity.AuthClientSessionInfo;
import cn.qtec.qkcl.access.auth.client.netty.AuthenticationClient;
import cn.qtec.qkcl.access.auth.client.utils.HexUtil;
import cn.qtec.qkcl.kmip.transport.transfer.TransEnum;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Created by zhangp on 2017/12/22
 */
public class MultiThreadClient extends Thread {
    private static AtomicInteger count = new AtomicInteger(0);

    public MultiThreadClient() {

    }


    @Override
    public void run() {
        int num = count.incrementAndGet();
        this.setName("Thread-" + num);

        long startTime = System.currentTimeMillis();

        byte[] rootKeyId = new byte[16];
        byte[] rootKeyValue = new byte[32];
        byte[] quantumKey = new byte[32];

        //quantumKey
        byte[] quantumKeyPrefix = HexUtil.hexStringToBytes("e3879b3c98ce4a5386416c6a82afee0e7844656c7d8faa3c774455493067a1");
        System.arraycopy(quantumKeyPrefix, 0, quantumKey, 0, 31);
        quantumKey[31] = (byte) (num);

        //rootKeyValue
        byte[] rootKeyValuePrefix = HexUtil.hexStringToBytes("F04EC04626C75E119B5A923C68D2A879D539F8739F41EE4120B1247209F8AC");
        System.arraycopy(rootKeyValuePrefix, 0, rootKeyValue, 0, 31);
        rootKeyValue[31] = (byte) (num);

        //rootKeyId
        byte[] rootKeyIDPrefix = HexUtil.hexStringToBytes("01010101aa0101c1ab011101011111");
        System.arraycopy(rootKeyIDPrefix, 0, rootKeyId, 0, 15);
        rootKeyId[15] = (byte) (num);

        String deviceID = "Thread-" + num;
        //初始化接入认证消息
        AccessAuthInfo accessAuthInfo = new AccessAuthInfo();
        accessAuthInfo.setRemoteIP("192.168.90.233");
        accessAuthInfo.setPort(7700);
        accessAuthInfo.setAuthModeEnum(AuthModeEnum.LOCAL);
        accessAuthInfo.setTransEnum(TransEnum.TCP);
        accessAuthInfo.setQuantumKeyValue(quantumKey);
        accessAuthInfo.setDeviceID(deviceID.getBytes());
        accessAuthInfo.setEncryptionAlgorithm(1);
        accessAuthInfo.setRootKeyId(rootKeyId);
        accessAuthInfo.setRootKeyValue(rootKeyValue);

        AuthenticationClient client = new AuthenticationClient();
        AuthClientSessionInfo authClientSessionInfo = client.startAccessAuth(accessAuthInfo);
        if (authClientSessionInfo != null) {
            System.out.println("线程名:" + Thread.currentThread().getName() + " deviceID  : " + new String(authClientSessionInfo.getUsername()));
            System.out.println("线程名:" + Thread.currentThread().getName() + " sessionID : " + HexUtil.bytesToHexString(authClientSessionInfo.getSessionID()));
            System.out.println("线程名:" + Thread.currentThread().getName() + " sessionKey: " + HexUtil.bytesToHexString(authClientSessionInfo.getSessionKey()));
            /*List<Channel> channels = authClientSessionInfo.getChannels();
            for (Channel channel : channels) {
                System.out.println("线程名:" + Thread.currentThread().getName() + " channel   : " + channel.remoteAddress());
            }*/
        } else {
            System.out.println("***********************************************************************************************");
            System.out.println("线程名:" + Thread.currentThread().getName() + " session is NULL");
            System.out.println("***********************************************************************************************");
        }
        long endTime = System.currentTimeMillis();
        System.out.println(num + "-线程名:" + Thread.currentThread().getName() + " 结束, endTime:" + endTime + ", 共耗时:" + (endTime - startTime));
        System.out.println("-------------------------------------------------------------------------------------");
        client.close();
    }
}