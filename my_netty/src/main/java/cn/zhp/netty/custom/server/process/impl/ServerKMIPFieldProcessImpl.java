package cn.zhp.netty.custom.server.process.impl;

import cn.qtec.key.kmip.field.KMIPField;
import cn.zhp.netty.custom.server.process.ServerKMIPFieldProcessInterface;
import org.springframework.stereotype.Component;

/**
 * @author Created by zhangp on 2017/9/12.
 * @version v1.0.0
 */
@Component
public class ServerKMIPFieldProcessImpl implements ServerKMIPFieldProcessInterface {
    @Override
    public KMIPField processSync(KMIPField srcField) {
        return null;
    }

    @Override
    public boolean processAsync(KMIPField srcField) {
        //todo
        return false;
    }
}
