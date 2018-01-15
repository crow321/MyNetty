package cn.qtec.qkcl.access.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Created by zhangp
 * @version 1.0.0
 * @desc
 * @time 2017/12/15
 */
@Embeddable
public class AuthRespInfo {
    @Column(name = "salt")
    private byte[] salt;
    @Column(name = "iteration_count")
    private int iterationCount;

    public AuthRespInfo() {
    }

    public AuthRespInfo(byte[] salt, int iterationCount) {
        this.salt = salt;
        this.iterationCount = iterationCount;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }
}
