package com.xuecheng.framework.domain.cms.request;

import com.xuecheng.framework.model.request.RequestData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * CMS 模版查询条件
 */
@Data
public class QuerySiteRequest extends RequestData {
    @ApiModelProperty("站点名称")
    private String siteName;
}
