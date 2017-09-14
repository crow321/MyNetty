package cn.zhp.netty.custom.utils;

import cn.qtec.key.kmip.field.KMIPField;
import cn.qtec.key.kmip.process.decoder.KMIPDecoder;
import cn.qtec.key.kmip.process.encoder.KMIPEncoder;
import cn.zhp.netty.custom.transport.entity.NettyMessage;
import cn.zhp.netty.custom.transport.entity.NettyMessageHeader;
import cn.zhp.netty.custom.transport.enums.MessageTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 用于封装和解封数据包
 *
 * @author Created by zhangp on 2017/9/12.
 * @version v1.3
 */
@Component
public class TransportUtil {
    private static final Logger logger = LoggerFactory.getLogger(TransportUtil.class);
    @Autowired
    private KMIPEncoder kmipEncoder;
    @Autowired
    private KMIPDecoder kmipDecoder;

    public ByteBuf wrapKMIPRequest(KMIPField field) {
        if (field == null) {
            logger.error("field is null!");
            return Unpooled.buffer(0);
        }

        ByteBuf byteBuf = Unpooled.buffer();
        //version 1
        byteBuf.writeByte(1);
        //消息类型 1字节 1:请求 2:响应
        byteBuf.writeByte(1);

        ArrayList<Byte> al = kmipEncoder.encodeRequest(field);
        byte[] body = ConvertUtil.arrayListToBytes(al);

        if (body != null) {
            logger.debug("--------------- KMIP编码后长度: {}", body.length);
            //消息长度
            byteBuf.writeShort(body.length);
            //消息体
            byteBuf.writeBytes(body);
        }
        return byteBuf;
    }

    public KMIPField unpackByteBufRequest(ByteBuf req) {
        if (req != null) {
            //检查版本
            if (req.readByte() != 1) {
                logger.error("请求消息版本错误");
                return null;
            }
            byte messageType = req.readByte();
            if (messageType < 0 || messageType > 2) {
                logger.error("消息类型错误, error message type: {}", messageType);
                return null;
            }
            short length = req.readShort();
            if (length < 0 || length > Short.MAX_VALUE) {
                logger.error("消息体长度错误!");
                return null;
            }

            byte[] body = new byte[length];
            req.readBytes(body);

            KMIPField resultField = null;
            try {
                resultField = kmipDecoder.decodeRequest(ConvertUtil.bytesToArrayList(body));
            } catch (Exception e) {
                logger.error("KMIP 解码错误...");
                e.printStackTrace();
            }
            return resultField;
        }
        return null;
    }

    public NettyMessage wrapKMIPFieldReq(KMIPField field) {
        //处理KMIPField
        byte[] fieldBody = getKMIPReqBytes(field);
        if (fieldBody == null) {
            logger.error("KMIPField body is null");
            return null;
        }

        NettyMessage message = new NettyMessage();
        NettyMessageHeader header = new NettyMessageHeader();
        message.setHeader(header);

        //消息头
        header.setMessageType(MessageTypeEnum.REQUEST);
        header.setVersion((byte) 1);
        header.setMessageLength((short) fieldBody.length);

        //消息体
        message.setBody(fieldBody);

        return message;
    }

    public NettyMessage unpacketKMIPReq(byte[] fieldBytes) {
        NettyMessage message = new NettyMessage();
        NettyMessageHeader header = new NettyMessageHeader();
        message.setHeader(header);

        KMIPField field = getKMIPReqField(fieldBytes);

        //消息头
        header.setVersion((byte) 1);
        header.setMessageType(MessageTypeEnum.RESPONSE);


        //消息体
        message.setBody(field);

        return message;
    }

    private byte[] getKMIPReqBytes(KMIPField field) {
        ArrayList<Byte> al = kmipEncoder.encodeRequest(field);
        return ConvertUtil.arrayListToBytes(al);
    }

    private KMIPField getKMIPReqField(byte[] fieldBytes) {
        try {
            return kmipDecoder.decodeRequest(ConvertUtil.bytesToArrayList(fieldBytes));
        } catch (Exception e) {
            logger.error("KMIP decoder error, {}", e.getLocalizedMessage());
            return null;
        }
    }
}
