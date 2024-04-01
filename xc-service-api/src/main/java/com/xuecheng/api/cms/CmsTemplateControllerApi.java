package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.ext.CmsTemplateExt;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsTemplateFileResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * CMS Template API
 */
//@Api(value = "CMS 模版管理接口")
public interface CmsTemplateControllerApi {
    //@ApiOperation("查询模版列表")
    public QueryResponseResult getTemplateList();
//    @ApiOperation("查询模版列表")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "path", dataType = "int"),
//            @ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int")
//    })
    public QueryResponseResult getTemplateList(int page, int size, QueryTemplateRequest queryTemplateRequest);
    //@ApiOperation("新增模版")
    public ResponseResult add(CmsTemplate cmsTemplate, MultipartFile file);
    //@ApiOperation("添加模版文件")
    public ResponseResult addTemplateFile(String templateId, MultipartFile file);
    //@ApiOperation("根据Id查询模版信息")
    public CmsTemplate getTemplateById(String templateId);
    //@ApiOperation("获取模版文件")
    public CmsTemplateFileResult getTemplateFileById(String templateId);
    //@ApiOperation("修改模版信息")
    public ResponseResult edit(String templateId, CmsTemplateExt cmsTemplateExt);
    //@ApiOperation("删除模版")
    public ResponseResult del(String templateId);
}
