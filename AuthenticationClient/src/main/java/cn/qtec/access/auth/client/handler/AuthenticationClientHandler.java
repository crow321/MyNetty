package cn.qtec.access.auth.client.handler;

import cn.qtec.access.auth.client.entity.SessionInfo;
import cn.qtec.access.auth.client.message.TransportMessage;
import cn.qtec.access.auth.client.process.impl.ProcessImpl;
import cn.qtec.access.auth.client.utils.PacketUtil;
import cn.qtec.access.auth.client.constant.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author Created by zhangp on 2017/9/29.
 */
@Sharable
public class AuthenticationClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationClientHandler.class);

    private ProcessImpl process;
    private CountDownLatch latch;
    private Object object;

    public AuthenticationClientHandler() {
        this.process = new ProcessImpl();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        TransportMessage transportMessage = process.generateStartAuthRequest();
        ByteBuf startAuthRequest = PacketUtil.wrapMessage(transportMessage);
        if (startAuthRequest != null) {
            ctx.writeAndFlush(startAuthRequest);
            logger.info("The client send authentication request(length: {} bytes) SUCCESS!", startAuthRequest.readableBytes());
        } else {
            logger.error("The client generates authentication request FAILED!");
            latch.countDown();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        TransportMessage responseMessage = PacketUtil.unwrapBinaryToMessage(byteBuf);
        if (responseMessage != null) {
            int messageType = responseMessage.getMessageType();
            logger.info("The client receives response message with message type {}.", messageType);
            switch (messageType) {
                case MessageType.ACCESS_AUTH_RESP:
                case MessageType.SERVER_PROOF:
                    TransportMessage transportMessage = process.processAuthMessage(responseMessage);
                    if (transportMessage == null) {
                        return;
                    }

                    int messageLength = transportMessage.getMessageLength();
                    logger.info("messageLength :{}", messageLength);
                    switch (messageLength) {
                        case 0:
                            logger.error("客户端接入认证失败!");
                            break;

                        case 4:
                            SessionInfo sessionInfo = process.getSessionInfo();
                            sessionInfo.setSocketChannel((SocketChannel) ctx.channel());
                            this.object = sessionInfo;
                            latch.countDown();

                            logger.info("客户端接入认证成功!");
                            break;

                        default:
                            ByteBuf response = PacketUtil.wrapMessage(transportMessage);
                            if (response != null) {
                                ctx.writeAndFlush(response).sync();
                                logger.info("The client send second authentication request(length: {} bytes) SUCCESS!", response.readableBytes());
                            }
                            break;
                    }
                    break;

                default:
                    logger.error("Received unknown message type of {}", messageType);
                    break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        latch.countDown();
        ctx.close();
    }

    public void resetLatch(CountDownLatch initLatch) {
        this.latch = initLatch;
    }

    public Object getObject() {
        return object;
    }
}
