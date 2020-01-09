/**
 * Copyright (C), 2015-2020
 * FileName: ElasticJobParser
 * Author:   linzx
 * Date:     2020/1/9 10:28 上午
 * History:
 */
package io.elasticjob.lite.spring.boot.parse;

import io.elasticjob.lite.spring.boot.annotation.ElasticJob;
import io.elasticjob.lite.spring.boot.config.JobAttributeTag;
import io.elasticjob.lite.api.dataflow.DataflowJob;
import io.elasticjob.lite.api.script.ScriptJob;
import io.elasticjob.lite.api.simple.SimpleJob;
import io.elasticjob.lite.config.JobCoreConfiguration;
import io.elasticjob.lite.config.JobTypeConfiguration;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import io.elasticjob.lite.config.script.ScriptJobConfiguration;
import io.elasticjob.lite.config.simple.SimpleJobConfiguration;
import io.elasticjob.lite.event.rdb.JobEventRdbConfiguration;
import io.elasticjob.lite.executor.handler.JobProperties;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import io.elasticjob.lite.spring.api.SpringJobScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * parse annotaion job config
 *
 * @author linzx
 */
@Slf4j
public class ElasticJobParser implements ApplicationContextAware, EnvironmentAware {

    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    private String prefix = "elasticJob.";

