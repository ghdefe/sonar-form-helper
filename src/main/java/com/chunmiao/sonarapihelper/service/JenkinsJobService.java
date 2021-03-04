package com.chunmiao.sonarapihelper.service;


import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class JenkinsJobService {


    public static final String JENKINS_HOST = "http://172.16.0.62:19011";
    public static final String USERNAME = "admin";
    public static final String TOKEN = "1178b9a119547892acc981e7246e8360f3";

    public void login() {
        try (FileInputStream fis = new FileInputStream(new File("C:\\Users\\chunmiaoz\\Desktop\\工作记录\\caiting\\sonar-form-helper\\src\\main\\resources\\config-template.xml"))) {
            final String fileString = Files.readString(Paths.get("C:\\Users\\chunmiaoz\\Desktop\\工作记录\\caiting\\sonar-form-helper\\src\\main\\resources\\config-template.xml"));

            JenkinsServer jenkinsServer = new JenkinsServer(new URI(JENKINS_HOST), USERNAME, TOKEN);
            final Map<String, Job> jobs = jenkinsServer.getJobs();
            jenkinsServer.createJob("test-job", fileString);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public void createJob() {

    }


}
