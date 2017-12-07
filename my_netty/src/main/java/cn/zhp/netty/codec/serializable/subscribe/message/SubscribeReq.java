package cn.zhp.netty.codec.serializable.subscribe.message;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Description:
 *      1）实现 Serializable接口
 *      2）默认的序列号ID
 * @author Created by zhangp on 2017/9/21.
 * @version v1.0.0
 */
@Component
public class SubscribeReq implements Serializable {
    //默认的序列号ID
    private static final long serialVersionUID = 1L;

    //订购编号
    private int subReqID;
    //用户名
    private String userName;
    //订购的产品名称
    private String productName;
    //订购者电话号码
    private String phoneNumber;
    //订购者的地址
    private String address;

    public int getSubReqID() {
        return subReqID;
    }

    public void setSubReqID(int subReqID) {
        this.subReqID = subReqID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "SubscribeReq.proto{" +
                "subReqID=" + subReqID +
                ", userName='" + userName + '\'' +
                ", productName='" + productName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
