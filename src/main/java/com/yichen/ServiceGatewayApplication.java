package com.yichen;

import com.yichen.config.DedupeResponseHeaderGatewayFilterFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author dengbojing
 */
@SpringBootApplication
public class ServiceGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceGatewayApplication.class, args);
    }

    @Bean
    public DedupeResponseHeaderGatewayFilterFactory dedupeResponseHeaderGatewayFilterFactory() {
        return new DedupeResponseHeaderGatewayFilterFactory();
    }


}
