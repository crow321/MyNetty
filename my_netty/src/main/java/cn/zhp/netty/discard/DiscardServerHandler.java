package cn.zhp.netty.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * Description:
 * Handles a server-side channel.
 *
 * @auth zhangp
 * @time 2017/9/6 9:56
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        // Discard the received data silently.
        //((ByteBuf) msg).release();

        try {
            ByteBuf in = (ByteBuf) msg;
            while (in.isReadable()) {
                System.out.println((char) in.readByte());
                System.out.flush();
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
