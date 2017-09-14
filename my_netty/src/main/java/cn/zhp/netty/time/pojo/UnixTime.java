package cn.zhp.netty.time.pojo;

import java.util.Date;

/**
 * Description:
 *
 * @auth zhangp
 * @time 2017/9/6 14:04
 */
public class UnixTime {
    private final long value;

    public UnixTime() {
        //这里要加2208988800，是因为获得到的时间是格林尼治时间，所以要变成东八区的时间，否则会与与北京时间有8小时的时差
        this(System.currentTimeMillis() / 1000L + 2208988800L);
    }

    public UnixTime(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new Date((getValue() - 2208988800L) * 1000L).toString();
    }
}
