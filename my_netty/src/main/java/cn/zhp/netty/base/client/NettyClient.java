package cn.zhp.netty.base.client;

import cn.zhp.netty.custom.handler.codec.NettyMessageDecoder;
import cn.zhp.netty.custom.handler.codec.NettyMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Description:
 *
 * @auth zhangp
 * @time 2017/9/6 13:11
 */
public class NettyClient {
    private final static Logger logger = LoggerFactory.getLogger(NettyClient.class);
    @Value(value = "${local_host}")
    private String host;
    @Value(value = "${netty_default_port}")
    private int port;
    private ArrayList<ChannelHandler> childChannelHandlers = new ArrayList<>();
    //客户端重连使用
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public NettyClient() {

    }

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        //配置客户端NIO线程组
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, false)
                    .handler(new ChildChannelHandlers());

            //异步连接操作
            ChannelFuture future = bootstrap.connect(host, port).sync();

            logger.debug("================================== Netty Client start on {}", port);
            logger.debug("================================== Waiting for connect...");

            // Wait until the connection is closed.
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();

            /**
            * 以下内容为客户端进行重连代码
            * */
            //释放所有资源后，清空资源，再次发起重连操作
           /* executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //睡眠 5s
                        TimeUnit.SECONDS.sleep(5);
                        //发起重连操作
                        start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });*/
        }
    }

    public void addChildChannelHandler(ChannelHandler childChannelHandler) {
        childChannelHandlers.add(childChannelHandler);
    }

    private class ChildChannelHandlers extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {

            socketChannel.pipeline().addLast(new NettyMessageDecoder());
            socketChannel.pipeline().addLast(new NettyMessageEncoder());
            for (ChannelHandler childChannelHandler : childChannelHandlers) {
                socketChannel.pipeline().addLast(childChannelHandler);
            }
        }
    }
}
