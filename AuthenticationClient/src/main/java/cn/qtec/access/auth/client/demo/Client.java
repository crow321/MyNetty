package cn.qtec.access.auth.client.demo;

import cn.qtec.access.auth.client.constant.EncryptionAlgorithm;
import cn.qtec.access.auth.client.entity.AccessAuthInfo;
import cn.qtec.access.auth.client.entity.SessionInfo;
import cn.qtec.access.auth.client.netty.AuthenticationClient;
import cn.qtec.access.auth.client.utils.HexUtil;
import io.netty.channel.socket.SocketChannel;

/**
 * @author Created by zhangp
 *         2017/11/16
 */
public class Client {

    public static void main(String[] args) {
        byte[] deviceID = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        byte[] quantumKey = HexUtil.hexStringToBytes("0202020202020202020202020605040102020202020202020202020206050401");
        byte[] rootKeyId = HexUtil.hexStringToBytes("01010101010101010101010101010103");
        byte[] rootKeyValue = HexUtil.hexStringToBytes("0202020202020202020202020605040102020202020202020202020206050401");

        //初始化接入认证消息
        long startTime = System.currentTimeMillis();

        AuthenticationClient client = AuthenticationClient.getInstance();

        //方式一 使用Q盾进行接入认证
        //SessionInfo sessionInfo = client.startAccessAuth("localhost", 7700);

        //方式二 使用本地进行接入认证
        AccessAuthInfo accessAuthInfo = new AccessAuthInfo(EncryptionAlgorithm.AES, rootKeyId, rootKeyValue, deviceID, quantumKey);
        SessionInfo sessionInfo = client.startAccessAuth("localhost", 7700, accessAuthInfo);

        long diffTime = System.currentTimeMillis() - startTime;
        System.out.println("客户端接入认证总耗时: " + diffTime + "ms");

        if (sessionInfo != null) {
            System.out.println("=================> sessionID    : " + HexUtil.bytesToHexString(sessionInfo.getSessionID()));
            System.out.println("=================> sessionKey   : " + HexUtil.bytesToHexString(sessionInfo.getSessionKey()));

            SocketChannel channel = sessionInfo.getSocketChannel();
            System.out.println("channel:" + channel.remoteAddress());
        }

        //关闭channel
        client.close();
    }

}
