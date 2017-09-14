package cn.zhp.netty.custom.handler;

import cn.zhp.netty.custom.client.thread.HeartBeatTask;
import cn.zhp.netty.custom.transport.entity.NettyMessage;
import cn.zhp.netty.custom.transport.entity.NettyMessageHeader;
import cn.zhp.netty.custom.transport.enums.MessageTypeEnum;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by zhangp on 2017/9/13.
 * @version v1.0.0
 */
@Component
@Sharable
public class ClientHeartBeatReqHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(ClientHeartBeatReqHandler.class);

    private volatile ScheduledFuture<?> heartBeat;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        NettyMessageHeader header = message.getHeader();
        /**
         * 若握手成功，则定期发送心跳消息；
         * 心跳定时器单位是毫秒， 默认是 5000， 即每5秒发送一条消息
         */
        if (header != null && header.getMessageType() == MessageTypeEnum.LOGIN_RESP) {
            logger.debug("·························> 检测心跳 ·······>>>>>>");
            heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx), 20000, 5000, TimeUnit.MILLISECONDS);
        }
        /**
         * 接收服务端发送的心跳应答消息
         */
        else if (header != null && header.getMessageType() == MessageTypeEnum.HEART_BEAT_RESP) {
            logger.debug("·························> Client receive server heart beat message : {}", message);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (heartBeat != null) {
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }
}
