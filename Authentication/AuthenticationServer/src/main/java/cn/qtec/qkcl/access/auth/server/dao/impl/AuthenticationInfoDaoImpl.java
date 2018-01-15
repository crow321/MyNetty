package cn.qtec.qkcl.access.auth.server.dao.impl;

import cn.qtec.qkcl.access.auth.server.dao.IAuthenticationInfoDao;
import cn.qtec.qkcl.access.auth.server.entity.AuthRespInfo;
import cn.qtec.qkcl.access.auth.server.entity.AuthenticationInfo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Created by zhangp on 2017/9/28.
 */
@Repository
public class AuthenticationInfoDaoImpl implements IAuthenticationInfoDao {
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void insert(AuthenticationInfo serverStoredInfo) {
        getCurrentSession().save(serverStoredInfo);
    }

    @Override
    public AuthRespInfo getAuthRespInfoByDeviceID(String deviceID) {
        String hql = "select info.authRespInfo from AuthenticationInfo as info where info.deviceID = :deviceID";
        Query query = getCurrentSession().createQuery(hql);
        query.setParameter("deviceID", deviceID);
        return (AuthRespInfo) query.uniqueResult();
    }

    @Override
    public byte[] queryRootKeyByRootKeyID(byte[] rootKeyID) {
        String hql = "select info.rootKeyValue from AuthenticationInfo as info where info.rootKeyID= :rootKeyID";
        Query query = getCurrentSession().createQuery(hql);
        query.setParameter("rootKeyID", rootKeyID);
        return (byte[]) query.uniqueResult();
    }

    @Override
    public byte[] queryPasswordByDeviceID(String deviceID) {
        String hql = "select info.password from AuthenticationInfo as info where info.deviceID= :deviceID";
        Query query = getCurrentSession().createQuery(hql);
        query.setParameter("deviceID", deviceID);
        return (byte[]) query.uniqueResult();
    }

    @Override
    public boolean deleteAuthenticationInfoByDeviceId(String deviceID) {
        String hql = "delete AuthenticationInfo where deviceID =:deviceID";
        Query query = getCurrentSession().createQuery(hql);
        query.setParameter("deviceID", deviceID);
        return query.executeUpdate() == 1;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
}
