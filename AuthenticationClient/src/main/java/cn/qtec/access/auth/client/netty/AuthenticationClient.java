package cn.qtec.access.auth.client.netty;

import cn.qtec.access.auth.client.constant.EncryptionAlgorithm;
import cn.qtec.access.auth.client.entity.AccessAuthInfo;
import cn.qtec.access.auth.client.entity.SessionInfo;
import cn.qtec.access.auth.client.handler.AuthenticationClientHandler;
import cn.qtec.access.auth.client.message.AuthenticationMessage;
import cn.qtec.access.auth.client.process.impl.ProcessImpl;
import cn.qtec.access.auth.client.utils.JniUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AuthenticationClient {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationClient.class);

    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 5000;
    private static final int DEFAULT_LATCH_COUNT = 1;
    private static final AuthenticationClient INSTANCE = new AuthenticationClient();

    private AuthenticationClientHandler clientHandler;
    private CountDownLatch latch;
    private EventLoopGroup eventLoopGroup;
    private Channel channel;

    private AuthenticationClient() {
        clientHandler = new AuthenticationClientHandler();
        eventLoopGroup = new NioEventLoopGroup();

        resetLatch();
    }

    public static AuthenticationClient getInstance() {
        return INSTANCE;
    }

    public SessionInfo startAccessAuth(String host, int port) {
        try {
            JniUtil.addLibraryDir(JniUtil.LIBRARY_PATH_WINDOW_x64);
            return INSTANCE.connectAndAuthentication(host, port);
        } catch (Exception e) {
            logger.error("Failed to load java.library.path error! e:{}", e.getMessage());
            return null;
        }
    }

    public SessionInfo startAccessAuth(String host, int port, AccessAuthInfo accessAuthInfo) {
        if (accessAuthInfo != null) {
            byte[] deviceID = accessAuthInfo.getDeviceID();
            byte[] quantumKey = accessAuthInfo.getQuantumKeyValue();

            if (deviceID == null) {
                logger.error("DeviceId should not be NULL!");
                return null;
            }
            if (quantumKey == null) {
                logger.error("QuantumKeyValue should not be NULL!");
                return null;
            }
            byte[] rootKeyId = accessAuthInfo.getRootKeyId();
            byte[] rootKeyValue = accessAuthInfo.getRootKeyValue();
            int encryptionAlgorithm = accessAuthInfo.getEncryptionAlgorithm();

            AuthenticationMessage authenticationMessage = new AuthenticationMessage();

            int length = deviceID.length + 32;
            switch (encryptionAlgorithm) {
                case EncryptionAlgorithm.AES:
                    if (rootKeyId == null) {
                        logger.error("RootKeyId should not be NULL!");
                        return null;
                    }
                    if (rootKeyValue == null) {
                        logger.error("RootKeyValue should not be NULL!");
                        return null;
                    }

                    authenticationMessage.setEncryptionKeyID(rootKeyId);
                    length = length % 16 == 0 ? length : (length / 16 + 1) * 16;
                    authenticationMessage.setMessageLength(length);
                    break;

                default:
                    authenticationMessage.setMessageLength(length);
                    break;
            }

            authenticationMessage.setVersion(1);
            authenticationMessage.setEncryptionAlgorithm(encryptionAlgorithm);
            authenticationMessage.setDeviceIDLength(deviceID.length);
            authenticationMessage.setDeviceID(deviceID);

            ProcessImpl.initAuthMessage(authenticationMessage, accessAuthInfo.getRootKeyValue(), quantumKey);

            return connectAndAuthentication(host, port);
        }
        logger.error("Local access authentication init FAILED, accessAuthInfo is NULL!");
        return null;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }

    private SessionInfo connectAndAuthentication(final String host, final int port) {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 2, 2))
                                    .addLast(clientHandler);
                        }
                    });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            if (future.isSuccess()) {
                logger.info("Authentication client is connecting to {}:{}", host, port);
                channel = future.channel();

                //默认超时时间为5000毫秒
                latch.await(DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

                return (SessionInfo) getResultObject();
            }
        } catch (InterruptedException e) {
            logger.error("Authentication client startup error:{}", e.getMessage());
        }
        logger.error("Authentication client startup failure!");
        close();
        return null;
    }

    public Object sendMessageSync(ByteBuf byteBuf, long timeout) {
        try {
            resetLatch();
            channel.writeAndFlush(byteBuf);

            latch.await(timeout, TimeUnit.MILLISECONDS);
            return getResultObject();
        } catch (InterruptedException e) {
            logger.error("the current thread is interrupted while waiting, e:{}", e.getMessage());
            return null;
        }
    }

    private Object getResultObject() {
        return clientHandler.getObject();
    }

    private void resetLatch() {
        latch = new CountDownLatch(DEFAULT_LATCH_COUNT);
        clientHandler.resetLatch(latch);
    }
}
