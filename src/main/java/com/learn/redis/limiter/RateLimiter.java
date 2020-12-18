package com.learn.redis.limiter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class RateLimiter implements Filter {

    @Autowired
    private RedisTemplate redisTemplate;

    private final static String FILE_PATH = "classpath:lua/*.lua";

    protected String key;

    private HashMap<String,DefaultRedisScript> scriptMap = new HashMap<>();

    public RateLimiter() throws IOException {
        warmUp();
    }

    /**
     * 脚本预热(不能用@PostConstruct注解,因为顺序问题,过滤器在onRefresh()就会实例化,不会经过后面的单例bean的@PostConstruct收集解析过程)
     * 默认的ScriptExecutor通过检索脚本的SHA1并尝试首先运行evalsha来优化性能，如果脚本还没有出现在Redis脚本缓存中，则会回退到eval
     * */
    //@PostConstruct
    private void warmUp() throws IOException {
        // 读取lua脚本传递到Redis
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(FILE_PATH);
        for (Resource resource : resources){
            DefaultRedisScript<Integer> script = new DefaultRedisScript<>();
            script.setLocation(resource);
            String filename = resource.getFilename();
            scriptMap.put(filename,script);
        }
    }

    private boolean tryAcquire(String url){
        DefaultRedisScript script = scriptMap.get(key);
        List<String> keys = new ArrayList<>();
        keys.add(url);
        keys.add(String.valueOf(System.currentTimeMillis()));
        Integer result = (Integer)redisTemplate.execute(script, keys);
        return result==1;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String url = request.getRequestURL().toString();
        if(tryAcquire(url)){
            //放行
            filterChain.doFilter(servletRequest, servletResponse);
        } else{
            return;
        }
    }
}
