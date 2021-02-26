package com.chunmiao.sonarapihelper.service;

enum SEVERITIES {

    // 阻断
    BLOCKER("BLOCKER"),
    // 严重
    CRITICAL("CRITICAL"),
    // 主要
    MAJOR("MAJOR"),
    // 次要
    MINOR("MINOR");




    private final String lever;

    SEVERITIES(String lever) {
        this.lever = lever;
    }

    public String getLever() {
        return lever;
    }
}
