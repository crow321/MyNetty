package cn.zhp.netty.discard;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:
 * Discards any incoming data.
 *
 * @auth zhangp
 * @time 2017/9/5 20:15
 */
public class DiscardServer {
    private final static Logger logger = LoggerFactory.getLogger(DiscardServer.class);

    private int port;

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //ServerBootstrap is a helper class that sets up a server.
            ServerBootstrap bootstrap = new ServerBootstrap();
            //The first one, often called 'boss', accepts an incoming connection.
            //The second one, often called 'worker', handles the traffic of the accepted connection once the boss accepts the connection and registers the accepted connection to the worker.
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //The ChannelInitializer is a special handler that is purposed to help a user configure a new Channel.
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    //option() is for the NioServerSocketChannel that accepts incoming connections. bossGroup
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //childOption() is for the Channels accepted by the parent ServerChannel, which is NioServerSocketChannel in this case. workerGroup
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            logger.debug("=====================Discard Server start and its port is " + port);

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8989;
        }
        new DiscardServer(port).run();
    }
}
