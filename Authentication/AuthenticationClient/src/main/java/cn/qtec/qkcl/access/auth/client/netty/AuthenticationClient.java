package cn.qtec.qkcl.access.auth.client.netty;

import cn.qtec.qkcl.access.auth.client.entity.AccessAuthInfo;
import cn.qtec.qkcl.access.auth.client.entity.AuthClientSessionInfo;
import cn.qtec.qkcl.access.auth.client.message.TransportMessage;
import cn.qtec.qkcl.access.auth.client.process.AbstractAuthClientProcessor;
import cn.qtec.qkcl.access.auth.client.process.impl.LocalAuthProcessor;
import cn.qtec.qkcl.access.auth.client.process.impl.QShieldAuthProcessor;
import cn.qtec.qkcl.access.auth.client.utils.PacketUtil;
import cn.qtec.qkcl.kmip.transport.transfer.AddressInfo;
import cn.qtec.qkcl.kmip.transport.transfer.TransEnum;
import cn.qtec.qkcl.kmip.transport.transfer.TransportInterface;
import cn.qtec.qkcl.kmip.transport.transfer.impl.HttpTransport;
import cn.qtec.qkcl.kmip.transport.transfer.impl.TcpTransport;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AuthenticationClient {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationClient.class);

    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 5000;
    private TransportInterface transportInterface;
    private AbstractAuthClientProcessor clientProcessor;
    private List<Channel> channels;

    public AuthenticationClient() {

    }

    public synchronized AuthClientSessionInfo startAccessAuth(AccessAuthInfo initInfo) {
        if (initInfo == null) {
            logger.error("Start access auth failed, accessAuthInfo can't be NULL");
            return null;
        }
        //logger.debug(initInfo.toString());
        init(initInfo, initInfo.getTransEnum());

        TransportMessage startAuthMessage = clientProcessor.generateStartAuthRequest(initInfo);
        ByteBuf startAuthByteBuf = PacketUtil.wrapTransportMessage(startAuthMessage);

        if (startAuthByteBuf != null) {
            createChannel(initInfo);
            if (channels != null) {
                try {
                    transportInterface.sendMessageByChannel(startAuthByteBuf, channels);
                    logger.info("客户端开始接入认证...");
                    AuthClientSessionInfo sessionInfo = clientProcessor.getSessionInfo(DEFAULT_CONNECT_TIMEOUT_MILLIS, startAuthMessage.getDeviceID());
                    if (sessionInfo != null) {
                        logger.info("客户端接入认证成功!");
                        sessionInfo.setChannels(channels);

                        //监控channel是否被关闭
                        new TimerTask(channels).start();

                        return sessionInfo;
                    } else {
                        logger.error("客户端接入认证失败!");
                    }
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        logger.error("The client get sessionInfo timeout, e: {}!", e.getMessage());
                    } else {
                        logger.error("The client send auth message error, cause: {}!", e.getCause());
                    }
                }
                this.close();
            }
        }
        return null;
    }

    private void createChannel(AccessAuthInfo initInfo) {
        String remoteIP = initInfo.getRemoteIP();
        int remotePort = initInfo.getPort();
        if (remoteIP == null || "".equals(remoteIP) || remotePort == 0) {
            throw new IllegalArgumentException("Create channel arguments error, remoteIP:" + remoteIP + ", remotePort:" + remotePort);
        }

        AddressInfo addressInfo = new AddressInfo(remoteIP, remotePort, initInfo.getTransEnum());
        List<AddressInfo> list = new ArrayList<>();
        list.add(addressInfo);
        try {
            this.channels = transportInterface.createChannels(list);
        } catch (Exception e) {
            //如果客户端创建channel时失败，尝试重连
            if (clientProcessor != null) {
                initTransport(addressInfo.getTransType());
                try {
                    this.channels = transportInterface.createChannels(list);
                } catch (Exception e1) {
                    logger.error("Client attempt to create channel failed, error:{}", e1.getMessage());
                }
            }
        }
    }

    public void close() {
        if (channels != null) {
            for (Channel channel : channels) {
                logger.debug("Close the client channel:{}", channel.localAddress());
                if (!channel.eventLoop().isShutdown()) {
                    channel.eventLoop().shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).syncUninterruptibly();
                }
            }
        } else {
            System.exit(0);
        }
    }

    private void init(AccessAuthInfo accessAuthInfo, TransEnum transEnum) {
        switch (accessAuthInfo.getAuthModeEnum()) {
            case Q_SHIELD:
                clientProcessor = new QShieldAuthProcessor();
                break;

            case LOCAL:
            default:
                clientProcessor = new LocalAuthProcessor();
                break;
        }

        initTransport(transEnum);
    }

    private void initTransport(TransEnum transEnum) {
        if (transEnum == null) {
            logger.error("The accessAuthInfo transEnum is NULL");
            return;
        }

        switch (transEnum) {
            case HTTP:
                transportInterface = new HttpTransport();
                break;

            case TCP:
            default:
                transportInterface = new TcpTransport();
                break;
        }
    }
}
