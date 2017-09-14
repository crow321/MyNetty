package cn.zhp.netty.custom.handler.codec;

import cn.qtec.key.kmip.field.KMIPField;
import cn.qtec.key.kmip.process.decoder.KMIPDecoder;
import cn.zhp.netty.custom.transport.entity.NettyMessage;
import cn.zhp.netty.custom.transport.entity.NettyMessageHeader;
import cn.zhp.netty.custom.transport.enums.MessageTypeEnum;
import cn.zhp.netty.custom.utils.ConvertUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by zhangp on 2017/9/13.
 * @version v1.0.0
 */
@Component
public class NettyMessageDecoder extends MessageToMessageDecoder<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(NettyMessageDecoder.class);
    @Autowired
    private KMIPDecoder kmipDecoder = new KMIPDecoder();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        logger.info("=====================Netty消息解码器!");
        NettyMessage message = new NettyMessage();
        NettyMessageHeader header = new NettyMessageHeader();
        int crcCode = msg.readInt();
        byte version = msg.readByte();
        long sessionID = msg.readLong();
        byte type = msg.readByte();
        byte priority = msg.readByte();
        short length = msg.readShort();

//        logger.debug("=================> 消息校验码: {}", crcCode);
//        logger.debug("=================> 消息版  本: {}", version);
//        logger.debug("=================> 消息会话ID: {}", sessionID);
        logger.debug("=================> 消息类  型: {}", type);
//        logger.debug("=================> 消息优先级: {}", priority);
        logger.debug("=================> 消息体长度: {}", length);

        header.setCrcCode(crcCode);
        header.setVersion(version);
        header.setSessionID(sessionID);
        //1 请求 2 响应
        header.setMessageType(type);
        header.setPriority(priority);
        header.setMessageLength(length);
        message.setHeader(header);

        if (length != msg.readableBytes()) {
            logger.error("decode: 消息长度和缓冲区可读字节不等！");
        }

        byte[] bodyBytes = new byte[length];
        msg.readBytes(bodyBytes);

        switch (type) {
            case MessageTypeEnum.LOGIN_REQ:
                String body = new String(bodyBytes);
                message.setBody(body);
                break;
            case MessageTypeEnum.LOGIN_RESP:
                byte bodyByte = bodyBytes[0];
                message.setBody(bodyByte);
                break;
            default:
                ArrayList<Byte> al = ConvertUtil.bytesToArrayList(bodyBytes);
                if (al != null && length > 50) {
                    KMIPField decoderField = kmipDecoder.decodeRequest(al);
                    message.setBody(decoderField);
                }
                break;
        }
        /*if (message.getBody() != null) {
            logger.debug("===================>decode--消息体:\n{}", message.getBody().toString());
        }*/

        out.add(message);

//        logger.error("decode receive ByteBuf is null");
    }
}
