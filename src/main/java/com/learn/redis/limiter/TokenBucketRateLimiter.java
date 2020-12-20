package com.learn.redis.limiter;

import javax.servlet.annotation.WebFilter;

/**
 * 如何将限流器加入过滤器
 * 在WebFilterHandler中会将TokenBucketRateLimiter的beanDefinition转换成FilterRegistrationBean,
 * 最精髓的是builder.addPropertyValue("filter", beanDefinition),后续会将beanDefinition实例化并填充到FilterRegistrationBean.filter
 * 然后tomcat进行启动时,在ServletContextInitializerBeans中添加到过滤链
 * */
@WebFilter(filterName = "tokenBucketRateLimiter", urlPatterns = "/*")
public class TokenBucketRateLimiter extends RateLimiter {

    public TokenBucketRateLimiter(){
        super();
        super.key = "token_bucket_rate_limiter.lua";
    }

}
