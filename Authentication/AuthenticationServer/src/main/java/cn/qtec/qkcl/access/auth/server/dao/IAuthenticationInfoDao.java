package cn.qtec.qkcl.access.auth.server.dao;


import cn.qtec.qkcl.access.auth.server.entity.AuthRespInfo;
import cn.qtec.qkcl.access.auth.server.entity.AuthenticationInfo;

import java.util.List;

/**
 * @author Created by zhangp on 2017/9/28.
 */
public interface IAuthenticationInfoDao {

    void insert(AuthenticationInfo serverStoredInfo);

    AuthRespInfo getAuthRespInfoByDeviceID(String deviceID);

    byte[] queryRootKeyByRootKeyID(byte[] rootKeyID);

    byte[] queryPasswordByDeviceID(String deviceID);

    boolean deleteAuthenticationInfoByDeviceId(String deviceID);
}
