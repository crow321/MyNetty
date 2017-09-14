package cn.zhp.netty.custom.handler;

import cn.zhp.netty.custom.transport.entity.NettyMessage;
import cn.zhp.netty.custom.transport.entity.NettyMessageHeader;
import cn.zhp.netty.custom.transport.enums.MessageTypeEnum;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Created by zhangp on 2017/9/13.
 * @version v1.0.0
 */
@Component
@Sharable
public class ServerHeartBeatRespHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientLoginAuthReqHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        NettyMessageHeader header = message.getHeader();
        //若是心跳请求，则返回心跳响应，其他消息透传
        //返回心跳应答消息
        if (header != null && header.getMessageType() == MessageTypeEnum.HEART_BEAT_REQ) {
            NettyMessage heartBeatResp = buildHeartBeatResp();
            logger.debug("Server Heart Beat Response message: {}", heartBeatResp);
            ctx.writeAndFlush(heartBeatResp);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    private NettyMessage buildHeartBeatResp() {
        NettyMessage message = new NettyMessage();
        NettyMessageHeader header = new NettyMessageHeader();
        header.setMessageType(MessageTypeEnum.HEART_BEAT_RESP);
        message.setHeader(header);
        return message;
    }
}
