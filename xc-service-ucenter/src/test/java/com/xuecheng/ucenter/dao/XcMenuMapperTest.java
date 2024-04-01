package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class XcMenuMapperTest {
    @Autowired
    XcMenuMapper xcMenuMapper;

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void testSelectXcMenu() {
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId("49");
        for(XcMenu xcMenu : xcMenus) {
            System.out.println(xcMenu.getCode());
        }
    }

    @Test
    public void testAPI() {
        ResponseEntity<String> entity = restTemplate.getForEntity("http://localhost:301001/xc-service-ucenter/ucenter/getuserext?username={1}", String.class, "xnuo");
        System.out.println(entity.getBody());
    }
}