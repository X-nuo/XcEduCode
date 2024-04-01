package com.xuecheng.framework.domain.system.response;

import com.xuecheng.framework.model.response.ResultCode;
import lombok.ToString;

@ToString
public enum SysCode implements ResultCode {
    SYS_ADDDICTIONARY_EXISTSNAME(false, 21001, "字典类型已存在"),
    SYS_ADDDICTIONARYVAL_NOTEXISTSNAME(false, 21002, "字典类型不存在");

    //操作代码
    //@ApiModelProperty(value = "操作是否成功", example = "true", required = true)
    boolean success;
    //操作代码
    //@ApiModelProperty(value = "操作代码", example = "22001", required = true)
    int code;
    //提示信息
    //@ApiModelProperty(value = "操作提示", example = "操作过于频繁！", required = true)
    String message;

    private SysCode(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
