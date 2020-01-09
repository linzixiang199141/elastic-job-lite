/**
 * Copyright (C), 2015-2020
 * FileName: ElasticJobImportBeanDefinitionRegistrar
 * Author:   linzx
 * Date:     2020/1/9 10:34 上午
 * History:
 */
package io.elastic.lite.job.spring.boot.register;

import io.elastic.lite.job.spring.boot.annotation.EnableElasticJob;
import io.elastic.lite.job.spring.boot.autoconfigure.ElasticJobAutoconfigure;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * register job bean
 *
 * @Author linzx
 * @Date 2020/1/9
 * @Since 1.0.0
 */
public class ElasticJobImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(ElasticJobAutoconfigure.class);
        registry.registerBeanDefinition("elasticJobAutoconfigure", beanDefinitionBuilder.getBeanDefinition());

        ClassPathJobScanner scanner = new ClassPathJobScanner(registry);
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableElasticJob.class.getName()));

        String[] packages = annoAttrs.getStringArray("value");
        if (packages == null) {
            packages = new String[1];
            packages[0] = ClassUtils.getPackageName(annotationMetadata.getClassName());
        }
        scanner.registerFilters();
        scanner.doScan(packages);
    }
}