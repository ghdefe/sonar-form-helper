package com.chunmiao.sonarapihelper.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;

@Component
@ConfigurationProperties(prefix = "sonar")
public class SonarProperties {

    private String token;

    private String[] codes;

    private String host;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public TreeSet<String> getCodes() {
        return new TreeSet<>(Arrays.asList(codes));
    }

    public void setCodes(String[] codes) {
        this.codes = codes;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
