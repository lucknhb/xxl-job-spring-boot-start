package com.nhb.autoconfigure.config;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.nhb.autoconfigure.listener.InterNetCloseHandler;
import com.nhb.autoconfigure.properties.XxlJobAdminConfigProperties;
import com.nhb.autoconfigure.properties.XxlJobConfigProperties;
import com.nhb.autoconfigure.properties.XxlJobExecutorConfigProperties;
import com.nhb.autoconfigure.runner.XxlJobExecutorAutoSaveRunner;
import com.nhb.autoconfigure.utils.InterNetUtil;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/9 15:43
 * @description: xxl_job自动配置
 */
@Slf4j
@EnableConfigurationProperties(XxlJobConfigProperties.class)
@Import(InterNetUtil.class)
@ConditionalOnProperty(prefix = XxlJobConfigProperties.PREFIX, value = "enable", havingValue = "true", matchIfMissing = true)
public class XxlJobAutoConfiguration {

    /**
     * SPRING 环境下注册该模块的执行器 并非 会直接在调度中心执行器管理中手动保存操作
     * @param xxlJobConfigProperties
     * @param environment
     * @param interNetUtil
     * @return
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor(XxlJobConfigProperties xxlJobConfigProperties,
                                               Environment environment,
                                               InterNetUtil interNetUtil) {
        log.debug(">>>>>>>>>>> XXL-JOB CLIENT CONFIG INIT FOR SPRING <<<<<<<<<<<");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        //调度中心相关配置
        XxlJobAdminConfigProperties xxlJobAdminProperties = xxlJobConfigProperties.getAdmin();
        String addresses = xxlJobAdminProperties.getAddresses();
        Assert.notBlank(addresses,"ERROR: XXL_JOB ADDRESS IS EMPTY! PLEASE SETTING THE xxl.job.admin.addresses!");
        //去除空格
        xxlJobSpringExecutor.setAdminAddresses(addresses.trim());
        String accessToken = xxlJobAdminProperties.getAccessToken();
        Assert.notBlank(accessToken,"ERROR: XXL_JOB ACCESS_TOKEN IS EMPTY! PLEASE SETTING THE xxl.job.admin.accessToken OR xxl.job.admin.access-token!");
        xxlJobSpringExecutor.setAccessToken(accessToken);
        //执行器相关配置
        XxlJobExecutorConfigProperties xxlJobExecutorProperties = xxlJobConfigProperties.getExecutor();
        //注册的执行器名称 当没有手动配置时 则使用spring配置中的applicationName+环境类型来注册
        String appName = xxlJobExecutorProperties.getAppName();
        if (StrUtil.isBlank(appName)) {
            appName = environment.getProperty("spring.application.name", "");
            String profileType = environment.getProperty("spring.profiles.active", "");
            appName = StrUtil.isNotBlank(profileType) ? appName + "_" + profileType : appName;
        }
        Assert.notBlank(appName,"ERROR:XXL_JOB APP NAME IS EMPTY! PLEASE SETTING THE xxl.job.executor.appName OR xxl.job.executor.app-name!");
        xxlJobSpringExecutor.setAppname(appName);
        xxlJobExecutorProperties.setAppName(appName);
        xxlJobSpringExecutor.setAddress(xxlJobExecutorProperties.getAddress());
        log.info(">>>>>>>>>>>>> XXL_JOB EXECUTOR NAME IS [{}] <<<<<<<<<<<<", appName);
        String ip = xxlJobExecutorProperties.getIp();
        if (StrUtil.isBlank(ip)) {
            ip = interNetUtil.findFirstNonLoopbackHostInfo().getIpAddress();
        }
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(xxlJobExecutorProperties.getPort());
        String logPath = xxlJobExecutorProperties.getLogPath();
        if (StrUtil.isBlank(logPath)) {
            logPath = environment.getProperty("LOGGING_PATH", "logs")
                    .concat("/").concat(appName).concat("/jobs");
        }
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(xxlJobExecutorProperties.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }

    /**
     * 自动创建调度中心上的配置
     * @param xxlJobConfigProperties
     * @return
     */
    @Bean
    @ConditionalOnExpression("!'${xxl.job.admin.user-name:}'.isEmpty() && !'${xxl.job.admin.password:}'.isEmpty()")
    public XxlJobExecutorAutoSaveRunner xxlJobExecutorAutoSaveRunner(XxlJobConfigProperties xxlJobConfigProperties) {
        return new XxlJobExecutorAutoSaveRunner(xxlJobConfigProperties);
    }

    /**
     * 服务销毁时 关闭IP工具类
     * @param interNetUtil
     * @return
     */
    @Bean
    public InterNetCloseHandler interNetCloseHandler(InterNetUtil interNetUtil) {
        return new InterNetCloseHandler(interNetUtil);
    }

}
