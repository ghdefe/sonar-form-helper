package com.chunmiao.sonarapihelper;

import com.chunmiao.sonarapihelper.service.SonarApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class sonarhelperTest {

    @Autowired
    private SonarApiService sonarApiService;


    @Test
    public void test(){
//        final String allProject = sonarApiService.getAllProjectIssues();
        sonarApiService.getProjectIssues();
        sonarApiService.getAllProjectIssues();
        sonarApiService.getProjectIssues();
        sonarApiService.getAllCompanyProjectIssues();
    }

}
