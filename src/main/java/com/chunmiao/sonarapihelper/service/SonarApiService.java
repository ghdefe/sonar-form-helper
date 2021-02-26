package com.chunmiao.sonarapihelper.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;


@Service
public class SonarApiService {

    final RestTemplate restTemplate = new RestTemplate();
    private final Logger log = LoggerFactory.getLogger(SonarApiService.class);


    /**
     * 获取所有 项目 在列表的bug数
     */
    public void getProjectIssuesCount() {
        final String[] codes = {
                "squid:S2259",
                "squid:S3986",
                "squid:S2111",
                "squid:S4973",
                "squid:S2583",
                "squid:S2119",
                "squid:S2095",
                "pmd:OverrideBothEqualsAndHashcode"
        };
        final HashSet<String> codeSet = new HashSet<>();
        for (String code : codes) {
            codeSet.add(code);
        }
        File csvFile = new File(System.getProperty("user.dir") + "/result", "result-count.csv");
        final File fatherPath = new File("D:\\scanner");

        try (
                final FileWriter fileWriter = new FileWriter(csvFile, Charset.defaultCharset());
                final CSVWriter csvWriter = new CSVWriter(fileWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
        ) {
            csvWriter.writeNext(new String[]{"公司", "项目", "次数"});
            for (File company : fatherPath.listFiles()) {
                final StringBuilder sb = new StringBuilder();
                for (File project : company.listFiles()) {

                    final String url = getProjectIssuesUrl(project);
                    HashMap<String, Integer> resHashMap = new HashMap<>();
                    resHashMap = fromUrlGetResultOnlyOnCodes(url, resHashMap, codeSet);
                    final List<Map.Entry<String, Integer>> result = sortHashMap(resHashMap);
                    writeResultToCSV(result, new File(System.getProperty("user.dir") + "/result/" + company.getName(), "result.csv"));
                    int count = 0;
                    for (Map.Entry<String, Integer> stringIntegerEntry : result) {
                        count += stringIntegerEntry.getValue();
                    }

                    log.info("项目名称: " + project.getName());
                    log.info("Bug数: " + count);
                    sb.append("项目名称: " + project.getName() + "\n");
                    sb.append("Bug数: " + count + "\n");
                    csvWriter.writeNext(new String[]{company.getName(),project.getName(),String.valueOf(count)});

                }


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * 总览
     * @return
     */
    public String getAllProjectIssues() {
        final List<String> urls = getAllUrl("D:\\scanner");
        final List<Map.Entry<String, Integer>> result = getResult(urls);
        writeResultToCSV(result, new File(System.getProperty("user.dir") + "/result", "result.csv"));
        return null;
    }

    /**
     * 公司下的所有bug
     * @return
     */
    public String getAllCompanyProjectIssues() {
        final File fatherPath = new File("D:\\scanner");
        for (File company : fatherPath.listFiles()) {
            getCompanyProjectIssues(company);
        }
        return null;
    }

    /**
     * 所有项目的bug
     * @return
     */
    public String getAllCompanyAllProjectIssues() {
        final File fatherPath = new File("D:\\scanner");
        for (File company : fatherPath.listFiles()) {
            for (File project : company.listFiles()) {
                getProjectIssues(project);
            }
        }
        return null;
    }

    public String getCompanyProjectIssues(File company) {
        final List<String> urls = getCompanyUrl(company);
        final List<Map.Entry<String, Integer>> result = getResult(urls);
        writeResultToCSV(result, new File(System.getProperty("user.dir") + "/result", company.getName() + ".csv"));
        return null;
    }

    public String getProjectIssues(File project) {
        final String url = getProjectIssuesUrl(project);
        HashMap<String, Integer> resHashMap = new HashMap<>();
        resHashMap = fromUrlGetResult(url, resHashMap);
        final List<Map.Entry<String, Integer>> result = sortHashMap(resHashMap);
        writeResultToCSV(result, new File(System.getProperty("user.dir") + "/result/" + project.getParentFile().getName(), project.getName() + ".csv"));
        return null;
    }

    private void writeResultToCSV(List<Map.Entry<String, Integer>> result, File csvFile) {
        final HashMap<String, String> codeOfIssue = getCodeOfIssue(ISSUETYPE.BUG);
        if (!csvFile.getParentFile().exists()) {
            csvFile.getParentFile().mkdirs();
        }
        try (
                final FileWriter fileWriter = new FileWriter(csvFile, Charset.defaultCharset());
                final CSVWriter csvWriter = new CSVWriter(fileWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
        ) {
            csvWriter.writeNext(new String[]{"BUG代码", "描述", "次数"});
            for (Map.Entry<String, Integer> entry : result) {
                final String key = entry.getKey();
                final Integer value = entry.getValue();
                final String describe = codeOfIssue.get(key);
                csvWriter.writeNext(new String[]{key, describe, value.toString()});
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<Map.Entry<String, Integer>> getResult(List<String> urls) {
        HashMap<String, Integer> resHashMap = new HashMap<>();
        for (String url : urls) {
            resHashMap = fromUrlGetResult(url, resHashMap);
        }
        return sortHashMap(resHashMap);
    }

    private List<String> getAllUrl(String dirPath) {
        final File fatherPath = new File(dirPath);
        final File[] companies = fatherPath.listFiles();
        final LinkedList<String> urls = new LinkedList<>();
        for (File company : companies) {
            urls.addAll(getCompanyUrl(company));
        }
        return urls;
    }

    private List<String> getCompanyUrl(File company) {
        final LinkedList<String> urls = new LinkedList<>();
        for (File project : Objects.requireNonNull(company.listFiles())) {
            String url = getProjectIssuesUrl(project);
            urls.add(url);
        }
        return urls;
    }

    private String getProjectIssuesUrl(File project) {
        String projectName = project.getParentFile().getName() + "-" + project.getName();
        return "http://localhost:9000/api/issues/search?componentKeys="
                + projectName
                + "&resolved=false&types=BUG&ps=500";
    }


    private HashMap<String, Integer> fromUrlGetResult(String url, HashMap<String, Integer> hashMap) {
        String resp = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSON.parseObject(resp);
        JSONArray issues = jsonObject.getJSONArray("issues");
        Iterator<Object> iterator = issues.iterator();
        while (iterator.hasNext()) {
            final JSONObject next = (JSONObject) iterator.next();
            final String message = next.getString("rule");
            if (hashMap.containsKey(message)) {
                hashMap.replace(message, hashMap.get(message) + 1);
            } else {
                hashMap.put(message, 1);
            }
        }
        return hashMap;
    }

    private HashMap<String, Integer> fromUrlGetResultOnlyOnCodes(String url, HashMap<String, Integer> hashMap, HashSet<String> codeSet) {
        String resp = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSON.parseObject(resp);
        JSONArray issues = jsonObject.getJSONArray("issues");
        Iterator<Object> iterator = issues.iterator();
        while (iterator.hasNext()) {
            final JSONObject next = (JSONObject) iterator.next();
            final String message = next.getString("rule");
            log.info(message);
            if (!codeSet.contains(message)) {
                log.info("跳过" + message);
                continue;
            }
            if (hashMap.containsKey(message)) {
                hashMap.replace(message, hashMap.get(message) + 1);
            } else {
                hashMap.put(message, 1);
            }
        }
        return hashMap;
    }

    private List<Map.Entry<String, Integer>> sortHashMap(HashMap<String, Integer> hashMap) {
        final ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<>(hashMap.entrySet());
        entries.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        return entries;
    }

    private HashMap<String, String> getCodeOfIssue(ISSUETYPE issuetype) {

        String url = "http://localhost:9000/api/rules/search?ps=500&languages=java&types=" + issuetype.name();
        JSONObject jsonObject = restTemplate.getForObject(url, JSONObject.class);
        final JSONArray rules = jsonObject.getJSONArray("rules");
        final Iterator<Object> iterator = rules.iterator();
        final HashMap<String, String> resHashMap = new HashMap<>();
        while (iterator.hasNext()) {
            final JSONObject next = (JSONObject) iterator.next();
            final String key = next.getString("key");
            final String name = next.getString("name");
            resHashMap.put(key, name);
        }
        return resHashMap;
    }

    private void printBug(String bugFileName, List<Map.Entry<String, Integer>> entries) {
        HashMap<String, String> codeOfBug = getCodeOfIssue(ISSUETYPE.BUG);
        for (Map.Entry<String, Integer> entry : entries) {
            String key = entry.getKey();
            String msg = codeOfBug.get(key);
            System.out.println(msg);
        }
    }
}
