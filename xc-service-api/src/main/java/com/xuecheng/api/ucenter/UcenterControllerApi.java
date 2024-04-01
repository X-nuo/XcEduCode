package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.model.response.ResponseResult;

//@Api(value = "UCENTER 用户中心管理接口")
public interface UcenterControllerApi {
    //@ApiOperation("查询用户信息")
    public XcUserExt getUserExt(String username);
}
