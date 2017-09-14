package cn.zhp.netty.custom.client;

import cn.zhp.netty.base.client.NettyClient;
import cn.zhp.netty.custom.handler.ClientHeartBeatReqHandler;
import cn.zhp.netty.custom.handler.ClientLoginAuthReqHandler;
import cn.zhp.netty.custom.handler.CustomClientHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Description:
 *
 * @auth zhangp
 * @time 2017/9/6 16:58
 */
@Component
public class CustomClient extends NettyClient{
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-context.xml");
        CustomClient customClient = (CustomClient) context.getBean("customClient");
        CustomClientHandler customClientHandler = (CustomClientHandler) context.getBean("customClientHandler");
        ClientLoginAuthReqHandler clientLoginAuthReqHandler = (ClientLoginAuthReqHandler) context.getBean("clientLoginAuthReqHandler");
        ClientHeartBeatReqHandler clientHeartBeatReqHandler = (ClientHeartBeatReqHandler) context.getBean("clientHeartBeatReqHandler");


        customClient.addChildChannelHandler(clientLoginAuthReqHandler);
        customClient.addChildChannelHandler(clientHeartBeatReqHandler);

        customClient.addChildChannelHandler(customClientHandler);
        customClient.start();
    }
}
