package test.handler;

import cn.zhp.netty.custom.transport.entity.NettyMessage;
import cn.zhp.netty.custom.transport.entity.NettyMessageHeader;
import cn.zhp.netty.custom.transport.enums.MessageTypeEnum;
import cn.zhp.netty.custom.transport.enums.ResultStatusEnum;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.BaseJunit4Test;

import static org.junit.Assert.*;

/**
 * @author Created by zhangp on 2017/9/14.
 * @version v1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring-context.xml")
public class ClientLoginAuthReqHandlerTest extends BaseJunit4Test {
    @Autowired
    private EmbeddedChannel channel;

    @Test
    public void testChannelRead() {
        byte type = MessageTypeEnum.LOGIN_RESP;
        byte status = ResultStatusEnum.SUCCESS;
        NettyMessage message = new NettyMessage();
        NettyMessageHeader header = new NettyMessageHeader();
        header.setMessageType(type);
        message.setHeader(header);
        //body: 0
        message.setBody(status);

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
        assertEquals("loginReq", result.getBody());
        NettyMessageHeader resultHeader = result.getHeader();
        assertEquals(MessageTypeEnum.LOGIN_REQ, resultHeader.getMessageType());
    }

}