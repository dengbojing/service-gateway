package com.yichen.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dengbojing
 */
@Getter
@Configuration
@ConfigurationProperties(prefix = "white")
public class WhiteListConfig {
    private Set<String> list = new HashSet<>();

    /**
     * 判断是否是白名单
     *
     * @param path 当前地址
     * @return true为非白名单地址
     */
    public boolean control(String path) {
        return !list.contains(path);
    }
}
