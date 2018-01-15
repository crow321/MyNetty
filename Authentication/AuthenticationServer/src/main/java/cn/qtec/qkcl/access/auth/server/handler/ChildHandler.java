package cn.qtec.qkcl.access.auth.server.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Created by zhangp
 *         2017/11/7
 */
@Component
public class ChildHandler extends ChannelInitializer<SocketChannel> {
    private static final int MAX_FRAME_LENGTH = 1024;
    private static final int LENGTH_FIELD_OFFSET = 2;
    private static final int LENGTH_FIELD_LENGTH = 2;

    @Autowired
    private AuthenticationServerHandler serverHandler;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH));
        ch.pipeline().addLast(serverHandler);
    }
}