    private Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> beanMap = ctx.getBeansWithAnnotation(ElasticJob.class);
        log.debug("EJOB：开始加载Job,找到Job：{}个", beanMap.size());
        for (Object confBean : beanMap.values()) {

            Class<?> clz = confBean.getClass();


            String jobTypeName = confBean.getClass().getInterfaces()[0].getSimpleName();
            ElasticJob elasticJob = clz.getAnnotation(ElasticJob.class);

            String jobClass = clz.getName();
            String jobName = elasticJob.name();

            log.debug("EJOB：加载Job,Name:{},Class:{}", jobName, jobClass);
            String cron = getEnvironmentStringValue(jobName, JobAttributeTag.CRON, elasticJob.cron());
            String shardingItemParameters = getEnvironmentStringValue(jobName, JobAttributeTag.SHARDING_ITEM_PARAMETERS, elasticJob.shardingItemParameters());
            String description = getEnvironmentStringValue(jobName, JobAttributeTag.DESCRIPTION, elasticJob.description());
            String jobParameter = getEnvironmentStringValue(jobName, JobAttributeTag.JOB_PARAMETER, elasticJob.jobParameter());
            String jobExceptionHandler = getEnvironmentStringValue(jobName, JobAttributeTag.JOB_EXCEPTION_HANDLER, elasticJob.jobExceptionHandler());
            String executorServiceHandler = getEnvironmentStringValue(jobName, JobAttributeTag.EXECUTOR_SERVICE_HANDLER, elasticJob.executorServiceHandler());

            String jobShardingStrategyClass = getEnvironmentStringValue(jobName, JobAttributeTag.JOB_SHARDING_STRATEGY_CLASS, elasticJob.jobShardingStrategyClass());
            String eventTraceRdbDataSource = getEnvironmentStringValue(jobName, JobAttributeTag.EVENT_TRACE_RDB_DATA_SOURCE, elasticJob.eventTraceRdbDataSource());
            String scriptCommandLine = getEnvironmentStringValue(jobName, JobAttributeTag.SCRIPT_COMMAND_LINE, elasticJob.scriptCommandLine());

            boolean failover = getEnvironmentBooleanValue(jobName, JobAttributeTag.FAILOVER, elasticJob.failover());
            boolean misfire = getEnvironmentBooleanValue(jobName, JobAttributeTag.MISFIRE, elasticJob.misfire());
            boolean overwrite = getEnvironmentBooleanValue(jobName, JobAttributeTag.OVERWRITE, elasticJob.overwrite());
            boolean disabled = getEnvironmentBooleanValue(jobName, JobAttributeTag.DISABLED, elasticJob.disabled());
            boolean monitorExecution = getEnvironmentBooleanValue(jobName, JobAttributeTag.MONITOR_EXECUTION, elasticJob.monitorExecution());
            boolean streamingProcess = getEnvironmentBooleanValue(jobName, JobAttributeTag.STREAMING_PROCESS, elasticJob.streamingProcess());

            int shardingTotalCount = getEnvironmentIntValue(jobName, JobAttributeTag.SHARDING_TOTAL_COUNT, elasticJob.shardingTotalCount());
            int monitorPort = getEnvironmentIntValue(jobName, JobAttributeTag.MONITOR_PORT, elasticJob.monitorPort());
            int maxTimeDiffSeconds = getEnvironmentIntValue(jobName, JobAttributeTag.MAX_TIME_DIFF_SECONDS, elasticJob.maxTimeDiffSeconds());
            int reconcileIntervalMinutes = getEnvironmentIntValue(jobName, JobAttributeTag.RECONCILE_INTERVAL_MINUTES, elasticJob.reconcileIntervalMinutes());

            // 核心配置
            JobCoreConfiguration coreConfig =
                    JobCoreConfiguration.newBuilder(jobName, cron, shardingTotalCount)
                            .shardingItemParameters(shardingItemParameters)
                            .description(description)
                            .failover(failover)
                            .jobParameter(jobParameter)
                            .misfire(misfire)
                            .jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobExceptionHandler)
                            .jobProperties(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), executorServiceHandler)
                            .build();

            // 不同类型的任务配置处理
            JobTypeConfiguration typeConfig = null;
            if (SimpleJob.class.getSimpleName().equals(jobTypeName)) {
                typeConfig = new SimpleJobConfiguration(coreConfig, jobClass);
            }

            if (DataflowJob.class.getSimpleName().equals(jobTypeName)) {
                typeConfig = new DataflowJobConfiguration(coreConfig, jobClass, streamingProcess);
            }

            if (ScriptJob.class.getSimpleName().equals(jobTypeName)) {
                typeConfig = new ScriptJobConfiguration(coreConfig, scriptCommandLine);
            }

            LiteJobConfiguration jobConfig = LiteJobConfiguration.newBuilder(typeConfig)
                    .overwrite(overwrite)
                    .disabled(disabled)
                    .monitorPort(monitorPort)
                    .monitorExecution(monitorExecution)
                    .maxTimeDiffSeconds(maxTimeDiffSeconds)
                    .jobShardingStrategyClass(jobShardingStrategyClass)
                    .reconcileIntervalMinutes(reconcileIntervalMinutes)
                    .build();

            List<BeanDefinition> elasticJobListeners = getTargetElasticJobListeners(elasticJob);

            // 构建SpringJobScheduler对象来初始化任务
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringJobScheduler.class);
            factory.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            if (!ScriptJob.class.getSimpleName().equals(jobTypeName)) {
                factory.addConstructorArgValue(confBean);
            }
            factory.addConstructorArgValue(zookeeperRegistryCenter);
            factory.addConstructorArgValue(jobConfig);

            // 任务执行日志数据源，以名称获取
            if (StringUtils.hasText(eventTraceRdbDataSource)) {
                BeanDefinitionBuilder rdbFactory = BeanDefinitionBuilder.rootBeanDefinition(JobEventRdbConfiguration.class);
                rdbFactory.addConstructorArgReference(eventTraceRdbDataSource);
                factory.addConstructorArgValue(rdbFactory.getBeanDefinition());
            }

            factory.addConstructorArgValue(elasticJobListeners);
            DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) ctx.getAutowireCapableBeanFactory();
            defaultListableBeanFactory.registerBeanDefinition("SpringJobScheduler", factory.getBeanDefinition());
            SpringJobScheduler springJobScheduler = (SpringJobScheduler) ctx.getBean("SpringJobScheduler");
            springJobScheduler.init();
            log.debug("EJOB：Job加载成功,Name:{},Class:{}", jobName, jobClass);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    private List<BeanDefinition> getTargetElasticJobListeners(ElasticJob elasticJob) {
        List<BeanDefinition> result = new ManagedList<>(2);
        String listeners = getEnvironmentStringValue(elasticJob.name(), JobAttributeTag.LISTENER, elasticJob.listener());
        if (StringUtils.hasText(listeners)) {
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(listeners);
            factory.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            result.add(factory.getBeanDefinition());
        }

        String distributedListeners = getEnvironmentStringValue(elasticJob.name(), JobAttributeTag.DISTRIBUTED_LISTENER, elasticJob.distributedListener());
        long startedTimeoutMilliseconds = getEnvironmentLongValue(elasticJob.name(), JobAttributeTag.DISTRIBUTED_LISTENER_STARTED_TIMEOUT_MILLISECONDS, elasticJob.startedTimeoutMilliseconds());
        long completedTimeoutMilliseconds = getEnvironmentLongValue(elasticJob.name(), JobAttributeTag.DISTRIBUTED_LISTENER_COMPLETED_TIMEOUT_MILLISECONDS, elasticJob.completedTimeoutMilliseconds());

        if (StringUtils.hasText(distributedListeners)) {
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(distributedListeners);
            factory.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            factory.addConstructorArgValue(startedTimeoutMilliseconds);
            factory.addConstructorArgValue(completedTimeoutMilliseconds);
            result.add(factory.getBeanDefinition());
        }
        return result;
    }

    /**
     * 获取配置中的任务属性值，environment没有就用注解中的值
     *
     * @param jobName      任务名称
     * @param fieldName    属性名称
     * @param defaultValue 默认值
     * @return
     */
    private String getEnvironmentStringValue(String jobName, String fieldName, String defaultValue) {
        String key = prefix + jobName + "." + fieldName;
        String value = environment.getProperty(key);
        if (StringUtils.hasText(value)) {
            return value;
        }
        return defaultValue;
    }

    private int getEnvironmentIntValue(String jobName, String fieldName, int defaultValue) {
        String key = prefix + jobName + "." + fieldName;
        String value = environment.getProperty(key);
        if (StringUtils.hasText(value)) {
            return Integer.valueOf(value);
        }
        return defaultValue;
    }

    private long getEnvironmentLongValue(String jobName, String fieldName, long defaultValue) {
        String key = prefix + jobName + "." + fieldName;
        String value = environment.getProperty(key);
        if (StringUtils.hasText(value)) {
            return Long.valueOf(value);
        }
        return defaultValue;
    }

    private boolean getEnvironmentBooleanValue(String jobName, String fieldName, boolean defaultValue) {
        String key = prefix + jobName + "." + fieldName;
        String value = environment.getProperty(key);
        if (StringUtils.hasText(value)) {
            return Boolean.valueOf(value);
        }
        return defaultValue;
    }
}