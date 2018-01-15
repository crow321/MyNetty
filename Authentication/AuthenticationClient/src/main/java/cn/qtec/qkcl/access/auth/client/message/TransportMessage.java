package cn.qtec.qkcl.access.auth.client.message;


/**
 * @author Created by zhangp on 2017/9/28.
 */
public class TransportMessage extends BaseMessage {
    private byte[] body;
    private byte[] deviceID;

    public TransportMessage(int version, byte[] body, byte[] deviceID) {
        super(version, 0, body.length, null);
        this.body = body;
        this.deviceID = deviceID;
    }


    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(byte[] deviceID) {
        this.deviceID = deviceID;
    }
}
