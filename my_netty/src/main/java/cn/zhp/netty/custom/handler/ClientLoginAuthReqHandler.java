package cn.zhp.netty.custom.handler;

import cn.zhp.netty.custom.transport.entity.NettyMessage;
import cn.zhp.netty.custom.transport.entity.NettyMessageHeader;
import cn.zhp.netty.custom.transport.enums.MessageTypeEnum;
import cn.zhp.netty.custom.transport.enums.ResultStatusEnum;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Description:
 *          进行登录认证
 * @author Created by zhangp on 2017/9/13.
 * @version v1.0.0
 */
@Component
@Sharable
public class ClientLoginAuthReqHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClientLoginAuthReqHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("------------ 进行登录认证");
        //进行单元测试时不能使用writeAndFlush, 无法触发ChannelRead()，导致返回null
        //ctx.writeAndFlush(buildLoginReq());
        //fireChannelRead()触发下一个inHandler的channelRead()方法
        ctx.fireChannelRead(buildLoginReq());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("------------ 接收服务端认证响应结果");
        NettyMessage message = (NettyMessage) msg;
        if (message.getHeader() != null && message.getHeader().getMessageType() == MessageTypeEnum.LOGIN_RESP) {
            byte loginResult = (byte) message.getBody();
            if (loginResult != 0) {
                logger.error("=========================>>>>>>>>> 认证失败！");
                ctx.close();
            }
            logger.debug("Login is OK, message: {}", message);
            logger.debug("=========================>>>>>>>>> 登录认证成功！");
        }
        //fireChannelRead()触发下一个inHandler的channelRead()方法
//        ctx.fireChannelRead(message);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("------------channelReadComplete");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("=========================>>>>>>>>> 认证失败！");
        logger.error("------------ LoginAuthReqHandler error: {}", cause.getMessage());
        ctx.close();
    }

    private NettyMessage buildLoginReq() {
        NettyMessage message = new NettyMessage();
        NettyMessageHeader header = new NettyMessageHeader();
        header.setMessageType(MessageTypeEnum.LOGIN_REQ);
        message.setHeader(header);
        message.setBody("loginReq");
        return message;
    }
}
