package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsSiteControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.request.QuerySiteRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms/site")
public class CmsSiteController implements CmsSiteControllerApi {
    @Autowired
    private SiteService siteService;

    @Override
    @GetMapping("/list")
    public QueryResponseResult getSiteList() {
        return siteService.getSites();
    }

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult getSiteList(@PathVariable("page") int page, @PathVariable("size") int size, QuerySiteRequest querySiteRequest) {
        return siteService.getSites(page, size, querySiteRequest);
    }

    @Override
    @PostMapping("/add")
    public ResponseResult add(@RequestBody CmsSite cmsSite) {
        return siteService.addSite(cmsSite);
    }

    @Override
    @GetMapping("/get/{id}")
    public CmsSite getSiteById(@PathVariable("id") String siteId) {
        return siteService.findSiteById(siteId);
    }

    @Override
    @PutMapping("/edit/{id}")
    public ResponseResult edit(@PathVariable("id") String siteId, @RequestBody CmsSite cmsSite) {
        return siteService.updateSite(siteId, cmsSite);
    }

    @Override
    @DeleteMapping("/del/{id}")
    public ResponseResult del(@PathVariable("id") String siteId) {
        return siteService.deleteSite(siteId);
    }


}
