package cn.zhp.netty.custom.transport;

import cn.qtec.key.kmip.field.KMIPBatch;
import cn.qtec.key.kmip.field.KMIPField;
import cn.qtec.key.kmip.kmipenum.EnumOperation;
import cn.qtec.key.kmip.process.encoder.KMIPEncoder;
import cn.qtec.key.kmip.types.KMIPByteString;
import cn.qtec.key.kmip.utils.KMIPUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @version v1.3
 *          Created by zhangp on 2017/9/12.
 */
@Component
public class TransportImpl implements TransportInterface {
    private final static Logger logger = LoggerFactory.getLogger(TransportImpl.class);

    @PostConstruct
    public void init() {

    }

    @Override
    public void sendMessage(ByteBuf message) {

        ByteBuf buf = Unpooled.buffer();

        //version 1
        buf.writeByte(1);
        //消息长度 2字节
//        buf.writeShort();

        KMIPField requestField = new KMIPField();
        KMIPBatch batch = new KMIPBatch();
        batch.setOperation(EnumOperation.Create);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        logger.debug("batchItem ID: {}", uuid);
        batch.setUniqueBatchItemID(new KMIPByteString(uuid));

        requestField.addBatch(batch);
        requestField.calculateBatchCount();

        KMIPEncoder encoder = new KMIPEncoder();
        ArrayList<Byte> al = encoder.encodeRequest(requestField);

        byte[] fieldBytes = KMIPUtils.toByteArray(al);

        buf.writeBytes(fieldBytes);

    }


}
