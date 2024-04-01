package com.xuecheng.govern.gateway.filter;


import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoginGatewayFilter implements Ordered, GlobalFilter {

    @Autowired
    private AuthService authService;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        System.out.println(request.getId());
        //当Cookie中身份令牌不存在，则拒绝访问
        String access_token = authService.getTokenFromCookie(request);
        System.out.println("access_token: " + access_token);
        if(access_token == null) {
            return access_denied(exchange.getResponse());
        }
        //当JWT令牌已过期，则拒绝访问
        long expire = authService.getExpire(access_token);
        System.out.println("expire: " + expire);
        if(expire <=0 ) {
            return access_denied(exchange.getResponse());
        }
        //当JWT令牌不存在，则拒绝访问
        String jwt = authService.getJwtFromHeader(request);
        System.out.println("jwt: " + jwt);
        if(jwt == null) {
            return access_denied(exchange.getResponse());
        }

        return chain.filter(exchange);
    }

    /**
     * 拒绝前端访问
     */
    private Mono<Void> access_denied(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        DataBuffer buffer = response.bufferFactory().wrap(JSON.toJSONString(new ResponseResult(CommonCode.UNAUTHENTICATED)).getBytes());
        return response.writeWith(Mono.just(buffer));
    }

}
