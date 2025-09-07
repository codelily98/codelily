package com.codelily.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(); // host/port는 application.yml 사용
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory f) {
        return new StringRedisTemplate(f);
    }
}
