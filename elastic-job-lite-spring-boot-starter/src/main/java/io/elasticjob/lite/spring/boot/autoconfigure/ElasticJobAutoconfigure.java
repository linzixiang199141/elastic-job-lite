/**
 * Copyright (C), 2015-2020
 * FileName: ElasticJobAutoconfigure
 * Author:   linzx
 * Date:     2020/1/9 10:32 上午
 * History:
 */
package io.elasticjob.lite.spring.boot.autoconfigure;

import io.elasticjob.lite.spring.boot.config.ElasticJobProperties;
import io.elasticjob.lite.spring.boot.parse.ElasticJobParser;
import io.elasticjob.lite.reg.zookeeper.ZookeeperConfiguration;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * job annotation autoconfigure
 *
 * @author linzx
 */
@EnableConfigurationProperties(ElasticJobProperties.class)
@Slf4j
public class ElasticJobAutoconfigure {

    @Autowired
    ElasticJobProperties elasticJobProperties;

    /**
     * init Zookeeper register
     *
     * @return
     */
    @Bean(name = "zookeeperRegistryCenter", value = "zookeeperRegistryCenter", initMethod = "init")
    @ConditionalOnMissingBean
    public ZookeeperRegistryCenter zookeeperRegistryCenter() {

        ElasticJobProperties.ZooKeeperProperties zooKeeperProperties = elasticJobProperties.getZooKeeper();
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(zooKeeperProperties.getServerLists(),
                zooKeeperProperties.getNamespace());
        zookeeperConfiguration.setBaseSleepTimeMilliseconds(zooKeeperProperties.getBaseSleepTimeMilliseconds());
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(zooKeeperProperties.getConnectionTimeoutMilliseconds());
        zookeeperConfiguration.setDigest(zooKeeperProperties.getDigest());
        zookeeperConfiguration.setMaxRetries(zooKeeperProperties.getMaxRetries());
        zookeeperConfiguration.setMaxSleepTimeMilliseconds(zooKeeperProperties.getMaxSleepTimeMilliseconds());
        zookeeperConfiguration.setSessionTimeoutMilliseconds(zooKeeperProperties.getSessionTimeoutMilliseconds());
        log.debug("elastic job：init Zookeeper,{}", zooKeeperProperties);
        return new ZookeeperRegistryCenter(zookeeperConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean
    public ElasticJobParser elasticJobSpringBootParser() {
        return new ElasticJobParser();
    }
}