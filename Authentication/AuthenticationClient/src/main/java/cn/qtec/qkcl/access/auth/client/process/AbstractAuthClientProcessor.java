package cn.qtec.qkcl.access.auth.client.process;

import cn.qtec.qkcl.access.auth.client.entity.AccessAuthInfo;
import cn.qtec.qkcl.access.auth.client.entity.AuthClientSessionInfo;
import cn.qtec.qkcl.access.auth.client.message.TransportMessage;
import cn.qtec.qkcl.message.enums.EMessageType;
import cn.qtec.qkcl.message.process.AbstractAuthServerMessageProcesser;

/**
 * @author Created by zhangp
 *         2017/12/7
 */
public abstract class AbstractAuthClientProcessor extends AbstractAuthServerMessageProcesser {

    public AbstractAuthClientProcessor() {
        super(EMessageType.ACCESS_AUTH_REQUEST);
    }

    public abstract TransportMessage generateStartAuthRequest(AccessAuthInfo initAuthInfo);

    public abstract AuthClientSessionInfo getSessionInfo(long timeout, byte[] deviceID) throws InterruptedException;
}
