# Nickle-Scheduler
## 技术架构：  
  SpringBoot,AKKA
## NickleScheduler是什么？    
  目前市面上可用的框架有以下几种：  
  1、quartz集群  
  2、elastic job   
  3、xxl-job   
  quartz集群需要面向quartz接口编写，十分不利于维护，不能实时监控任务的状态。    
  elastic job早已停止维护，再加上采用zk容易造成脑裂不推荐。    
  xxl-job社区活跃，但是代码还需要进行改造，如权限认证，线程池修改等，如果业务适配需要去仔细看源码才能找到解决方案。    
  以上两种流行分布式调度器都采用了quartz集群来做，而nicklescheduler完全原生实现，采用akka actor模型完全异步处理调度任务和任务执行，可对各个actor原生扩展增大并发量。  
## 目前尚未完成：  
  1、分片处理  
  2、浏览器页面处理  
  3、与spring深度整合  
## 对使用者说明：  
  1、目前NickleScheduler仅仅支持简单任务调度和任务容错，后续会增加web端对任务进行统计  
  2、可能存在些许bug和不合理之处，还希望各位多多指出，提出PR  
  3、真诚邀请志同道合的朋友加入贡献者一起完善整个调度器功能，相信在开源的力量下，本框架会变得越来越好，akka本身就是高性能全异步处理框架，十分适合用来做分布式调度，希望各位踊跃参与开发！！！  
## 作者联系方式：  
微信：18510746130  
QQ交流群：865810144
## NickleScheduler架构图：  
![image](https://github.com/NickleHuang/Nickle-Scheduler/blob/master/doc/NickleScheduler架构图.jpg)  
## NickleScheduler流程图：  
![image](https://github.com/NickleHuang/Nickle-Scheduler/blob/master/doc/nickle-scheduler流程图.jpg)
## NickleScheduler使用方式：
客户端面向SchedulerJob接口编写即可，其他流程参考springboot
## NickleScheduler启动方式：  
1、修改合适的端口和数据库配置并且将sql文件夹下的sql在数据库中执行    
2、client端和server端不区分先后顺序，client端会不断重试直到连接到master   
3、test用例有两个模式的执行任务，直接运行client、server端的Application类即可   
