package com.xuecheng.auth;

import com.xuecheng.framework.client.XcServiceList;
import org.bson.internal.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class ClientTest {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;

    /**
     * 模拟客户端申请令牌（Password模式）
     */
    @Test
    public void testApplyToken() {
        //1.令牌申请URL http://localhost:40400/auth/oauth/token
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        //String applyTokenURL = serviceInstance.getUri() + "/auth/oauth/token";
        String applyTokenURL = "http://" + serviceInstance.getServiceId() + "/auth/oauth/token";
        //2.封装请求内容
        //2.1 Basic Auth
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String clientInfo = "XcWebApp:XcWebApp";
        String basicAuth = "Basic " + new String(Base64.encode(clientInfo.getBytes()));
        headers.add("Authorization", basicAuth);
        //2.2 Body请求体
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", "admin");
        body.add("password", "111111");
        //2.3 封装请求内容
        HttpEntity<MultiValueMap<String, String>> apply_content = new HttpEntity<>(body, headers);
        //3.对异常结果进行处理
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });
        //4.发送请求（申请令牌）
        ResponseEntity<Map> responseEntity = restTemplate.exchange(applyTokenURL, HttpMethod.POST, apply_content, Map.class);
        System.out.println(responseEntity.getBody());
    }
}
