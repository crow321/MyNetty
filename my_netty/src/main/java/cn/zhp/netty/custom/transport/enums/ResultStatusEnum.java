package cn.zhp.netty.custom.transport.enums;

/**
 * @author Created by zhangp on 2017/9/13.
 * @version v1.0.0
 */
public class ResultStatusEnum {
    public static final byte DEFAULT     = -1;
    public static final byte SUCCESS     = 0X00;
    public static final byte FAIL        = 0X01;

    private byte value;
    public ResultStatusEnum() {
        this.value = DEFAULT;
    }
    public ResultStatusEnum(byte value) {
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
        return "ResutlEnum{" +
                "value=" + value +
                '}';
    }
}
