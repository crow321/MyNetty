package cn.zhp.netty.custom.server.process;

import cn.qtec.key.kmip.field.KMIPField;

/**
 * @author Created by zhangp on 2017/9/12.
 * @version v1.0.0
 */
public interface ServerKMIPFieldProcessInterface {

    public KMIPField processSync(KMIPField srcField);

    public boolean processAsync(KMIPField srcField);
}
