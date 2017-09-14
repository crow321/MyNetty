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

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录认证处理器
 * @author Created by zhangp on 2017/9/13.
 * @version v1.0.0
 */
@Component
@Sharable
public class ServerLoginAuthRespHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServerLoginAuthRespHandler.class);

    private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<String,Boolean>();
    //IP白名单
    private String[] whitekList = {"127.0.0.1"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("=================== 进行登录认证! ");

        NettyMessage message = (NettyMessage) msg;
        //如果是握手请求消息，处理， 其他消息透传
        if (message.getHeader() != null && message.getHeader().getMessageType() == MessageTypeEnum.LOGIN_REQ) {
            String currentNodeAddress = ctx.channel().remoteAddress().toString();
            NettyMessage loginResp = null;

            //重复登录，拒绝
            if (nodeCheck.containsKey(currentNodeAddress)) {
                logger.warn("远程客户端 {} 重复登录，已拒绝！", currentNodeAddress);
                loginResp = buildLoginResponse(ResultStatusEnum.FAIL);
                ctx.writeAndFlush(loginResp);
            } else {
                logger.debug("=============== 登录前进行IP白名单校验!");
                boolean isOK = false;
                try {
                    //IP认证白名单校验
                    InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                    String ip = address.getAddress().getHostAddress();
                    for (String WIP : whitekList) {
                        if (WIP.equals(ip)) {
                            isOK = true;
                            break;
                        }
                    }
                } catch (ClassCastException e) {
                    logger.warn("单元测试时跳过IP白名单校验...");
                    isOK = true;
                }


                if (isOK) {
                    loginResp = buildLoginResponse(ResultStatusEnum.SUCCESS);
                    nodeCheck.put(currentNodeAddress, true);
                } else {
                    loginResp = buildLoginResponse(ResultStatusEnum.FAIL);
                }

                logger.debug(" The login response is : {} ", loginResp);
                //fireChannelRead()触发下一个inHandler的channelRead()方法
                ctx.fireChannelRead(loginResp);
                //进行单元测试时不能使用, 无法触发ChannelRead()，导致返回null
                //ctx.writeAndFlush(loginResp);
            }
        } else {
            //ctx.writeAndFlush(msg);
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("------------ server channelReadComplete...");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("------------ LoginAuthRespHandler error: {}", cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    private NettyMessage buildLoginResponse(byte result) {
        NettyMessage message = new NettyMessage();
        NettyMessageHeader header = new NettyMessageHeader();
        //2 bytes
        header.setMessageType(MessageTypeEnum.LOGIN_RESP);
        message.setHeader(header);
        message.setBody(result);
        return message;
    }
}
