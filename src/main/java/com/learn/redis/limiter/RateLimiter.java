package com.learn.redis.limiter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class RateLimiter implements Filter {

    @Autowired
    private RedisTemplate redisTemplate;

    private AtomicInteger atomicInteger = new AtomicInteger();

    private final static String FILE_PATH = "classpath:lua/*.lua";

    protected String key;

    private HashMap<String,DefaultRedisScript> scriptMap = new HashMap<>();

    /**
     * 脚本预热
     * 由于过滤器属于tomcat容器进行管理的bean,所以@PostConstruct注解的调用,在tomcat启动时,也会调用一遍(spring会在实例化过滤器之后,初始化之前调用一遍),但是之前会先做验证
     * method.getParameterTypes().length != 0
     * || Modifier.isStatic(method.getModifiers())
     * || method.getExceptionTypes().length > 0
     * || !method.getReturnType().getName().equals("void")
     * 其中第三项,要求不能抛出异常,否则会导致tomcat启动失败
     *
     * 默认的ScriptExecutor通过检索脚本的SHA1并尝试首先运行evalsha来优化性能，如果脚本还没有出现在Redis脚本缓存中，则会回退到eval
     * */
    @PostConstruct
    private void warmUp(){
        // 读取lua脚本传递到Redis
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = new Resource[0];
        try {
            resources = resolver.getResources(FILE_PATH);
            for (Resource resource : resources){
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                // 指定ReturnType为Long.class，注意这里不能使用Integer.class，因为ReturnType不支持。只支持List.class, Boolean.class和Long.class
                script.setResultType(Long.class);
                script.setLocation(resource);
                String filename = resource.getFilename();
                scriptMap.put(filename,script);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean tryAcquire(String url){
        DefaultRedisScript script = scriptMap.get(key);
        List<String> keys = new ArrayList<>();
        keys.add(url);
        keys.add(String.valueOf(System.currentTimeMillis() / 1000));
        Long result = (Long)redisTemplate.execute(script, keys);
        return result==1;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String url = request.getRequestURL().toString();
        if(tryAcquire(url)){
            //放行
            System.out.println("第"+atomicInteger.incrementAndGet()+"次请求: 通过");
            filterChain.doFilter(servletRequest, servletResponse);
        } else{
            System.out.println("第"+atomicInteger.incrementAndGet()+"次请求: 拒绝");
            return;
        }
    }
}
