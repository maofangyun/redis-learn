package com.learn.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 测试HLL算法
 * */
@Component
public class Redis4HLL {

    @Autowired
    private RedisTemplate redisTemplate;

    public void add4HLL(){
        HyperLogLogOperations hyperLogLog = redisTemplate.opsForHyperLogLog();
        for (int i = 0; i < 100000; i++) {
            hyperLogLog.add("test","userID"+i%500);
        }
    }

    public long get4HLL(){
        HyperLogLogOperations hyperLogLog = redisTemplate.opsForHyperLogLog();
        Long count = hyperLogLog.size("test");
        return count;
    }
}
