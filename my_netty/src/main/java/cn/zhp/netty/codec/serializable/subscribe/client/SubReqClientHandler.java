package cn.zhp.netty.codec.serializable.subscribe.client;

import cn.zhp.netty.codec.serializable.subscribe.message.SubscribeReq;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by zhangp on 2017/9/21.
 * @version v1.0.0
 */
public class SubReqClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SubReqClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //客户端发送10条订购请求
        for (int i = 0; i < 10; i++) {
            ctx.write(subreq(i));
        }
        ctx.flush();
    }

    private SubscribeReq subreq(int i) {
        SubscribeReq req = new SubscribeReq();
        req.setAddress("杭州市萧山区");
        req.setSubReqID(i);
        req.setPhoneNumber("1234567890");
        req.setProductName("Netty 权威指南");
        req.setUserName("test");
        return req;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("================ > client Receive server response:\n\t{}", msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("xxxxxxxxxxxxxxxxxxxxx 连接异常");
        ctx.close();
    }
}
