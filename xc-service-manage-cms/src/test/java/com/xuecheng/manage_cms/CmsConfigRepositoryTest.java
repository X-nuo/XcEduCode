package com.xuecheng.manage_cms;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsConfigRepositoryTest {
    @Autowired
    private CmsConfigRepository cmsConfigRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void testFindById() {
        Optional<CmsConfig> cmsConfig = cmsConfigRepository.findById("5a791725dd573c3574ee333f");   //轮播图数据模型
        System.out.println(cmsConfig.get());
    }

    @Test
    public void testGetConfigByHttp() {
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/get/5a791725dd573c3574ee333f", Map.class);
        System.out.println(forEntity);
    }
}
