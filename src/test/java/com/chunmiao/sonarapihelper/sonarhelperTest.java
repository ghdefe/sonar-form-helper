package com.chunmiao.sonarapihelper;

import com.chunmiao.sonarapihelper.service.SonarApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

@SpringBootTest
public class sonarhelperTest {

    @Autowired
    private SonarApiService sonarApiService;

    private final String[] codes = {
            "squid:S2259",
            "squid:S3986",
            "squid:S2111",
            "squid:S4973",
            "squid:S2583",
            "squid:S2119",
            "squid:S2095",
            "pmd:OverrideBothEqualsAndHashcode"
    };

    public final HashSet<String> CODESET = new HashSet<>(Arrays.asList(codes));

    @Test
    public void generateProperties(){
        final File fatherPath = new File("D:\\scanner");
        final File propertiesFile = new File("D:\\sonar\\sonar-project-template.properties");
        sonarApiService.generatePropertiesFile(fatherPath, propertiesFile);
    }

    @Test
    public void getAllInCodes(){
        sonarApiService.getAllProjectIssuesResult(CODESET);
        sonarApiService.getCompanyIssuesResult(CODESET);
        sonarApiService.getProjectIssuesResult(CODESET);
    }



}
