package cn.zhp.netty.custom.handler.codec;

import cn.qtec.key.kmip.field.KMIPField;
import cn.qtec.key.kmip.process.encoder.KMIPEncoder;
import cn.zhp.netty.custom.transport.entity.NettyMessage;
import cn.zhp.netty.custom.transport.entity.NettyMessageHeader;
import cn.zhp.netty.custom.transport.enums.MessageTypeEnum;
import cn.zhp.netty.custom.utils.ConvertUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Created by zhangp on 2017/9/12.
 * @version v1.0.0
 */
@Component
public class NettyMessageEncoder extends MessageToByteEncoder<NettyMessage> {
    private static final Logger logger = LoggerFactory.getLogger(NettyMessageEncoder.class);
    @Autowired
    private KMIPEncoder kmipEncoder = new KMIPEncoder();

    @Override
    protected void encode(ChannelHandlerContext ctx, NettyMessage msg, ByteBuf out) throws Exception {
        if (msg == null || msg.getHeader() == null) {
            throw new Exception("The encode message is null");
        }
//        logger.info("Netty消息编码器 ============= 消息:\n{}", msg);

        NettyMessageHeader header = msg.getHeader();
        byte type = header.getMessageType();
        ByteBuf sendBuf = Unpooled.buffer();
        sendBuf.writeInt(header.getCrcCode());
        sendBuf.writeByte(header.getVersion());
        sendBuf.writeLong(header.getSessionID());
        sendBuf.writeByte(type);
        sendBuf.writeByte(header.getPriority());

        Object obj = msg.getBody();
        if (type == MessageTypeEnum.LOGIN_REQ) {
            String body = (String) obj;
            sendBuf.writeShort(body.getBytes().length);
            sendBuf.writeBytes(body.getBytes());
        } else if (type == MessageTypeEnum.LOGIN_RESP) {
            byte body = (byte) obj;
            sendBuf.writeShort(1);
            sendBuf.writeByte(body);
        } else {
            KMIPField body1 = (KMIPField) obj;
            byte[] bodyBytes = ConvertUtil.arrayListToBytes(kmipEncoder.encodeRequest(body1));
            //内容不为空
            if (bodyBytes != null) {
                sendBuf.writeShort(bodyBytes.length);
                sendBuf.writeBytes(bodyBytes);
            }
        }
        logger.debug("==================== Netty消息编码成功");
        // 把Message添加到List传递到下一个Handler
        out.writeBytes(sendBuf);
    }
}
