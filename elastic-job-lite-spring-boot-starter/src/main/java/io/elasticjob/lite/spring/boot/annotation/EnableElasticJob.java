/**
 * Copyright (C), 2015-2020
 * FileName: EnableElasticJob
 * Author:   linzx
 * Date:     2020/1/9 10:12 上午
 * History:
 */
package io.elasticjob.lite.spring.boot.annotation;

import io.elasticjob.lite.spring.boot.register.ElasticJobImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * elasticjob开启
 *
 * @author linzx
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ElasticJobImportBeanDefinitionRegistrar.class})
public @interface EnableElasticJob {
    @AliasFor("jobBasePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] jobBasePackages() default {};
}