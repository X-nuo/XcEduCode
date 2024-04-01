package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.apache.commons.lang3.StringUtils;
import org.bson.internal.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.tokenValiditySeconds}")
    long tokenValiditySeconds;

    /**
     * 用户登陆
     * @param username
     * @param password
     * @return
     */
    public AuthToken login(String username, String password) {
        //申请令牌
        AuthToken authToken = this.applyToken(username, password);
        if(authToken == null) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_ERROR);
        }
        //Redis存储令牌
        this.saveToken(authToken);
        Long expire = redisTemplate.getExpire("user_token:" + authToken.getAccess_token());
        if(expire <= 0) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_ERROR);
        }
        return authToken;
    }

    /**
     * 申请Token令牌
     * @param username
     * @param password
     * @return
     */
    private AuthToken applyToken(String username, String password) {
        //1.令牌申请URL http://localhost:40400/auth/oauth/token
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        if(serviceInstance == null) {
            ExceptionCast.cast(AuthCode.AUTH_AUTHSERVER_NOTFOUND);
        }
        //String applyTokenURL = serviceInstance.getUri() + "/auth/oauth/token";
        String applyTokenURL = "http://" + serviceInstance.getServiceId() + "/auth/oauth/token";
        //2.封装请求内容
        //2.1 Basic Auth
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String clientInfo = clientId + ":" + clientSecret;
        String basicAuth = "Basic " + new String(Base64.encode(clientInfo.getBytes()));
        headers.add("Authorization", basicAuth);
        //2.2 Body请求体
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);
        //2.3 封装请求内容
        HttpEntity<MultiValueMap<String, String>> apply_content = new HttpEntity<>(body, headers);
        //2.4 对异常结果进行处理
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //放行400和401错误（Spring Security对账号不存在和密码错误会返回400和401，在后面进行专门处理）
                if(response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });
        //4.发送请求（申请令牌）
        ResponseEntity<Map> responseEntity = restTemplate.exchange(applyTokenURL, HttpMethod.POST, apply_content, Map.class);
        //5.封装令牌信息
        Map applyResult = responseEntity.getBody();
        if(applyResult == null
                || applyResult.get("access_token") == null
                || applyResult.get("refresh_token") == null
                || applyResult.get("jti") == null) {
            String error_description = (String) applyResult.get("error_description");
            //处理账号不存在或密码错误异常
            if(StringUtils.isNotEmpty(error_description)) {
                if(error_description.equals("坏的凭证")){
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }else if (error_description.indexOf("UserDetailsService returned null")>=0) {
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }
            }
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_ERROR);
        }
        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) applyResult.get("jti"));         //jti作为用户身份的唯一标识
        authToken.setJwt_token((String) applyResult.get("access_token"));
        authToken.setRefresh_token((String) applyResult.get("refresh_token"));
        return authToken;
    }

    /**
     * Redis存储令牌
     * @param authToken
     */
    private void saveToken(AuthToken authToken) {
        //redis key
        String access_token = authToken.getAccess_token();
        //redis value
        String content = JSON.toJSONString(authToken);
        redisTemplate.boundValueOps("user_token:" + access_token).set(content, tokenValiditySeconds, TimeUnit.SECONDS);
    }

    /**
     * Redis获取令牌
     * @param accessToken
     * @return
     */
    public AuthToken getToken(String accessToken) {
        String tokenString = (String) redisTemplate.opsForValue().get("user_token:" + accessToken);
        if(tokenString != null) {
            AuthToken authToken = JSON.parseObject(tokenString, AuthToken.class);
            return authToken;
        }
        return null;
    }

    /**
     * Redis删除令牌
     * @param accessToken
     * @return
     */
    public boolean delToken(String accessToken) {
        return redisTemplate.delete("user_token:" + accessToken);
    }
}
