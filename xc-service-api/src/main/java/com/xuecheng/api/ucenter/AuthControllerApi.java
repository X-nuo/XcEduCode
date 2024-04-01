package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.model.response.ResponseResult;

//@Api(value = "AUTH 用户认证接口")
public interface AuthControllerApi {
    //@ApiOperation("用户登陆")
    public LoginResult login(LoginRequest loginRequest);
    //@ApiOperation("用户信息")
    public JwtResult userJwt();
    //@ApiOperation("用户退出")
    public ResponseResult logout();
}
