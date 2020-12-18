package com.learn.redis.limiter;

import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * 如何将限流器加入过滤器
 * 在WebFilterHandler中会将TokenBucketRateLimiter的beanDefinition转换成FilterRegistrationBean,
 * 然后tomcat进行启动时,在ServletContextInitializerBeans中添加到过滤链
 * */
@WebFilter(filterName = "tokenBucketRateLimiter", urlPatterns = "/*")
public class TokenBucketRateLimiter extends RateLimiter {

    public TokenBucketRateLimiter() throws IOException {
        super();
        super.key = "slide_window_rate_limiter.lua";
    }

}
