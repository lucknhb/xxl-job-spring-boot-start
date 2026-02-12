package com.nhb.autoconfigure.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/11 16:52
 * @description:
 */
@Slf4j
public class XxlJobUtil {

    /**
     * 获取XXL_JOB版本号
     * @return
     */
    public static String getVersion() {
        try {
            Properties props = new Properties();
            InputStream is = XxlJobUtil.class.getClassLoader()
                    .getResourceAsStream("META-INF/maven/com.xuxueli/xxl-job-core/pom.properties");
            if (is != null) {
                props.load(is);
                return props.getProperty("version", "Unknown");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "Unknown";
    }
}
