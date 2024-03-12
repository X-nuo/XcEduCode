package com.xuecheng.framework.domain.ucenter.ext;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by mrt on 2018/5/21.
 */
@Data
@ToString
@NoArgsConstructor
public class AuthToken {
    String access_token;    //jti（用户唯一标识）
    String refresh_token;   //刷新token
    String jwt_token;       //jwt令牌（访问token）
}
