package com.gamecard.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 *
 * @author GameCard Team
 */
@Configuration
@MapperScan("com.gamecard.mapper")
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器
     * <p>
     * 分页插件通过 application.yml 中的 mybatis-plus.page 配置项自动启用，
     * 无需在此处手动注册 PaginationInnerInterceptor。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        return new MybatisPlusInterceptor();
    }

}
