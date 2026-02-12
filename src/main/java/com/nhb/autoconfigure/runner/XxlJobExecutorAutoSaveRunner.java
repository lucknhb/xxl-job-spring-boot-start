package com.nhb.autoconfigure.runner;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.nhb.autoconfigure.properties.XxlJobAdminConfigProperties;
import com.nhb.autoconfigure.properties.XxlJobConfigProperties;
import com.nhb.autoconfigure.utils.XxlJobUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/11 10:01
 * @description:
 */
@Slf4j
public class XxlJobExecutorAutoSaveRunner implements ApplicationRunner {
    private final XxlJobConfigProperties xxlJobConfigProperties;

    public XxlJobExecutorAutoSaveRunner(XxlJobConfigProperties xxlJobConfigProperties){
        this.xxlJobConfigProperties = xxlJobConfigProperties;
    }

    /**
     * Callback used to run the bean.
     *
     * @param args incoming application arguments
     * @throws Exception on error
     */
    @Override
    public void run(ApplicationArguments args) {
        XxlJobAdminConfigProperties xxlJobAdminProperties = xxlJobConfigProperties.getAdmin();
        if (StrUtil.isBlank(xxlJobAdminProperties.getLoginUri()) && StrUtil.isBlank(xxlJobAdminProperties.getSaveUri())) {
            log.error(">>>>>>>>> XXL_JOB SETTING MISSING loginUri OR saveUri !!! <<<<<<<<<<");
            return;
        }
        String userName = xxlJobAdminProperties.getUserName();
        String password = xxlJobAdminProperties.getPassword();
        if (StrUtil.isBlank(userName) && StrUtil.isBlank(password)) {
            log.error(">>>>>>>>> XXL_JOB SETTING MISSING userName OR password !!! <<<<<<<<<<");
            return;
        }
        //登录XXL_JOB 调度中心
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userName", userName);
        paramMap.put("password", password);
        try (HttpResponse httpResponse = HttpRequest.post(xxlJobAdminProperties.getAddresses() + xxlJobConfigProperties.getAdmin().getLoginUri())
                .form(paramMap)
                .timeout(15000)
                .execute()) {
            int status = httpResponse.getStatus();
            Assert.isTrue(200 == status, "XXL_JOB 登录失败,请检查用户名密码是否正确");
            String body = httpResponse.body();
            //{"code":200,"data":null,"msg":"Success","success":true}
            JSONObject result = new JSONObject(body);
            Assert.isTrue(200 == result.getInt("code"), "XXL_JOB 登录失败,请检查用户名密码是否正确");
            List<HttpCookie> cookies = httpResponse.getCookies();
            Integer groupId = findGroupByAppName(xxlJobConfigProperties.getExecutor().getAppName(), cookies);
            //为空的情况下 进行保存
            if (Objects.isNull(groupId)) {
                saveExecutor(cookies);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 保存执行器
     */
    public boolean saveExecutor(List<HttpCookie> cookies){
        HttpResponse response = this.getRequest(cookies)
                .method(Method.POST)
                .setUrl(xxlJobConfigProperties.getAdmin().getAddresses() + xxlJobConfigProperties.getAdmin().getSaveUri())
                .form(new HashMap<String, Object>() {{
                    this.put("addressType", "0");
                    this.put("title", xxlJobConfigProperties.getExecutor().getAppTitle());
                    this.put("appname", xxlJobConfigProperties.getExecutor().getAppName());
                }})
                .execute();
        String body = response.body();
        JSONObject result = new JSONObject(body);
        boolean code = "200".equals(result.getStr("code"));
        if (!code) {
            log.error(">>>>> ERROR: SAVE EXECUTOR GROUP NOT SUCCESS <<<<<");
        }
        return code;
    }

    /**
     * 判断是否已存在执行器
     * @return
     */
    public Integer findGroupByAppName(String appName,List<HttpCookie> cookies){
        HttpResponse response = this.getRequest(cookies)
                .method(Method.POST)
                .setUrl(xxlJobConfigProperties.getAdmin().getAddresses() + xxlJobConfigProperties.getAdmin().getGroupUri())
                .form(new HashMap<String, Object>() {{
                    this.put("appname", appName);
                }})
                .execute();
        String body = response.body();
        if (XxlJobUtil.getVersion().startsWith("2")) {
            JSONObject result = new JSONObject(body);
            JSONArray data = result.getJSONArray("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject dataJSONObject = data.getJSONObject(i);
                if (appName.equals(dataJSONObject.getStr("appname"))) {
                    return dataJSONObject.getInt("id");
                }
            }
        }else if (XxlJobUtil.getVersion().startsWith("3")) {
            JSONObject result = new JSONObject(body);
            JSONObject data = result.getJSONObject("data");
            JSONArray dataArrays = data.getJSONArray("data");
            for (int i = 0; i < dataArrays.size(); i++) {
                JSONObject dataJSONObject = dataArrays.getJSONObject(i);
                if (appName.equals(dataJSONObject.getStr("appname"))) {
                    return dataJSONObject.getInt("id");
                }
            }
        }
        return null;
    }

    /**
     * 初始化请求
     * @return
     */
    private HttpRequest getRequest(List<HttpCookie> cookies) {
        HttpRequest request = new HttpRequest(xxlJobConfigProperties.getAdmin().getAddresses());
        request.cookie(cookies);
        request.header("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        return request;
    }
}
