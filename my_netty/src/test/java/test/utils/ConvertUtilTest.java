package test.utils;

import cn.zhp.netty.custom.utils.TransportUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.BaseJunit4Test;

/**
 * @version v1.3
 *          Created by zhangp on 2017/9/12.
 */
public class ConvertUtilTest extends BaseJunit4Test {
    @Autowired
    private TransportUtil transportUtil;

    @Test
    public void arrayListToBytes() throws Exception {
        transportUtil.wrapKMIPRequest(null);
    }


}