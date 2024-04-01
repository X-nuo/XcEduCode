package com.xuecheng.manage_cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsTemplateFileResult;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.service.TemplateService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {
    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TemplateService templateService;

    @Test
    public void testFindAll() {
        List<CmsPage> cmsPages = cmsPageRepository.findAll();
        System.out.println(cmsPages);
    }

    /**
     * 分页查询
     */
    @Test
    public void testFindByPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(pageable);
        System.out.println(cmsPages.getTotalElements());
    }

    /**
     * 条件查询
     */
    @Test
    public void testFindByCriteria() {
        //构建条件
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
        Example<CmsPage> example = Example.of(cmsPage);
        //条件查询
        List<CmsPage> all = cmsPageRepository.findAll(example);
        System.out.println(all);
    }

    /**
     * 页面静态化
     */
    @Test
    public void testGenerateHtml() {
        String pageId = "643e55041bc1e341e1b79a87";
        Optional<CmsPage> cmsPageOptional = cmsPageRepository.findById(pageId);
        if(cmsPageOptional.isPresent()) {
            CmsPage cmsPage = cmsPageOptional.get();
            //1.获取数据模型
            String dataUrl = cmsPage.getDataUrl();
            ResponseEntity<Map> mapEntity = restTemplate.getForEntity(dataUrl, Map.class);
            Map body = mapEntity.getBody();
            //2.获取页面模版
            String templateId = cmsPage.getTemplateId();
            CmsTemplateFileResult templateFileResult = templateService.getTemplateFile(templateId);
            String templateFile = templateFileResult.getTemplateContent();
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
                //3.4 页面静态化
                String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, body);
                System.out.println(html);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (TemplateException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void testGenerateHtml2() throws IOException, TemplateException {
        //获取课程视图信息
        ResponseEntity<Map> forEntity =
                restTemplate.getForEntity("http://localhost:31200/course/courseview/ff80808187b726610187b72c95d30002", Map.class);
        Map body = forEntity.getBody();
        //模版配置
        Configuration configuration = new Configuration(Configuration.getVersion());
        //设置模版加载路径
        configuration.setDirectoryForTemplateLoading(new File("/Users/xnuo/Desktop/XcEduProject/template/"));
        configuration.setDefaultEncoding("utf-8");
        //加载模版
        Template template = configuration.getTemplate("course.ftl");
        //静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, body);
        System.out.println(content);

    }
}
