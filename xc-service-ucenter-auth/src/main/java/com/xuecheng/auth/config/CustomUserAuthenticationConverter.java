package com.xuecheng.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CustomUserAuthenticationConverter extends DefaultUserAuthenticationConverter {
    @Autowired
    UserDetailsService userDetailsService;

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        HashMap map = new LinkedHashMap();
        Object principal = authentication.getPrincipal();
        UserJwt userJwt = null;
        if(principal instanceof  UserJwt){
            userJwt = (UserJwt) principal;
        }else{
            //refresh_token默认不去调用userDetailService获取用户信息，这里我们手动去调用，得到 UserJwt
            UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());
            userJwt = (UserJwt) userDetails;
        }
        map.put("user_name", authentication.getName());
        map.put("name", userJwt.getName());
        map.put("id", userJwt.getId());
        map.put("utype",userJwt.getUtype());
        map.put("userpic",userJwt.getUserpic());
        map.put("companyId",userJwt.getCompanyId());
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            map.put("authorities", AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }

        return map;
    }


}
