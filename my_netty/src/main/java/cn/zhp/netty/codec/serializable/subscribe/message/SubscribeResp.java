package cn.zhp.netty.codec.serializable.subscribe.message;

import java.io.Serializable;

/**
 * Description:
 *      1）实现 Serializable接口
 *      2）默认的序列号ID
 * @author Created by zhangp on 2017/9/21.
 * @version v1.0.0
 */
public class SubscribeResp implements Serializable {
    private static final long serialVersionUID = 2L;

    //订购编号
    private int subReqID;
    //订购结果：0 表示成功
    private int respCode;
    //可选的详细描述信息
    private String desc;

    public int getSubReqID() {
        return subReqID;
    }

    public void setSubReqID(int subReqID) {
        this.subReqID = subReqID;
    }

    public int getRespCode() {
        return respCode;
    }

    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "SubscribeResp{" +
                "subReqID=" + subReqID +
                ", respCode=" + respCode +
                ", desc='" + desc + '\'' +
                '}';
    }
}
