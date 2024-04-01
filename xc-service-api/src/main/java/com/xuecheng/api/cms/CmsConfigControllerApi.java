package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsConfig;

//@Api(value = "CMS 配置（数据模型）管理接口")
public interface CmsConfigControllerApi {
    //@ApiOperation("根据ID查询配置信息")
    public CmsConfig getModel(String id);
}
