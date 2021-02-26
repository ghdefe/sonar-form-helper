package com.chunmiao.sonarapihelper.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chunmiao.sonarapihelper.service.SonarApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.*;

@RestController
public class SonarApiController {

    @Autowired
    private SonarApiService sonarApiService;

    @GetMapping("/allProjectIssues")
    public String getAllProjectIssues(){
        sonarApiService.getAllProjectIssues();
        return null;
    }

    @GetMapping("/companyProjectIssues")
    public String getCompanyProjectIssues(){
        return null;

    }

    @GetMapping("/singleProjectIssues")
    public String getSingleProjectIssues(){
        return null;

    }




}
