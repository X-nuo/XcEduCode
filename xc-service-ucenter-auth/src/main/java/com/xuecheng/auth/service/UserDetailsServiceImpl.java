package com.xuecheng.auth.service;

import com.xuecheng.auth.client.UserClient;
import com.xuecheng.auth.config.UserJwt;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //获取用户信息（远程调用UCENTER服务）
        XcUserExt userExt = userClient.getUserExt(username);
        if(userExt == null){
            return null;
        }
        //获取正确密码（已加密）
        String password = userExt.getPassword();
        //获取用户权限
        List<String> permissionList = new ArrayList<>();
        List<XcMenu> xcMenus = userExt.getPermissions();
        for(XcMenu xcMenu : xcMenus) {
            permissionList.add(xcMenu.getCode());
        }
        String user_permission_string  = StringUtils.join(permissionList.toArray(), ",");
        //创建UserJwt对象
        UserJwt userDetails = new UserJwt(username,
                password,
                AuthorityUtils.commaSeparatedStringToAuthorityList(user_permission_string));
        userDetails.setId(userExt.getId());
        userDetails.setUtype(userExt.getUtype());
        userDetails.setCompanyId(userExt.getCompanyId());
        userDetails.setName(userExt.getName());
        userDetails.setUserpic(userExt.getUserpic());

        return userDetails;
    }
}
