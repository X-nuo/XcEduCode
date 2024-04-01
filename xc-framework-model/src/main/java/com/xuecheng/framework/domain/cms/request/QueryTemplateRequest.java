package com.xuecheng.framework.domain.cms.request;

import com.xuecheng.framework.model.request.RequestData;
import lombok.Data;

/**
 * CMS 模版查询条件
 */
@Data
public class QueryTemplateRequest extends RequestData {
    //@ApiModelProperty("站点ID")
    private String siteId;
}
