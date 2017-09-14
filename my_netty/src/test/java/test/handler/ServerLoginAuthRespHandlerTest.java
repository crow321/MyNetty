package test.handler;

import cn.zhp.netty.custom.transport.entity.NettyMessage;
import cn.zhp.netty.custom.transport.entity.NettyMessageHeader;
import cn.zhp.netty.custom.transport.enums.MessageTypeEnum;
import cn.zhp.netty.custom.transport.enums.ResultStatusEnum;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.BaseJunit4Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Created by zhangp on 2017/9/14.
 * @version v1.0.0
 */
public class ServerLoginAuthRespHandlerTest extends BaseJunit4Test {
    @Autowired
    private EmbeddedChannel channel;

    @Test
    public void channelRead() throws Exception {
        byte type = MessageTypeEnum.LOGIN_REQ;
        NettyMessage message = new NettyMessage();
        NettyMessageHeader header = new NettyMessageHeader();
        header.setMessageType(type);
        message.setHeader(header);

        /*************************************************************************************
         ****                         write bytes to inBound                               ***
         *************************************************************************************/
        assertTrue(channel.writeInbound(message));
        assertTrue(channel.finish());

        /*************************************************************************************
         ****                         read message inBound                                 ***
         *************************************************************************************/
        NettyMessage result = channel.readInbound();
        assertNotNull(result);
        assertEquals(ResultStatusEnum.SUCCESS, result.getBody());
        NettyMessageHeader resultHeader = result.getHeader();
        assertEquals(MessageTypeEnum.LOGIN_RESP, resultHeader.getMessageType());
    }
}