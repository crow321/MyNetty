package cn.qtec.qkcl.access.auth.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Created by zhangp on 2017/9/29.
 */
@Sharable
@Component
public class AuthenticationServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServerHandler.class);

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        logger.debug("========> 客户端({})正在连接...   ", channel.remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /*ByteBuf byteBuf = (ByteBuf) msg;
        int length = byteBuf.readableBytes();
        logger.debug("收到客户端消息----------长度   : {}", length);
        //模拟报文解析
        long startTime = System.currentTimeMillis();
        MessageObject receiveMessage = packetTools.unwrapBaseMessage(byteBuf);

        //处理并响应
        MessageObject responseMessage = process.process(receiveMessage);

        //模拟报文封装
        ByteBuf resp = packetTools.wrapBaseMessage(responseMessage);

        if (resp != null) {
            ChannelFuture channelFuture = ctx.writeAndFlush(resp);

            long diffTime = System.currentTimeMillis() - startTime;
            logger.info("服务端响应客户端报文，共耗时    : {}ms", diffTime);
            if (channelFuture.isSuccess()) {
                logger.debug("向客户端发送消息成功, 消息长度  : {}", resp.readableBytes());
                if (process.isAuthSuccess()) {
                    logger.debug("客户端({})认证成功!", ctx.channel().remoteAddress());
                }
            }
        }*/
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        logger.debug("Server reads client({}) data completely.", channel.remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("The client({}) has left...", ctx.channel().remoteAddress());
        /*process.removeSessionKeyBySessionID(sessionID);
        byte[] sessionKey = process.getSessionKey(this.sessionID);
        if (sessionKey == null) {
            logger.debug("The sessionKey has remove!");
        }*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Socket Error:{}", cause.getLocalizedMessage());
        cause.printStackTrace();
    }

}
