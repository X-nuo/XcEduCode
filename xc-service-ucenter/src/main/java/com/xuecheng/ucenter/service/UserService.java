package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    XcUserRepository xcUserRepository;
    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;
    @Autowired
    XcMenuMapper xcMenuMapper;

    /**
     * 获取用户扩展信息
     * @param username
     * @return
     */
    public XcUserExt getUserExt(String username) {
        XcUserExt xcUserExt = new XcUserExt();
        //保存用户信息
        XcUser xcUser = this.findXcUserByName(username);
        if(xcUser == null) {
            return null;
        }
        BeanUtils.copyProperties(xcUser, xcUserExt);
        //保存用户公司ID
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(xcUser.getId());
        if(xcCompanyUser != null) {
            xcUserExt.setCompanyId(xcCompanyUser.getCompanyId());
        }
        //保存用户权限信息
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUser.getId());
        xcUserExt.setPermissions(xcMenus);

        return xcUserExt;
    }

    /**
     * 查询用户信息（根据用户名称）
     * @param username
     * @return
     */
    public XcUser findXcUserByName(String username) {
        return xcUserRepository.findXcUserByUsername(username);
    }
}
