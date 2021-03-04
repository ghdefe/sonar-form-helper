package com.chunmiao.sonarapihelper;

import com.chunmiao.sonarapihelper.service.JenkinsJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JenkinsJobServiceTest {
    @Autowired
    private JenkinsJobService jenkinsJobService;

    @Test
    public void triggerTest(){
        jenkinsJobService.login();
    }
}
