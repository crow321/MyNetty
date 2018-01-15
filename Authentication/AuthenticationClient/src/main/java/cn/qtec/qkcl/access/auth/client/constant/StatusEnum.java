package cn.qtec.qkcl.access.auth.client.constant;

/**
 * @author Created by zhangp on 2017/12/17
 */
public enum StatusEnum {
    SUCCESS("成功", 0),
    LOCAL_FLAG("本地标志", 1),;

    private String description;
    private int value;

    StatusEnum(String description, int value) {
        this.description = description;
        this.value = value;
    }

    public static String getDescription(int value) {
        String desc = "";
        switch (value) {
            case 0:
                desc = "收到响应: 成功";
                break;
/*
            case 1:
                desc = "收到响应失败";
                break;
            case 2:
                desc = "重复登录";
                break;
            case 3:
                desc = "未通过校验";
                break;
*/
            default:
                desc = "收到的响应状态: 失败";
                break;
        }
        return desc;
    }

    public int getValue() {
        return value;
    }
}
