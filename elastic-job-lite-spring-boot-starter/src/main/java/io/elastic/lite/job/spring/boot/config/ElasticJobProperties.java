/**
 * Copyright (C), 2015-2020
 * FileName: ElasticJobProperties
 * Author:   linzx
 * Date:     2020/1/9 10:25 上午
 * History:
 */
package io.elastic.lite.job.spring.boot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * zookeeper config properties
 *
 * @Author linzx
 * @Date 2020/1/9
 * @Since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "elastic-job")
public class ElasticJobProperties {
    private ZooKeeperProperties zooKeeper = new ZooKeeperProperties();


    @Data
    public static class ZooKeeperProperties {
        /**
         * 连接Zookeeper服务器的列表.
         * 包括IP地址和端口号.
         * 多个地址用逗号分隔.
         * 如: host1:2181,host2:2181
         */
        private String serverLists = "localhost:2181";

        /**
         * 命名空间.
         */
        private String namespace = "elastic_job_spring";

        /**
         * 等待重试的间隔时间的初始值.
         * 单位毫秒.
         */
        private int baseSleepTimeMilliseconds = 1000;

        /**
         * 等待重试的间隔时间的最大值.
         * 单位毫秒.
         */
        private int maxSleepTimeMilliseconds = 3000;

        /**
         * 最大重试次数.
         */
        private int maxRetries = 3;

        /**
         * 会话超时时间.
         * 单位毫秒.
         */
        private int sessionTimeoutMilliseconds = 10000;

        /**
         * 连接超时时间.
         * 单位毫秒.
         */
        private int connectionTimeoutMilliseconds = 10000;

        /**
         * 连接Zookeeper的权限令牌.
         * 缺省为不需要权限验证.
         */
        private String digest = "";

    }
}