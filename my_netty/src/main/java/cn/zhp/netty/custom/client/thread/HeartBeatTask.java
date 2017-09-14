package cn.zhp.netty.custom.client.thread;

import cn.zhp.netty.custom.transport.entity.NettyMessage;
import cn.zhp.netty.custom.transport.entity.NettyMessageHeader;
import cn.zhp.netty.custom.transport.enums.MessageTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by zhangp on 2017/9/13.
 * @version v1.0.0
 */
public class HeartBeatTask implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(HeartBeatTask.class);
    private final ChannelHandlerContext ctx;

    public HeartBeatTask(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        NettyMessage heartBeatReq = buildHeartBeat();
        logger.debug("·················· > Heart Beat Task ：{}", heartBeatReq.getHeader().getMessageType());
        ctx.writeAndFlush(heartBeatReq);
    }

    /**
     * 只包含消息类型 MessageTypeEnum.HEART_BEAT_REQ
     * @return
     */
    private NettyMessage buildHeartBeat() {
        NettyMessage message = new NettyMessage();
        NettyMessageHeader header = new NettyMessageHeader();
        header.setMessageType(MessageTypeEnum.HEART_BEAT_REQ);
        message.setHeader(header);
        return message;
    }
}
