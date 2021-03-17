package com.chunmiao.sonarapihelper;

import com.chunmiao.sonarapihelper.service.JenkinsJobService;
import com.chunmiao.sonarapihelper.service.SonarApiService;
import com.chunmiao.sonarapihelper.service.SonarProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;

@SpringBootTest
public class SonarHelperTest {

    @Autowired
    private SonarApiService sonarApiService;

    @Autowired
    private SonarProperties sonarProperties;



    @Test
    public void generateProperties(){
        final File fatherPath = new File("D:\\scanner");
        final File propertiesFile = new File("D:\\sonar\\sonar-project-template.properties");
        sonarApiService.generatePropertiesFile(fatherPath, propertiesFile);
    }

    @Test
    public void getAllInCodes(){
        TreeSet<String> CODESET = sonarProperties.getCodes();

//        // 获取三种报告
//        sonarApiService.getAllProjectIssuesResult(CODESET);
//        sonarApiService.getCompanyIssuesResult(CODESET);
//        sonarApiService.getProjectIssuesResult(CODESET);

        // 获取第二种模板报告
        sonarApiService.getSecondCountRepo();
    }






}
