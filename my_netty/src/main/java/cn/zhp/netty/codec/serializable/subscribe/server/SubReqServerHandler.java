package cn.zhp.netty.codec.serializable.subscribe.server;

import cn.zhp.netty.codec.serializable.subscribe.message.SubscribeReq;
import cn.zhp.netty.codec.serializable.subscribe.message.SubscribeResp;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by zhangp on 2017/9/21.
 * @version v1.0.0
 */
@ChannelHandler.Sharable
public class SubReqServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SubReqServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SubscribeReq req = (SubscribeReq) msg;
        if ("test".equalsIgnoreCase(req.getUserName())) {
            logger.debug("=============== > Server accept client subscribe req:\n\t{}", req);
            ctx.writeAndFlush(resp(req.getSubReqID()));
        }
    }

    private SubscribeResp resp(int subReqID) {
        SubscribeResp resp = new SubscribeResp();
        resp.setSubReqID(subReqID);
        resp.setRespCode(0);
        resp.setDesc("Netty book order succeed, 3 days later, sent to the designated address!");
        return resp;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("xxxxxxxxxxxxxxxxxxxxxxxx 连接异常");
        ctx.close();
    }
}
