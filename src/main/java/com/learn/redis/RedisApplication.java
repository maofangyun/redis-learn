package com.learn.redis;

import com.learn.redis.limiter.SlideWindowRateLimiter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan("com.learn.redis.limiter")
@SpringBootApplication
public class RedisApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(RedisApplication.class, args);
//        Redis4HLL redis4HLL = applicationContext.getBean(Redis4HLL.class);
//        redis4HLL.add4HLL();
//        System.out.println(redis4HLL.get4HLL());
    }

}
