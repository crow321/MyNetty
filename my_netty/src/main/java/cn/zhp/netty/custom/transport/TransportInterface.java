package cn.zhp.netty.custom.transport;

import io.netty.buffer.ByteBuf;

/**
 * @auth Created by zhangp on 2017/9/12.
 */
public interface TransportInterface {

    public void sendMessage(ByteBuf message);
}
