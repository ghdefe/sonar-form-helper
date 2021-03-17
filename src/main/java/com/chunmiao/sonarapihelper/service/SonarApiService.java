package com.chunmiao.sonarapihelper.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.chunmiao.sonarapihelper.service.finalEnum.ISSUETYPE;
import com.opencsv.CSVWriter;
import com.sun.source.util.Trees;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;


@Service
public class SonarApiService {

    @Value("${scanner.dir}")
    private String scannerDir;

    @Value("${sonar.token}")
    public String SONAR_TOKEN;

    @Autowired
    private SonarProperties sonarProperties;

    private final Logger log = LoggerFactory.getLogger(SonarApiService.class);

    private final OkHttpClient okHttpClient = new OkHttpClient();

    private final String resultDir = System.getProperty("user.dir") + "/result/";


    public void generatePropertiesFile(File fatherPath, File propertiesTemplate) {
        int i = 0;
        final File[] dirs = fatherPath.listFiles();
        for (File dir : dirs) {
            for (File dirProject : Objects.requireNonNull(dir.listFiles())) {
                System.out.println("当前执行第" + (++i) + "个目录\n执行目录: " + dirProject.getName());
                final File propertiesFile = new File(dirProject, "sonar-project.properties");
                if (propertiesFile.exists()) {
                    propertiesFile.delete();
                }
                // 修改模板
                replaceAndCopyString(
                        propertiesTemplate
                        , propertiesFile
                        , "\\$\\{myProjectName\\}"
                        , dir.getName() + "-" + dirProject.getName()
                );
//                excuteCMDCommand("cmd.exe /k D:\\sonar\\sonar-scanner-4.6.0.2311-windows\\bin\\sonar-scanner.bat", dirProject);
            }
        }

    }

