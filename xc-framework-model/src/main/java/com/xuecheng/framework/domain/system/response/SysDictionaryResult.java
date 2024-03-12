package com.xuecheng.framework.domain.system.response;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SysDictionaryResult extends ResponseResult {
    SysDictionary sysDictionary;
    public SysDictionaryResult(ResultCode resultCode, SysDictionary sysDictionary) {
        super(resultCode);
        this.sysDictionary = sysDictionary;
    }
}
