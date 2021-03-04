package com.chunmiao.sonarapihelper;

import com.chunmiao.sonarapihelper.service.JenkinsJobService;
import com.chunmiao.sonarapihelper.service.SonarApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class sonarhelperTest {

    @Autowired
    private SonarApiService sonarApiService;

    @Autowired
    private JenkinsJobService jenkinsJobService;

    @Test
    public void testSonar(){
    }

    @Test
    public void testJenkins(){
        jenkinsJobService.login();
    }

}
