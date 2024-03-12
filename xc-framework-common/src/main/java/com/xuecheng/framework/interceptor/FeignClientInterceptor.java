package com.xuecheng.framework.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 微服务调用拦截器
 * 目的：远程调用添加Authorization JWT头部信息
 */
public class FeignClientInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();
            if(headerNames != null) {
                while(headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    String value = request.getHeader(name);
                    if(name.equals("authorization")) {
                        requestTemplate.header(name, value);
                    }
                }
            }
        }
    }
}
