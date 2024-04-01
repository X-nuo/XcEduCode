package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.cms.response.GenerateHtmlResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

/**
 * CMS Page API
 */
//@Api(value = "CMS 页面管理接口")
public interface CmsPageControllerApi {
//    @ApiOperation("查询页面列表")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "path", dataType = "int"),
//            @ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int")
//    })
    public QueryResponseResult getPageList(int page, int size, QueryPageRequest queryPageRequest);
    //@ApiOperation("增加页面")
    public CmsPageResult add(CmsPage cmsPage);
    //@ApiOperation("保存页面")
    public CmsPageResult save(CmsPage cmsPage);
    //@ApiOperation("根据ID查询页面")
    public CmsPage getPageById(String pageId);
    //@ApiOperation("修改页面信息")
    public CmsPageResult edit(String pageId, CmsPage cmsPage);
    //@ApiOperation("删除页面")
    public ResponseResult del(String pageId);
    //@ApiOperation("预览页面")
    public GenerateHtmlResult preview(String pageId);
    //@ApiOperation("发布页面")
    public ResponseResult post(String pageId);
    //@ApiOperation("一键发布页面")
    public CmsPostPageResult postQuick(CmsPage cmsPage);
}
