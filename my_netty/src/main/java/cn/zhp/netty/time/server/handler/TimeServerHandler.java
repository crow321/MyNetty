package cn.zhp.netty.time.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Description:
 *
 * @auth zhangp
 * @time 2017/9/6 11:30
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(TimeServerHandler.class);

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.debug("### Time Server ###  channel registered...");
        super.channelRegistered(ctx);
    }

    /**
     * the channelActive() method will be invoked when a connection is established and ready to generate traffic.
     * Let's write a 32-bit integer that represents the current time in this method.
     * @param ctx
     * @throws Exception
     */
    /*@Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        logger.debug("######## channel active...");
        *//*final ByteBuf time = ctx.alloc().buffer(4);
        time.writeByte((int) (System.currentTimeMillis() / 1000L + 2208988800L));

        final ChannelFuture future = ctx.writeAndFlush(time);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                assert future == channelFuture;
                ctx.close();
            }
        });*//*
    }*/

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        //将缓冲区 buf 的字节数组复制到 req 中
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        logger.debug("### Time Server ^_^=================>> read receive order: {}", body);

        // 返回给客户端的消息
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("### Time Server ### read completed...");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage());
        ctx.close();
    }
}
