package cn.zhp.netty.time.codec.encoder;

import cn.zhp.netty.time.pojo.UnixTime;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Description:
 * translates a UnixTime back into a ByteBuf.
 *
 * @auth zhangp
 * @time 2017/9/6 14:20
 */
/*public class TimeEncoder extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        UnixTime m = (UnixTime) msg;
        ByteBuf encoded = ctx.alloc().buffer(4);
        encoded.writeInt((int) m.getValue());
        ctx.write(encoded, promise); //(1)
    }
}*/
//To simplify even further, you can make use of MessageToByteEncoder:
public class TimeEncoder extends MessageToByteEncoder<UnixTime> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, UnixTime unixTime, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt((int) unixTime.getValue());
    }
}

