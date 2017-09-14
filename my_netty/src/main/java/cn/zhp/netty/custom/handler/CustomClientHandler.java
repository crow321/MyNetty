package cn.zhp.netty.custom.handler;

import cn.qtec.key.kmip.field.KMIPBatch;
import cn.qtec.key.kmip.field.KMIPField;
import cn.qtec.key.kmip.kmipenum.EnumOperation;
import cn.qtec.key.kmip.types.KMIPByteString;
import cn.zhp.netty.custom.transport.entity.NettyMessage;
import cn.zhp.netty.custom.utils.TransportUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Description:
 *
 * @auth zhangp
 * @time 2017/9/6 17:02
 */
@Component
public class CustomClientHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(CustomClientHandler.class);
    @Autowired
    private TransportUtil transportUtil;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.debug("-------- 客户端···链路注册!");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("-------- 客户端···链路激活!");
//        byte[] req = "test client req".getBytes();
//        byteBuf = Unpooled.buffer(req.length);


        KMIPField requestField = new KMIPField();
        KMIPBatch batch = new KMIPBatch();
        batch.setOperation(EnumOperation.Create);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        logger.debug("----------------- batchItem ID: {}", uuid);
        batch.setUniqueBatchItemID(new KMIPByteString(uuid));

        requestField.addBatch(batch);
        requestField.calculateBatchCount();

        NettyMessage message = transportUtil.wrapKMIPFieldReq(requestField);
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("-------- 客户端···链路断开!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("-------- 客户端···接收到请求消息!");
        logger.debug("--------channelRead---\n服务器发来的数据为：{}",  ((ByteBuf) msg).readInt());

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("-------- 客户端···请求消息接处理完成!");
        ctx.close();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.debug("-------- 客户端···链路发送异常:{}", cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.debug("-------- 客户端···发生用户自定义事件 ");
        super.userEventTriggered(ctx, evt);
    }
}
