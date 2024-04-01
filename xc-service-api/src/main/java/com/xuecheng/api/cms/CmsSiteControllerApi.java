package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.request.QuerySiteRequest;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

/**
 * CMS Site API
 */
//@Api(value = "CMS 站点管理接口")
public interface CmsSiteControllerApi {
    //@ApiOperation("查询站点列表")
    public QueryResponseResult getSiteList();
//    @ApiOperation("查询站点列表")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "path", dataType = "int"),
//            @ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int")
//    })
    public QueryResponseResult getSiteList(int page, int size, QuerySiteRequest querySiteRequest);
    //@ApiOperation("新增站点")
    public ResponseResult add(CmsSite cmsSite);
    //@ApiOperation("根据ID查询站点")
    public CmsSite getSiteById(String siteId);
    //@ApiOperation("修改站点信息")
    public ResponseResult edit(String siteId, CmsSite cmsSite);
    //@ApiOperation("删除站点")
    public ResponseResult del(String siteId);

}
