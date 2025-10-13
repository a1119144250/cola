package com.xiaowang.cola.tools.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置
 *
 * @author cola
 */
@Configuration
public class RedisConfig {

  /**
   * 配置RedisTemplate
   * 使用String序列化器，保证存储的数据是可读的字符串
   */
  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // 使用StringRedisSerializer来序列化和反序列化redis的key和value
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    // key采用String的序列化方式
    template.setKeySerializer(stringRedisSerializer);
    // value采用String的序列化方式
    template.setValueSerializer(stringRedisSerializer);
    // hash的key也采用String的序列化方式
    template.setHashKeySerializer(stringRedisSerializer);
    // hash的value也采用String的序列化方式
    template.setHashValueSerializer(stringRedisSerializer);

    template.afterPropertiesSet();
    return template;
  }
}
