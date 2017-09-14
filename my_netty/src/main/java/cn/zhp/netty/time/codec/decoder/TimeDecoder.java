package cn.zhp.netty.time.codec.decoder;

import cn.zhp.netty.time.pojo.UnixTime;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Description:
 *      TimeDecoder which deals with the fragmentation issue,
 *
 * @auth zhangp
 * @time 2017/9/6 13:57
 */
public class TimeDecoder extends ByteToMessageDecoder{

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            return;
        }

        list.add(new UnixTime(byteBuf.readUnsignedInt()));
    }
}
