支持XXL_JOB 2.X/3.X
此start引用了以下包 当需要自指定版本是请剔除以下包
```xml

<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.8.43</version>
</dependency>

<dependency>
    <groupId>com.xuxueli</groupId>
    <artifactId>xxl-job-core</artifactId>
    <version>3.3.2</version>
</dependency>
```

使用该start时的配置项如下

```yaml
xxl:
  job:
    enable: true #是否自动注册执行器
    admin:
      address: #调度中心地址 例如: http://127.0.0.1:8080/xxl-job-admin
      access-token: #调度中心地址 请求token
      user-name: #当需要自动添加 调度中心->执行器管理中的执行器信息时(非注册执行器) 需要提供该配置项
      password: #当需要自动添加 调度中心->执行器管理中的执行器信息时(非注册执行器) 需要提供该配置项
      login-uri: #当需要自动添加 调度中心->执行器管理中的执行器信息时(非注册执行器) 需要提供该配置项 默认为 /login 具体可通过查看调度中心登录时的路径
      group-uri: #当需要自动添加 调度中心->执行器管理中的执行器信息时(非注册执行器) 需要提供该配置项 默认为 /jobgroup/pageList 具体可通过查看执行器分页查询路径
      save-uri: #当需要自动添加 调度中心->执行器管理中的执行器信息时(非注册执行器) 需要提供该配置项 具体可通过查看执行器保存路径
    executor:
      address: #执行器注册 为空时使用内嵌服务 "IP:PORT" 作为注册地址
      ip: #执行器IP 为空时 自动获取IP
      port: #执行器端口 默认9999
      log-path: #执行器日志保存路径 默认 /logs/xxl-job/job
      log-retention-days: #执行器日志文件保存天数 默认30
      app-name: #当需要自动添加 调度中心->执行器管理中的执行器信息时(非注册执行器) 需要提供该配置项 执行器appName 当为空时使用spring配置中的name+环境类型
      app-title: #当需要自动添加 调度中心->执行器管理中的执行器信息时(非注册执行器) 需要提供该配置项 执行器名称 为空时自截取app-name 12位
```
