package com.learn.redis.limiter;

import java.io.IOException;


/**
 * 滑动窗口限流功能
 * */
//@WebFilter(filterName = "slideWindowRateLimiter", urlPatterns = "/*")
public class SlideWindowRateLimiter extends RateLimiter {

    public SlideWindowRateLimiter(){
        super();
        super.key = "slide_window_rate_limiter.lua";
    }
}
