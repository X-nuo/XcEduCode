package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.cms.response.GenerateHtmlResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms/page")
public class CmsPageController implements CmsPageControllerApi {
    @Autowired
    private PageService pageService;

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult getPageList(@PathVariable("page") int page, @PathVariable("size") int size, QueryPageRequest queryPageRequest) {
        return pageService.getPages(page, size, queryPageRequest);
    }

    @Override
    @PostMapping("/add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage) {
        return pageService.addPage(cmsPage);
    }

    @Override
    @PostMapping("/save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage) {
        return pageService.save(cmsPage);
    }

    @Override
    @GetMapping("/get/{id}")
    public CmsPage getPageById(@PathVariable("id") String pageId) {
        return pageService.findPageById(pageId);
    }

    @Override
    @PutMapping("/edit/{id}")
    public CmsPageResult edit(@PathVariable("id") String pageId, @RequestBody CmsPage cmsPage) {
        return pageService.updatePage(pageId, cmsPage);
    }

    @Override
    @DeleteMapping("/del/{id}")
    public ResponseResult del(@PathVariable("id") String pageId) {
        return pageService.deletePage(pageId);
    }

    @Override
    @GetMapping("/preview/{id}")
    public GenerateHtmlResult preview(@PathVariable("id") String pageId) {
        return pageService.generateHtml(pageId);
    }

    @Override
    @PostMapping("/post/{id}")
    public ResponseResult post(@PathVariable("id") String pageId) {
        return pageService.postPage(pageId);
    }

    @Override
    @PostMapping("/postQuick")
    public CmsPostPageResult postQuick(@RequestBody CmsPage cmsPage) {
        return pageService.postQuick(cmsPage);
    }
}
