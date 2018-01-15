package cn.qtec.qkcl.access.auth.server.constant;

/**
 * @author Created by zhangp on 2017/12/17
 */
public enum StatusEnum {
    SUCCESS("成功", 0),
    LOCAL_FLAG("本地标志", 1),
    FAILURE("失败", 101),;

    private String description;
    private int value;

    StatusEnum(String description, int value) {
        this.description = description;
        this.value = value;
    }

    public String getDescription(int value) {
        switch (value) {
            case 0:
                description = "成功";
                break;
/*            case 1:
                description = "失败";
                break;
            case 2:
                description = "重复登录";
                break;
            case 3:
                description = "校验失败";
                break;*/
            default:
                description = "失败";
                break;
        }
        return description;
    }

    public int getValue() {
        return value;
    }
}
