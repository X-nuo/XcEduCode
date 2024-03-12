package com.xuecheng.framework.domain.system.request;

import com.xuecheng.framework.model.request.RequestData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class QueryDictionaryRequest extends RequestData {
    @ApiModelProperty("字典名称")
    private String dname;
}
