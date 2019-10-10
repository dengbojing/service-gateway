package com.yichen.config;

/**
 * @author dengbojing
 */
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义处理404
 * @author dengbojing
 */
@Slf4j
public class SimpleExceptionHandler extends DefaultErrorWebExceptionHandler {

    /**
     * Create a new {@code DefaultErrorWebExceptionHandler} instance.
     *
     * @param errorAttributes    the error attributes
     * @param resourceProperties the resources configuration properties
     * @param errorProperties    the error configuration properties
     * @param applicationContext the current application context
     */
    public SimpleExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    /**
     * 指定响应处理方法为JSON处理的方法
     *
     * @param errorAttributes errorAttributes
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * 根据code获取对应的HttpStatus
     *
     * @param errorAttributes errorAttributes
     */
    @Override
    protected HttpStatus getHttpStatus(Map<String, Object> errorAttributes) {
        if (null == errorAttributes) {
            return HttpStatus.valueOf(500);
        }
        val status = errorAttributes.get("code");
        return status != null ? HttpStatus.valueOf((int) status) : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        Throwable error = super.getError(request);
        if ("404 NOT_FOUND".equals(error.getMessage())) {
            log.info("404:" + request.uri().getPath());
            Map<String, Object> response = new HashMap<>(2);
            response.put("code", 404);
            response.put("message", "未注册地址");
            return response;
        }
        return super.getErrorAttributes(request, includeStackTrace);
    }
}
