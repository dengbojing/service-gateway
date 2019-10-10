package com.yichen.config;

import com.yichen.filter.RateLimitByJwtGatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author dengbojing
 */
@Configuration
public class GateWayConfiguration {


    /**
     * routes:
     *         - id: service-project
     *           uri: lb://service-project
     *           predicates:
     *            - Path=/project/**
     *           filters:
     *             - StripPrefix=1
     *         - id: service-project
     *           uri: lb://service-worker
     *           predicates:
     *             - Path=/worker/**
     *           filters:
     *             - StripPrefix=1
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("service-project",r -> r.path("/core/**")
                        .filters(f -> f.stripPrefix(1).filter(new RateLimitByJwtGatewayFilter(10,1, Duration.ofSeconds(1))))
                        .uri("lb://service-core"))
                .route("service-manager",r -> r.path("/manager/**")
                        .filters(f -> f.stripPrefix(1).filter(new RateLimitByJwtGatewayFilter(10,1, Duration.ofSeconds(1))))
                        .uri("lb://service-manager"))
                .build();
    }



}
