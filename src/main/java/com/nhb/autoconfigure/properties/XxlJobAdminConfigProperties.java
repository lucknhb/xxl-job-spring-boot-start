package com.nhb.autoconfigure.properties;

import lombok.Data;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/9 16:15
 * @description: admin相关配置
 */
@Data
public class XxlJobAdminConfigProperties {
    /**
     * 调度中心地址 例如：<a href="http://127.0.0.1:8080/xxl-job-admin"/>http://127.0.0.1:8080/xxl-job-admin</a>
     */
    private String addresses;
    /**
     * 调度中心登录路径
     */
    private String loginUri = "/login";
    /**
     * 执行器保存路径
     */
    private String saveUri;
    /**
     * 查询执行器路径
     */
    private String groupUri = "/jobgroup/pageList";
    /**
     * 调度中心通讯 token
     */
    private String accessToken;
    /**
     * 调度中心通讯超时时间 <BR/>
     * 单位秒；默认3s；
     */
    private Integer timeout = 3;
    /**
     * 登录调度中心的账号
     */
    private String userName;
    /**
     * 登录调度中心的密码
     */
    private String password;
}
