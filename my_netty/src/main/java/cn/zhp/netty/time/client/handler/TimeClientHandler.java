package cn.zhp.netty.time.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:
 *
 * @auth zhangp
 * @time 2017/9/6 13:40
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(TimeClientHandler.class);
    private byte[] req;
    private int counter;

    public TimeClientHandler() {
        req = ("QUERY TIME ORDER" + System.getProperty("line.separator")).getBytes();

    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.debug("*** Time Client *** channel registered ");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf message = null;
        logger.debug("*** Time Client *** channel active ");
        for (int i = 0; i < 100; i++) {
            message = Unpooled.buffer(req.length);
            message.writeBytes(req);
            ctx.writeAndFlush(message);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       /* ByteBuf m = (ByteBuf) msg;
        try {
            long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println("+++++++++++ " + new Date(currentTimeMillis));
            ctx.close();
        } finally {
            m.release();
        }*/

        //With the updated decoder, the TimeClientHandler does not use ByteBuf anymore:
       /* UnixTime m = (UnixTime) msg;
        System.out.println(m);
        ctx.close();*/

        /*ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        logger.debug("================> Now is: {}; the counter is {}", body, ++counter);
        ctx.close();*/
        String body = (String) msg;
        logger.debug("================> Now is: {}; the counter is {}", body, ++counter);
//        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("************************** error:{}", cause.getMessage());
        ctx.close();
    }
}
