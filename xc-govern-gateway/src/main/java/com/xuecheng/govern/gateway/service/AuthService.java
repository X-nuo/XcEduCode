package com.xuecheng.govern.gateway.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 获取Cookie令牌
     * @param request
     * @return
     */
    public String getTokenFromCookie(ServerHttpRequest request) {
        HttpCookie uid = request.getCookies().getFirst("uid");
        String access_token = uid.getValue();
        if(StringUtils.isEmpty(access_token)) {
            return null;
        }
        return access_token;
    }

    /**
     * 获取JWT令牌（Header Authorization）
     * @param request
     * @return
     */
    public String getJwtFromHeader(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        if(StringUtils.isEmpty(authorization) || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization;
    }

    /**
     * 查询JWT令牌有效期（Redis）
     * @param access_token
     * @return
     */
    public long getExpire(String access_token) {
        Long expire = redisTemplate.getExpire("user_token:" + access_token);
        return expire;
    }
}
