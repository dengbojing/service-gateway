package com.yichen.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dengbojing
 */
@Slf4j
@Getter
@Setter
@AllArgsConstructor
public class RateLimitByJwtGatewayFilter implements GatewayFilter, Ordered {
    private int capacity;
    private int refillTokens;
    private Duration refillDuration;
    @Value("${rateLimit,enable}")
    private boolean enableRateLimit;

    private static final Map<String, Bucket> CACHE = new ConcurrentHashMap<>();

    private RateLimitByJwtGatewayFilter(){}

    public RateLimitByJwtGatewayFilter(int capacity, int refillTokens, Duration refillDuration){
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillDuration = refillDuration;
    }

    private Bucket createNewBucket() {
        Refill refill = Refill.intervally(refillTokens,refillDuration);
        Bandwidth limit = Bandwidth.classic(capacity,refill);
        return Bucket4j.builder().addLimit(limit).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enableRateLimit){
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("X-Authorization");
        String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        Bucket bucket = CACHE.computeIfAbsent(ip, k -> createNewBucket());

        log.debug("IP: " + ip + ",TokenBucket Available Tokens: " + bucket.getAvailableTokens());
        if (bucket.tryConsume(1)) {
            return chain.filter(exchange);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
