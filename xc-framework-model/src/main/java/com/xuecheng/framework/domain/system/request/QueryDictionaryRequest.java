package com.xuecheng.framework.domain.system.request;

import com.xuecheng.framework.model.request.RequestData;
import lombok.Data;

@Data
public class QueryDictionaryRequest extends RequestData {
    //@ApiModelProperty("字典名称")
    private String dname;
}
