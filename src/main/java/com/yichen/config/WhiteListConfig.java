package com.yichen.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author dengbojing
 */
@Getter
@Configuration
@ConfigurationProperties(prefix = "white")
public class WhiteListConfig {
    private final Set<String> list = new HashSet<>();

    /**
     * 判断是否是白名单
     *
     * @param path 当前地址
     * @return true为非白名单地址
     */
    public boolean control(String path) {
        List<String> tempList = list.stream().filter(path::startsWith).collect(Collectors.toList());
        return tempList.size() == 0;
    }
}
