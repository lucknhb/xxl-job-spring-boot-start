package com.nhb.autoconfigure.properties;

import lombok.Data;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/9 16:21
 * @description: 执行器配置
 */
@Data
public class XxlJobExecutorConfigProperties {
    /**
     * 执行器名称
     */
    private String appName;
    /**
     * 执行器标题(中文名称)
     */
    private String appTitle;
    /**
     * 执行器注册 [选填]：优先使用该配置作为注册地址，为空时使用内嵌服务 ”IP:PORT“ 作为注册地址
     */
    private String address;
    /**
     * 执行器IP [选填]：默认为空表示自动获取IP，多网卡时可手动设置指定IP，该IP不会绑定Host仅作为通讯使用
     */
    private String ip;
    /**
     * 执行器端口号 [选填]：小于等于0则自动获取；默认端口为9999<BR/>
     * 单机部署多个执行器时，注意要配置不同执行器端口
     */
    private Integer port = 9999;
    /**
     * 执行器运行日志文件存储磁盘路径 [选填] ：需要对该路径拥有读写权限；为空则使用默认路径；
     */
    private String logPath = "/logs/xxl-job/job";
    /**
     * 执行器日志文件保存天数 [选填] ： 过期日志自动清理, 限制值大于等于3时生效; 否则, 如-1, 关闭自动清理功能
     */
    private Integer logRetentionDays = 30;
}
