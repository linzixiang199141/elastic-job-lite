/**
 * Copyright (C), 2015-2020
 * FileName: ClassPathJobScanner
 * Author:   linzx
 * Date:     2020/1/9 10:37 上午
 * History:
 */
package io.elastic.lite.job.spring.boot.register;

import io.elastic.lite.job.spring.boot.annotation.ElasticJob;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * scanner
 *
 * @Author linzx
 * @Date 2020/1/9
 * @Since 1.0.0
 */
public class ClassPathJobScanner extends ClassPathBeanDefinitionScanner {

    public ClassPathJobScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }


    /**
     * register filters
     */
    protected void registerFilters() {
        this.addIncludeFilter(new AnnotationTypeFilter(ElasticJob.class));
    }


    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        return super.doScan(basePackages);
    }
}