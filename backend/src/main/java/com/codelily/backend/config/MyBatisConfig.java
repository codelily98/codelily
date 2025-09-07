package com.codelily.backend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.codelily.backend.mapper")
public class MyBatisConfig {
    // 필요시 TypeHandler, Interceptor 추가 가능
}
