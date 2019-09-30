package com.learn.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RedisLearnApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(RedisLearnApplication.class, args);
//        Redis4HLL redis4HLL = applicationContext.getBean(Redis4HLL.class);
//        redis4HLL.add4HLL();
//        System.out.println(redis4HLL.get4HLL());
        Redis4Throttle redis4Throttle = applicationContext.getBean(Redis4Throttle.class);
        for (int i = 0; i < 20; i++) {
            boolean actionAllowed = redis4Throttle.isActionAllowed();
            System.out.println(actionAllowed);
        }
    }

}