    /**
     * 获取Sonar中所有项目名称
     *
     * @return
     */
    public String[] getAllProjectInSonar() {
        String[] res = null;
        String url = "http://" + sonarProperties.getHost() + "/api/components/search?qualifiers=TRK&ps=500";
        Request request = new Request.Builder()
                .header("Authorization", SONAR_TOKEN)
                .url(url)
                .get()
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            log.info("获取Sonar中所有项目名: " + response.code());
            if (response.code() == 200) {
                JSONObject jsonObject = JSONObject.parseObject(response.body().string());
                JSONArray components = jsonObject.getJSONArray("components");
                TreeSet<String> resSet = new TreeSet<>();
                components.listIterator().forEachRemaining(o -> {
                    JSONObject o1 = (JSONObject) o;
                    String key = o1.getString("key");
                    resSet.add(key);
                });
                res = resSet.toArray(new String[]{});
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return res;
    }

    /**
     * 获取项目扫描代码行数
     */
    public String getProjectCodeLine(String project) {
        String url = "http://" + sonarProperties.getHost() + "/api/measures/component?additionalFields=periods&component=" + project + "&metricKeys=ncloc";
        Request request = new Request.Builder()
                .header("Authorization", SONAR_TOKEN)
                .url(url)
                .get()
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() == 200) {
                JSONObject jsonObject = JSONObject.parseObject(response.body().string());
                String res = jsonObject.getJSONObject("component").getJSONArray("measures").getJSONObject(0).getString("value");
                return res;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public HashMap<String, Integer> getProjectIssuesCount(String project) {
        String url = "http://" + sonarProperties.getHost() + "/api/issues/search?componentKeys=" + project + "&facets=rules&languages=java&ps=500";
        Request request = new Request.Builder()
                .header("Authorization", SONAR_TOKEN)
                .url(url)
                .get()
                .build();
        HashMap<String, Integer> resMap = new HashMap<>();
        getUrlToJson(request).ifPresentOrElse(jsonObject -> {
            jsonObject.getJSONArray("facets").getJSONObject(0)
                    .getJSONArray("values")
                    .iterator().forEachRemaining(o -> {
                JSONObject o1 = (JSONObject) o;
                String val = o1.getString("val"); // bug代码
                int count = o1.getInteger("count"); // bug数量
                resMap.put(val,count);
            });
        },() -> {

        });

        return resMap;
    }

    /**
     * 生成第二种统计报告表
     */
    public void getSecondCountRepo() {
        String[] projects = new String[]{
                "bs-be-budget-config"
        };
        String[] bugCodes = new String[]{
                "squid:S2259",
                "squid:S3986",
                "squid:S2111",
                "squid:S4973",
                "squid:S2583",
                "squid:S2119",
                "squid:S2095",
                "pmd:OverrideBothEqualsAndHashcode"
        };
        File csvFile = new File(System.getProperty("user.dir") + "result/", "result.csv");
        try (
                final FileWriter fileWriter = new FileWriter(csvFile, Charset.defaultCharset(), true);
                final CSVWriter csvWriter = new CSVWriter(fileWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
        ) {


            for (String project : projects) {
                String url = getProjectIssuesUrl(project);
                HashMap<String, Integer> resMap = new HashMap<>();
                fromUrlGetResult(url, resMap);

                csvWriter.writeNext(new String[]{"", project, });

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    /**
     * 总览
     */
    public void getAllProjectIssuesResult() {
        final List<String> urls = getAllUrl(scannerDir);
        final HashMap<String, Integer> result = getResult(urls);
        writeResultToCSV(result, new File(resultDir, "Result.csv"), "", "");
    }

    /**
     * 总览存在codes中的bug数
     */
    public void getAllProjectIssuesResult(Set<String> codeSet) {
        final List<String> urls = getAllUrl(scannerDir);
        final HashMap<String, Integer> result = getResult(urls, codeSet);
        writeResultToCSV(result, new File(resultDir, "Result-in-codes.csv"), "", "");
    }

    /**
     * 统计公司bug
     */
    public void getCompanyIssuesResult() {
        final File fatherPath = new File(scannerDir);
        for (File company : Objects.requireNonNull(fatherPath.listFiles())) {
            getCompanyIssues(company);
        }
    }

    /**
     * 统计各公司存在codes中的bug数
     */
    public void getCompanyIssuesResult(Set<String> codeSet) {
        final File fatherPath = new File(scannerDir);
        for (File company : Objects.requireNonNull(fatherPath.listFiles())) {
            getCompanyIssues(company, codeSet);
        }
    }

    /**
     * 统计项目bug
     */
    public void getProjectIssuesResult() {
        final File fatherPath = new File(scannerDir);
        for (File company : Objects.requireNonNull(fatherPath.listFiles())) {
            for (File project : Objects.requireNonNull(company.listFiles())) {
                getProjectIssues(project);
            }
        }
    }

    /**
     * 统计各项目存在codes中的bug数
     */
    public void getProjectIssuesResult(Set<String> codeSet) {
        final File fatherPath = new File(scannerDir);
        for (File company : Objects.requireNonNull(fatherPath.listFiles())) {
            for (File project : Objects.requireNonNull(company.listFiles())) {
                getProjectIssues(project, codeSet);
            }
        }
    }


    public void getCompanyIssues(File company) {
        final List<String> urls = getCompanyUrl(company);
        final HashMap<String, Integer> result = getResult(urls);
        writeResultToCSV(result, new File(resultDir, "Company.csv"), company.getName(), "");
    }

    public void getCompanyIssues(File company, Set<String> bugCodeSet) {
        final List<String> urls = getCompanyUrl(company);
        final HashMap<String, Integer> result = getResult(urls, bugCodeSet);
        writeResultToCSV(result, new File(resultDir, "Company-in-codes.csv"), company.getName(), "");
    }

    public void getProjectIssues(File project) {
        final String url = getProjectIssuesUrl(project);
        HashMap<String, Integer> resHashMap = new HashMap<>();
        fromUrlGetResult(url, resHashMap);
        writeResultToCSV(resHashMap, new File(resultDir, "Project.csv"), project.getParentFile().getName(), project.getName());
    }

    public void getProjectIssues(File project, Set<String> bugCodeSet) {
        final String url = getProjectIssuesUrl(project);
        HashMap<String, Integer> resHashMap = new HashMap<>();
        fromUrlGetResult(url, resHashMap, bugCodeSet);
        writeResultToCSV(resHashMap, new File(resultDir, "Project-in-codes.csv"), project.getParentFile().getName(), project.getName());
    }

    private void writeResultToCSV(HashMap<String, Integer> result, File csvFile, String company, String project) {
        final HashMap<String, String> codeOfIssue = getCodeOfIssue();
        if (!csvFile.getParentFile().exists()) {
            csvFile.getParentFile().mkdirs();
        }
        try (
                final FileWriter fileWriter = new FileWriter(csvFile, Charset.defaultCharset(), true);
                final CSVWriter csvWriter = new CSVWriter(fileWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
        ) {
            if (!csvFile.exists()) {
                csvWriter.writeNext(new String[]{"公司", "项目", "BUG代码", "描述", "次数"});
            }
            TreeSet<String> codes = sonarProperties.getCodes();
            for (String code : codes) {
                Integer count = result.get(code) == null ? 0 : result.get(code);
                String describe = codeOfIssue.get(code);
                csvWriter.writeNext(new String[]{company, project, code, describe, count.toString()});
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 获取结果
     *
     * @param urls
     * @return Map<bug代码, 次数>
     */
    private HashMap<String, Integer> getResult(List<String> urls) {
        HashMap<String, Integer> resHashMap = new HashMap<>();
        for (String url : urls) {
            fromUrlGetResult(url, resHashMap);
        }
        return resHashMap;
    }

    /**
     * 获取在统计列表的bug结果
     *
     * @param urls
     * @param bugCodes
     * @return Map<bug代码, 次数>
     */
    private HashMap<String, Integer> getResult(List<String> urls, Set<String> bugCodes) {
        HashMap<String, Integer> resHashMap = new HashMap<>();
        for (String url : urls) {
            fromUrlGetResult(url, resHashMap, bugCodes);
        }
        return resHashMap;
    }

    /**
     * 获取扫描目录所有project的api链接
     *
     * @param dirPath
     * @return
     */
    private List<String> getAllUrl(String dirPath) {
        final File fatherPath = new File(dirPath);
        final File[] companies = fatherPath.listFiles();
        final LinkedList<String> urls = new LinkedList<>();
        for (File company : companies) {
            urls.addAll(getCompanyUrl(company));
        }
        return urls;
    }

    /**
     * 获取待统计公司所有project链接
     *
     * @param company
     * @return
     */
    private List<String> getCompanyUrl(File company) {
        final LinkedList<String> urls = new LinkedList<>();
        for (File project : Objects.requireNonNull(company.listFiles())) {
            String url = getProjectIssuesUrl(project);
            urls.add(url);
        }
        return urls;
    }

    /**
     * 获取project链接
     *
     * @param project
     * @return
     */
    private String getProjectIssuesUrl(File project) {
        String projectName = project.getParentFile().getName() + "-" + project.getName();
        return "http://" + sonarProperties.getHost() + "/api/issues/search?componentKeys="
                + projectName
                + "&resolved=false&types=BUG&ps=500";
    }

    private String getProjectIssuesUrl(String project) {
        return "http://" + sonarProperties.getHost() + "/api/issues/search?componentKeys="
                + project
                + "&resolved=false&types=BUG&ps=500";
    }


    private void fromUrlGetResult(String url, HashMap<String, Integer> resultHashMap) {
        Request request = new Request.Builder()
                .header("Authorization", SONAR_TOKEN)
                .url(url)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        try (Response execute = call.execute();) {
            String resp = execute.body().string();
            JSONObject jsonObject = JSON.parseObject(resp);
            JSONArray issues = jsonObject.getJSONArray("issues");
            Iterator<Object> iterator = issues.iterator();
            while (iterator.hasNext()) {
                final JSONObject next = (JSONObject) iterator.next();
                final String message = next.getString("rule");
                if (resultHashMap.containsKey(message)) {
                    resultHashMap.replace(message, resultHashMap.get(message) + 1);
                } else {
                    resultHashMap.put(message, 1);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    private void fromUrlGetResult(String url, HashMap<String, Integer> resultHashMap, Set<String> codeSet) {
        Request request = new Request.Builder()
                .header("Authorization", SONAR_TOKEN.trim())
//                .url(url)
                .url("http://172.16.0.41:11190/api/issues/search?componentKeys=bs-be-basicinfo&resolved=false&types=BUG&ps=500")
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        try (Response response = call.execute()) {
            String resp = response.body().string();
            JSONObject jsonObject = JSON.parseObject(resp);
            JSONArray issues = jsonObject.getJSONArray("issues");
            for (Object issue : issues) {
                final JSONObject next = (JSONObject) issue;
                final String message = next.getString("rule");
                log.info(message);
                if (!codeSet.contains(message)) {
                    log.info("跳过" + message);
                    continue;
                }
                if (resultHashMap.containsKey(message)) {
                    resultHashMap.replace(message, resultHashMap.get(message) + 1);
                } else {
                    resultHashMap.put(message, 1);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }

    private List<Map.Entry<String, Integer>> sortHashMap(HashMap<String, Integer> hashMap) {
        final ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<>(hashMap.entrySet());
        entries.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        return entries;
    }

    private HashMap<String, String> getCodeOfIssue() {
        String url = "http://" + sonarProperties.getHost() + "/api/rules/search?ps=500&languages=java&types=" + ISSUETYPE.BUG.name();
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        final HashMap<String, String> resHashMap = new HashMap<>();
        getUrlToJson(request).ifPresent(jsonObject -> {
            final JSONArray rules = jsonObject.getJSONArray("rules");
            for (Object rule : rules) {
                final JSONObject next = (JSONObject) rule;
                final String key = next.getString("key");
                final String name = next.getString("name");
                resHashMap.put(key, name);
            }
        });
        return resHashMap;
    }

    private void replaceAndCopyString(File template, File file, String source, String target) {
        String temp;
        try (
                final FileReader fileReader = new FileReader(template);
                BufferedReader br = new BufferedReader(fileReader);
                final FileWriter fileWriter = new FileWriter(file);
        ) {
            while ((temp = br.readLine()) != null) {
                temp = temp.replaceAll(source, target);
                fileWriter.write(temp);
                fileWriter.append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Optional<JSONObject> getUrlToJson(Request request) {
        JSONObject jsonObject = null;
        try {
            Response response = okHttpClient.newCall(request).execute();
            assert response.body() != null;
            String string = response.body().string();
            log.error(string);
            jsonObject = JSONObject.parseObject(string);
            jsonObject = JSONObject.parseObject(response.body().string());

        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return Optional.ofNullable(jsonObject);

    }
}
