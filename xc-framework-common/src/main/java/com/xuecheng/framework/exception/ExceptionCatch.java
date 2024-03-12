package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;



/**
 * 异常捕获类
 */
@ControllerAdvice
public class ExceptionCatch {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);
    //使用ImmutableMap集合存放非自定义异常类型（不可预知异常）和错误代码
    public static ImmutableMap<Class<? extends Throwable>, ResultCode> EXCEPTIONS;
    //使用ImmutableMap.Builder来添加一些基础的异常类型
    protected static ImmutableMap.Builder<Class<? extends Throwable>, ResultCode> builder = ImmutableMap.builder();
    static {
        builder.put(HttpMessageNotReadableException.class, CommonCode.INVALID_PARAM);
        //... 添加一些基础异常信息
    }

    /**
     * 捕获自定义异常(CustionException)方法
     * @return
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult catchCustomException(CustomException e) {
        LOGGER.error("catch exception: {}\r\nexception: ", e.getMessage(), e);
        return new ResponseResult(e.getResultCode());
    }

    /**
     * 捕获不可预知异常(Exception)方法
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult catchException(Exception e) {
        LOGGER.error("catch exception: {}\r\nexception: ", e.getMessage(), e);
        if(EXCEPTIONS == null) {
            EXCEPTIONS = builder.build();
        }
        //根据捕获到的未知异常信息，在ImmutableMap中查找，获取相应的错误代码
        final ResultCode resultCode = EXCEPTIONS.get(e.getClass());
        if(resultCode != null) {
            return new ResponseResult(resultCode);
        }else {
            //未查找到该异常，统一返回'99999'错误代码
            return new ResponseResult(CommonCode.SERVER_ERROR);
        }
    }
}
