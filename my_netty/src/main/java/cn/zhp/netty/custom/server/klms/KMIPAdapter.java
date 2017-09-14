package cn.zhp.netty.custom.server.klms;

import cn.qtec.key.kmip.field.KMIPField;

/**
 * @author Created by zhangp on 2017/9/12.
 * @version v1.0.0
 */
public interface KMIPAdapter {
    public KMIPField doProcessSync(KMIPField reqField);

    public KMIPField doProcessAsync(KMIPField reqField);
}
