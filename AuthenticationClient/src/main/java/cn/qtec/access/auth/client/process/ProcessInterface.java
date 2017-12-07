package cn.qtec.access.auth.client.process;

import cn.qtec.access.auth.client.entity.SessionInfo;
import cn.qtec.access.auth.client.message.TransportMessage;

/**
 * @author zhangp
 * @date 2017/9/30
 */
public interface ProcessInterface {
    /**
     * 客户端发送开始接入认证请求接口
     *
     * @return 客户端准备发送的开始接入认证请求报文
     */
    TransportMessage generateStartAuthRequest();

    /**
     * 客户端处理服务端响应报文接口
     *
     * @param transportMessage 收到服务端的响应报文
     * @return 解析响应报文后返回的结果
     */
    TransportMessage processAuthMessage(TransportMessage transportMessage);

    /**
     * 获取会话密钥和会话ID
     *
     * @return sessionID and sessionKey
     */
    SessionInfo getSessionInfo();

}
