package com.xuecheng.manage_cms;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.SysDictionaryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SysDictionaryRepositoryTest {
    @Autowired
    private SysDictionaryRepository sysDictionaryRepository;

    @Test
    public void testFindByDicType() {
        System.out.println(sysDictionaryRepository.findByDType("200"));
    }

    @Test
    public void testQueryByExample() {
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("dName", ExampleMatcher.GenericPropertyMatchers.contains());
        SysDictionary sysDictionary = new SysDictionary();
        sysDictionary.setDName("课程");

        Example<SysDictionary> example = Example.of(sysDictionary, exampleMatcher);

        List<SysDictionary> all = sysDictionaryRepository.findAll(example);
        System.out.println(all);
    }
}
