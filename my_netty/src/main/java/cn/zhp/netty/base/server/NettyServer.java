package cn.zhp.netty.base.server;

import cn.zhp.netty.custom.handler.codec.NettyMessageDecoder;
import cn.zhp.netty.custom.handler.codec.NettyMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Description:
 *
 * @auth zhangp
 * @time 2017/9/6 14:54
 */
@Component
public class NettyServer {
    private final static Logger logger = LoggerFactory.getLogger(NettyServer.class);
    @Value(value = "${netty_default_port}")
    private int port;
    private ArrayList<ChannelHandler> childChannels = new ArrayList<>();

    public NettyServer() {
    }

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChildChannelHandler());

            //绑定端口，同步等待成功
            ChannelFuture f = b.bind(port).sync();

            logger.debug("================================== Netty服务器启动, 端口: {}", port);

            //等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        }finally {
            //优雅退出
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public ArrayList<ChannelHandler> getChildChannels() {
        return childChannels;
    }

    public void setChildChannels(ArrayList<ChannelHandler> childChannels) {
        this.childChannels = childChannels;
    }

    public void addChildChannel(ChannelHandler childChannel) {
        childChannels.add(childChannel);
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
//            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("$_".getBytes())));
//            socketChannel.pipeline().addLast(new StringDecoder());

            socketChannel.pipeline().addLast(new NettyMessageDecoder());
            socketChannel.pipeline().addLast(new NettyMessageEncoder());

            for (ChannelHandler childChannel : childChannels) {
                socketChannel.pipeline().addLast(childChannel);
            }
        }
    }
}
