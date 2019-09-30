package com.learn.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 测试限流功能
 * */
@Component
public class Redis4Throttle {

    @Autowired
    private RedisTemplate redisTemplate;

    private static String KEY = "mao";

    private static int PERIOD = 60;

    private static int MAX = 5;

    private boolean isAllow;

    public boolean isActionAllowed(){
        RedisCallback redisCallback = new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                long nowTs = System.currentTimeMillis();
                redisConnection.openPipeline();
                redisConnection.zAdd(KEY.getBytes(),nowTs,(""+nowTs).getBytes());
                redisConnection.zRemRangeByScore(KEY.getBytes(),0,nowTs-1000*PERIOD);
                redisConnection.closePipeline();
                Long zCard = redisConnection.zCard(KEY.getBytes());
                isAllow = (zCard <= MAX)?true:false;
                return null;
            }
        };
        redisTemplate.executePipelined(redisCallback);
        return isAllow;
    }
}
