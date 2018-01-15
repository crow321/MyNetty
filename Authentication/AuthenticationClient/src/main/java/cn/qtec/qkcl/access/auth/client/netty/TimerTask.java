package cn.qtec.qkcl.access.auth.client.netty;

import io.netty.channel.Channel;
import io.netty.channel.EventLoop;

import java.util.List;

/**
 * @author Created by zhangp on 2017/12/21
 */
public class TimerTask extends Thread {
    private List<Channel> channels;

    TimerTask(List<Channel> channels) {
        this.channels = channels;
        super.setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(2000);
                for (Channel channel : channels) {
                    boolean registered = channel.isRegistered();
                    if (!registered) {
                        EventLoop eventLoop = channel.eventLoop();
                        if (!eventLoop.isShutdown()) {
                            eventLoop.shutdownGracefully();
                        }
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
