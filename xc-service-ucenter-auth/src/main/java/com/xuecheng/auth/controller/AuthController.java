package com.xuecheng.auth.controller;

import com.xuecheng.api.ucenter.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class AuthController implements AuthControllerApi {
    @Autowired
    AuthService authService;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;

    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        if(StringUtils.isEmpty(loginRequest.getUsername())) {
            //请输入账号
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
        if(StringUtils.isEmpty(loginRequest.getPassword())) {
            //请输入密码
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }
        //登陆返回认证令牌
        AuthToken authToken = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        String access_token = authToken.getAccess_token();
        //Cookie保存令牌
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", access_token, cookieMaxAge, false);
        return new LoginResult(CommonCode.SUCCESS, authToken.getAccess_token());
    }

    @Override
    @GetMapping("/userjwt")
    public JwtResult userJwt() {
        //获取Cookie令牌
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String access_token = cookieMap.get("uid");
        //获取认证令牌
        AuthToken authToken = authService.getToken(access_token);
        //返回JwtToken
        return new JwtResult(CommonCode.SUCCESS, authToken.getJwt_token());
    }

    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        //获取Cookie令牌
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String access_token = cookieMap.get("uid");
        //删除认证令牌
        authService.delToken(access_token);
        //Cookie删除令牌
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", access_token, 0, false);
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
