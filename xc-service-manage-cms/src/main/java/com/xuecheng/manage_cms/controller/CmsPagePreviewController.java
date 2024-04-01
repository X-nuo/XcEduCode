package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.domain.cms.response.GenerateHtmlResult;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

@Controller
public class CmsPagePreviewController extends BaseController {
    @Autowired
    private PageService pageService;

    @GetMapping(value = "/cms/preview/{id}")
    public void preview(@PathVariable("id") String pageId) {
        GenerateHtmlResult generateHtmlResult = pageService.generateHtml(pageId);
        String html = generateHtmlResult.getHtml();
        if(StringUtils.isNotEmpty(html)) {
            ServletOutputStream outputStream = null;
            try {
                response.setHeader("Content-type", "text/html;charset=utf-8");
                outputStream = response.getOutputStream();
                outputStream.write(html.getBytes("utf-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
