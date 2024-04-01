package com.xuecheng.framework.domain.cms.request;

import com.xuecheng.framework.model.request.RequestData;
import lombok.Data;

/**
 * CMS 页面查询条件
 */
@Data
public class QueryPageRequest extends RequestData {
    //@ApiModelProperty("站点ID")
    private String siteId;
    //@ApiModelProperty("页面ID")
    private String pageId;
    //@ApiModelProperty("模版ID")
    private String templateId;
    //@ApiModelProperty("页面别名")
    private String pageAliase;
}
