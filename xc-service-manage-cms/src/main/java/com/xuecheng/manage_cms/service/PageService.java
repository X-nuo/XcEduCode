package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsSiteServer;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.*;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {
    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private SiteService siteService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 查询CMS页面列表（分页查询，条件查询）
     * @param page
     * @param size
     * @param queryPageRequest
     * @return
     */
    public QueryResponseResult getPages(int page, int size, QueryPageRequest queryPageRequest) {
        //构建查询条件（站点id）
        CmsPage cmsPage = new CmsPage();
        if(StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        Example<CmsPage> example = Example.of(cmsPage);
        //构建分页
        if(page <= 0) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page-1, size);
        //查询
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(example, pageable);
        //构建响应信息
        QueryResult<CmsPage> queryResult = new QueryResult();
        queryResult.setTotal(cmsPages.getTotalElements());
        queryResult.setList(cmsPages.getContent());

        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    /**
     * 增加页面
     * @param cmsPage
     * @return
     */
    public CmsPageResult addPage(CmsPage cmsPage) {
        /*
            if(cmsPage == null) {
                1.当未传入CmsPage信息，会产生HttpMessageNotReadableException异常
                2.在ExceptionCatch类中会自动捕获该异常，并以INVALID_PARAM（非法参数异常）信息返回给客户端
            }
         */
        //PageName、SiteId、PageWebPath作为cms_page表的唯一索引
        //检查新建页面是否存在
        CmsPage old_CmsPage = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(old_CmsPage != null) {
            //抛出自定义异常（页面已存在）
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        //新建页面默认状态"未发布"
        cmsPage.setPageStatus("101001");
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
    }

    /**
     * 保存页面（对外调用，页面存在则更新页面）
     * @param cmsPage
     * @return
     */
    public CmsPageResult save(CmsPage cmsPage) {
        CmsPage old_CmsPage = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(old_CmsPage == null) {
            return this.addPage(cmsPage);   //添加新页面
        }else {
            return this.updatePage(old_CmsPage.getPageId(), cmsPage);   //更新页面
        }
    }

    /**
     * 根据ID查询页面
     * @param pageId
     * @return
     */
    public CmsPage findPageById(String pageId) {
        Optional<CmsPage> cmsPage = cmsPageRepository.findById(pageId);
        if(cmsPage.isPresent()) {
            return cmsPage.get();
        }
        return null;
    }

    /**
     * 修改页面信息
     * @param pageId
     * @param cmsPage
     * @return
     */
    public CmsPageResult updatePage(String pageId, CmsPage cmsPage) {
        CmsPage old_CmsPage = this.findPageById(pageId);
        if(old_CmsPage != null) {
            //执行更新
            old_CmsPage.setPageName(cmsPage.getPageName());
            old_CmsPage.setPageAliase(cmsPage.getPageAliase());
            old_CmsPage.setSiteId(cmsPage.getSiteId());
            old_CmsPage.setTemplateId(cmsPage.getTemplateId());
            old_CmsPage.setPageType(cmsPage.getPageType());
            old_CmsPage.setDataUrl(cmsPage.getDataUrl());
            old_CmsPage.setPageWebPath(cmsPage.getPageWebPath());
            old_CmsPage.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            old_CmsPage.setPageCreateTime(cmsPage.getPageCreateTime());
            CmsPage new_CmsPage = cmsPageRepository.save(old_CmsPage);
            return new CmsPageResult(CommonCode.SUCCESS, new_CmsPage);
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
     * 删除页面
     * @param pageId
     * @return
     */
    public ResponseResult deletePage(String pageId) {
        CmsPage old_CmsPage = this.findPageById(pageId);
        if(old_CmsPage != null) {
            //如果页面已经发布，通过htmlFileId删除GridFS中的静态化页面
            //...
            cmsPageRepository.deleteById(pageId);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 页面静态化
     * @param pageId
     * @return
     */
    public GenerateHtmlResult generateHtml(String pageId) {
        CmsPage cmsPage = this.findPageById(pageId);
        if(cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //1.获取数据模型
        String dataUrl = cmsPage.getDataUrl();
        System.out.println(dataUrl);
        if(StringUtils.isEmpty(dataUrl)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        if(body == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //2.获取页面模版
        String templateId = cmsPage.getTemplateId();
        CmsTemplateFileResult templateFileResult = templateService.getTemplateFile(templateId);
        String templateFile = templateFileResult.getTemplateContent();
        if(StringUtils.isEmpty(templateFile)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //3.执行页面静态化
        //3.1 模版配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //3.2 模版加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateFile);
        //3.3 配置模版加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        try {
            //3.4 获取模版
            Template template = configuration.getTemplate("template");
            //3.4 生成静态页面并返回
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, body);
            if(StringUtils.isEmpty(html)) {
                ExceptionCast.cast(CmsCode.CMS_COURSE_PERVIEWISNULL);
            }

            return new GenerateHtmlResult(CommonCode.SUCCESS, html);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发布页面
     * @param pageId
     * @return
     */
    public ResponseResult postPage(String pageId) {
        //1.静态化页面
        GenerateHtmlResult generateHtmlResult = this.generateHtml(pageId);
        String html = generateHtmlResult.getHtml();
        if(StringUtils.isEmpty(html)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //2.GridFS保存静态页面
        //2.1 如果已经存在静态页面，先删除
        CmsPage cmsPage = this.findPageById(pageId);
        if(cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String htmlFileId = cmsPage.getHtmlFileId();
        if(StringUtils.isNotEmpty(htmlFileId)) {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
        //2.2 保存静态页面
        ObjectId objectId = gridFsTemplate.store(IOUtils.toInputStream(html), cmsPage.getPageName());
        htmlFileId = objectId.toString();
        if(StringUtils.isEmpty(htmlFileId)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_SAVEHTMLERROR);
        }
        //2.3 保存GridFS生成的FileID，改变cmsPage的页面状态(已发布)
        cmsPage.setHtmlFileId(htmlFileId);
        cmsPage.setPageStatus("101002");
        cmsPageRepository.save(cmsPage);
        //3.RabbitMQ发送页面消息（message: {"pageId": pageId}, routingkey: siteId）
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("pageId", pageId);
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, cmsPage.getSiteId(), JSON.toJSONString(msgMap));
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 一键发布页面
     * 页面访问地址 = 站点域名 + 站点WebPath + 页面WebPath + 页面名称
     * @param cmsPage
     * @return
     */
    public CmsPostPageResult postQuick(CmsPage cmsPage) {
        //保存或更新页面
        CmsPageResult cmsPageResult = this.save(cmsPage);
        if(!cmsPageResult.isSuccess()) {
            return new CmsPostPageResult(CommonCode.FAIL, null);
        }
        CmsPage cmsPage_saved = cmsPageResult.getCmsPage();
        String pageId = cmsPage_saved.getPageId();
        //发布页面
        ResponseResult responseResult = this.postPage(pageId);
        if(!responseResult.isSuccess()) {
            return new CmsPostPageResult(CommonCode.FAIL, null);
        }
        //构建页面访问地址
        CmsSite cmsSite = siteService.findSiteById(cmsPage_saved.getSiteId());
        String siteDomain = cmsSite.getSiteDomain();
        String siteWebPath = cmsSite.getSiteWebPath();
        String pageWebPath = cmsPage.getPageWebPath();
        String pageName = cmsPage_saved.getPageName();
        String pageUrl = siteDomain + siteWebPath + pageWebPath + pageName;
        return new CmsPostPageResult(CommonCode.SUCCESS, pageUrl);
    }
}
