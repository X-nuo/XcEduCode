package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsTemplateControllerApi;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.ext.CmsTemplateExt;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsTemplateFileResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/cms/template")
public class CmsTemplateController implements CmsTemplateControllerApi {
    @Autowired
    private TemplateService templateService;

    @Override
    @GetMapping("/list")
    public QueryResponseResult getTemplateList() {
        return templateService.getTemplates();
    }

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult getTemplateList(@PathVariable("page") int page, @PathVariable("size") int size, QueryTemplateRequest queryTemplateRequest) {
        return templateService.getTemplates(page, size, queryTemplateRequest);
    }

    @Override
    @PostMapping("/add")
    public ResponseResult add(@RequestPart CmsTemplate cmsTemplate, @RequestPart(required = false) MultipartFile file) {
        return templateService.addTemplate(cmsTemplate, file);
    }

    @Override
    @PostMapping("/addFile/{id}")
    public ResponseResult addTemplateFile(@PathVariable("id") String templateId, @RequestPart MultipartFile file) {
        return templateService.addTemplateFileById(templateId, file);
    }

    @Override
    @GetMapping("/get/{id}")
    public CmsTemplate getTemplateById(@PathVariable("id") String templateId) {
        return templateService.findTemplateById(templateId);
    }

    @Override
    @GetMapping("/getFile/{id}")
    public CmsTemplateFileResult getTemplateFileById(@PathVariable("id") String templateId) {
        return templateService.getTemplateFile(templateId);
    }

    @Override
    @PutMapping("/edit/{id}")
    public ResponseResult edit(@PathVariable("id") String templateId, @RequestBody CmsTemplateExt cmsTemplateExt) {
        return templateService.updateTemplate(templateId, cmsTemplateExt);
    }

    @Override
    @DeleteMapping("/del/{id}")
    public ResponseResult del(@PathVariable("id") String templateId) {
        return templateService.deleteTemplate(templateId);
    }
}
