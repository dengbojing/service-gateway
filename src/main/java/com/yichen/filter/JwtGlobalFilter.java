package com.yichen.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yichen.config.WhiteListConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.UUID;

/**
 * @author dengbojing
 */
@Slf4j
@Component
public class JwtGlobalFilter {

    @Value("${jwt.key}")
    private String key;

    /**
     * 客户端ID
      */
    @Value("${jwt.clientId}")
    private String clientId;

    /**
     * 超时时间，单位秒
     */
    @Value("${jwt.timeout}")
    private Long timeout;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private WhiteListConfig whiteListConfig;


    @Bean
    @Order(-1)
    public GlobalFilter a() {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            // 是否需要做权限控制
            if (whiteListConfig.control(path)) {
                String token = exchange.getRequest().getHeaders().getFirst("X-Authorization");
                // 无有效token
                if (token == null) {
                    ServerHttpResponse response = exchange.getResponse();
                    //设置headers
                    HttpHeaders httpHeaders = response.getHeaders();
                    httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
                    httpHeaders.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                    //设置body
                    Body body = new Body();
                    body.setCode(401);
                    body.setMessage("请先登录");
                    DataBuffer bodyDataBuffer = response.bufferFactory().wrap(string(body).getBytes());
                    return response.writeWith(Mono.just(bodyDataBuffer));
                }
                // 解析token
                Subject subject = subject(token);
                // 用于多个服务调用的请求追踪
                String traceId = UUID.randomUUID().toString().replace("-", "");
                // 将用户ID等内容放入Header
                return chain.filter(exchange.mutate().request(
                        exchange.getRequest()
                                .mutate()
                                .header("X-UserId", subject.getUserId())
                                .header("X-OrganizationId", subject.getOrganizationId())
                                .header("X-OrganizationType", subject.getOrganizationType().name())
                                .header("X-TraceId", traceId)
                                .header("X-PreCaller", "GATEWAY")
                                .build()).build());
            } else {
                return chain.filter(exchange);
            }
        };
    }


    @Getter
    @Setter
    private static class Body {
        private Integer code = 200;
        private String message;
    }

    /**
     * 解析Token为subject
     *
     * @param jwt token
     * @return 解析结果
     */
    private Subject subject(String jwt) {
        val subject = Jwts.parser().setSigningKey(key()).parseClaimsJws(jwt).getBody().getSubject();
        try {
            return objectMapper.readValue(subject, Subject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Subject();
    }

    /**
     * 获取私钥
     *
     * @return 私钥
     */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(Base64Utils.encode(key.getBytes()));
    }

    private String string(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "{}";
    }

    @Getter
    private enum OrganizationType {
        /**
         * 个人
         */
        PERSONAL,
        /**
         * 企业
         */
        COMPANY
    }

    @Getter
    @Setter
    private static class Subject {
        private String userId;
        private String organizationId;
        private OrganizationType organizationType;
        private String departmentId;
    }

}
