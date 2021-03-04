package com.chunmiao.sonarapihelper.service;


import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class JenkinsJobService {

    @Value("${jenkins.host}")
    public String JENKINS_HOST;

    @Value("${jenkins.username}")
    public String USERNAME;

    @Value("${jenkins.token}")
    public String TOKEN;

    public void login() {
        try {
            String fileString = Files.readString(Paths.get(String.valueOf(ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "config-template.xml"))));
            JenkinsServer jenkinsServer = new JenkinsServer(new URI(JENKINS_HOST), USERNAME, TOKEN);
            final Map<String, Job> jobs = jenkinsServer.getJobs();
            jenkinsServer.createJob("test-job", fileString);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void createJob() {

    }


}
