package cn.zhp.netty.custom.transport.enums;

/**
 * @author Created by zhangp on 2017/9/13.
 * @version v1.0.0
 */
public class MessageTypeEnum {
    public static final byte DEFAULT          = -1;
    public static final byte LOGIN_REQ        = 0x01;
    public static final byte LOGIN_RESP       = 0x02;
    public static final byte HEART_BEAT_REQ   = 0x03;
    public static final byte HEART_BEAT_RESP  = 0x04;
    public static final byte REQUEST          = 0x05;
    public static final byte RESPONSE         = 0x06;


    private byte value;
    public MessageTypeEnum() {
        this.value = DEFAULT;
    }
    public MessageTypeEnum(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EnumMessageType{" +
                "value=" + value +
                '}';
    }
}
