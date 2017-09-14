package cn.zhp.netty.custom.transport.entity;

/**
 * @author Created by zhangp on 2017/9/12.
 * @version v1.0.0
 */
public final class NettyMessage {
    //消息头
    private NettyMessageHeader header;
    //消息体
    private Object body;

    public final NettyMessageHeader getHeader() {
        return header;
    }

    public final void setHeader(NettyMessageHeader header) {
        this.header = header;
    }

    public final Object getBody() {
        return body;
    }

    public final void setBody(Object body) {
        this.body = body;
    }

    public boolean hasBody() {
        return body != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
//        sb.append("NettyMessage:");
        sb.append("\n\t消息头:\t").append(header.toString());
        if (hasBody()) {
            sb.append("\n\t消息体:\t").append(body.toString());
        }
        return sb.toString();
    }
}
