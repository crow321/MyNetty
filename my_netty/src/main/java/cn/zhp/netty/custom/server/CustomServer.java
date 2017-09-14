package cn.zhp.netty.custom.server;

import cn.zhp.netty.base.server.NettyServer;
import cn.zhp.netty.custom.handler.CustomServerHandler;
import cn.zhp.netty.custom.handler.ServerHeartBeatRespHandler;
import cn.zhp.netty.custom.handler.ServerLoginAuthRespHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Description:
 *
 * @auth zhangp
 * @time 2017/9/6 16:39
 */
@Component
public class CustomServer extends NettyServer{
    public static void main(String[] args) throws Exception {
//        CustomServer customServer = new CustomServer();
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-context.xml");
        CustomServer customServer = (CustomServer) context.getBean("customServer");
        ServerLoginAuthRespHandler serverLoginAuthRespHandler = (ServerLoginAuthRespHandler) context.getBean("serverLoginAuthRespHandler");
        CustomServerHandler customServerHandler = (CustomServerHandler) context.getBean("customServerHandler");
        ServerHeartBeatRespHandler beatRespHandler = (ServerHeartBeatRespHandler) context.getBean("serverHeartBeatRespHandler");


        customServer.addChildChannel(serverLoginAuthRespHandler);
        customServer.addChildChannel(beatRespHandler);
        customServer.addChildChannel(customServerHandler);
        customServer.start();
    }
}
