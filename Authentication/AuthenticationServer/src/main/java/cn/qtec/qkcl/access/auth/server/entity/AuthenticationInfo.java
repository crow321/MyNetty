package cn.qtec.qkcl.access.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Created by zhangp on 2017/9/28.
 */
@Entity
@Table(name = "qkey_authentication_info")
public class AuthenticationInfo {
    @Id
    @Column(name = "user_id")
    private long userID;
    @Column(name = "user_name")
    private String deviceID;
    @Column(name = "password")
    private byte[] password;
    @Column(name = "passType")
    private int passType;
    @Column(name = "user_guid")
    private String userGUID;
    @Column(name = "parent_name")
    private String parentName;
    @Column(name = "config_version")
    private String configVersion;
    @Column(name = "root_key_id")
    private byte[] rootKeyID;
    @Column(name = "root_key_value")
    private byte[] rootKeyValue;
    @Column(name = "mobilephone")
    private String mobilePhone;
    @Column
    private String email;
    @Column(name = "user_type")
    private int userType;
    @Column(name = "rfu1")
    private String rfu1;
    @Column(name = "rfu2")
    private int rfu2;
    @Embedded
    private AuthRespInfo authRespInfo;

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public int getPassType() {
        return passType;
    }

    public void setPassType(int passType) {
        this.passType = passType;
    }

    public String getUserGUID() {
        return userGUID;
    }

    public void setUserGUID(String userGUID) {
        this.userGUID = userGUID;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }

    public byte[] getRootKeyID() {
        return rootKeyID;
    }

    public void setRootKeyID(byte[] rootKeyID) {
        this.rootKeyID = rootKeyID;
    }

    public byte[] getRootKeyValue() {
        return rootKeyValue;
    }

    public void setRootKeyValue(byte[] rootKeyValue) {
        this.rootKeyValue = rootKeyValue;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getRfu1() {
        return rfu1;
    }

    public void setRfu1(String rfu1) {
        this.rfu1 = rfu1;
    }

    public int getRfu2() {
        return rfu2;
    }

    public void setRfu2(int rfu2) {
        this.rfu2 = rfu2;
    }

    public AuthRespInfo getAuthRespInfo() {
        return authRespInfo;
    }

    public void setAuthRespInfo(AuthRespInfo authRespInfo) {
        this.authRespInfo = authRespInfo;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }
}
