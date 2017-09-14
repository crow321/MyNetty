package cn.zhp.netty.custom.handler;

import cn.zhp.netty.custom.transport.entity.NettyMessage;
import cn.zhp.netty.custom.utils.TransportUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Description:
 *
 * @auth zhangp
 * @time 2017/9/6 16:45
 */
@Component
@Sharable
public class CustomServerHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(CustomServerHandler.class);
    @Autowired
    private TransportUtil transportUtil;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.debug("-------- 服务端···链路注册成功! 客户端地址: {}", ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        logger.debug("-------- 服务端···链路激活!");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("-------- 服务端···链路断开成功! 客户端地址: {}", ctx.channel().remoteAddress().toString());
//        logger.debug("--------------------- E -- N -- D -------------------------------------------------------------\n");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("----------  服务端···接收请求消息!");
        /*ByteBuf buf = (ByteBuf) msg;
        logger.debug("----------  服务端···开始解析消息 ====>");
        KMIPField field = transportUtil.unpackByteBufRequest(buf);
//        logger.debug("================ 收到客户端发送的数据: {}", field);
        ctx.channel().write(Unpooled.copyInt(1));*/
        NettyMessage message = (NettyMessage) msg;
        byte[] body = (byte[]) message.getBody();
        NettyMessage req = transportUtil.unpacketKMIPReq(body);

//        logger.debug("================ 收到客户端发送的数据: {}", message);
        logger.debug("================ 收到客户端发送的数据: {}", req);
        ctx.channel().write(message);

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("-------- 服务端···接收消息处理完成!");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.debug("-------- 服务端···链路异常:{}", cause.getMessage());
        ctx.close();
    }
}
