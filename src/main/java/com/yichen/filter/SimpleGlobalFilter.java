package com.yichen.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author dengbojing
 */
@Slf4j
@Component
public class SimpleGlobalFilter {

    private static final String ELAPSED_TIME_BEGIN = "elapsedTimeBegin";

    @Bean
    @Order(-100)
    public GlobalFilter b() {
        return (exchange, chain) -> {
            exchange.getAttributes().put(ELAPSED_TIME_BEGIN, System.currentTimeMillis());
            return chain.filter(exchange).then(
                    Mono.fromRunnable(() -> {
                        Long startTime = exchange.getAttribute(ELAPSED_TIME_BEGIN);
                        if (startTime != null) {
                            String sb = exchange.getRequest().getURI().getRawPath() +
                                    ": " +
                                    (System.currentTimeMillis() - startTime) +
                                    "ms";
                            log.info(sb);
                        }
                    })
            );
        };
    }
}
