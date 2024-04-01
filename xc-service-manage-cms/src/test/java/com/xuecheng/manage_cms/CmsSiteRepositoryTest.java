package com.xuecheng.manage_cms;

import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsSiteRepositoryTest {
    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    /**
     * 查询所有站点信息
     */
    @Test
    public void testFindAll() {
        List<CmsSite> all = cmsSiteRepository.findAll();
        System.out.println(all);
    }

    /**
     * 根据id查询站点信息
     */
    @Test
    public void testFindById() {
        Optional<CmsSite> cmsSite = cmsSiteRepository.findById("5a751fab6abb5044e0d19ea1");
        System.out.println(cmsSite.get());
    }
}
