package com.learn.redis.limiter;

import java.util.HashMap;
import java.util.Map;

/**
 * 漏桶算法的原理,和令牌桶算法的区别:
 * 后者可以处理突发的流量,前者由于漏出请求的速率一定,无法承担突发流量?
 * */
public class FunnelRateLimiter {

    private final Map<String, Funnel> funnels = new HashMap<>();

    public boolean isActionAllowed(String userId, String actionKey, int capacity, float leakingRate) {
        String key = String.format("%s:%s", userId, actionKey);
        Funnel funnel = funnels.get(key);
        if (funnel == null) {
            funnel = new Funnel(capacity, leakingRate);
            funnels.put(key, funnel);
        }
        // 请求获取1个quota
        return funnel.watering(1);
    }


    static class Funnel {
        int capacity;       // 漏斗容量
        float leakingRate;  // 漏嘴流水速率
        int leftQuota;      // 漏斗剩余空间
        long leakingTs;     // 上一次漏水时间

        public Funnel(int capacity, float leakingRate) {
            this.capacity = capacity;
            this.leakingRate = leakingRate;
            this.leftQuota = capacity;
            this.leakingTs = System.currentTimeMillis();
        }

        void makeSpace() {
            long nowTs = System.currentTimeMillis();
            // 距离上一次漏水过去了多久
            long deltaTs = nowTs - leakingTs;
            // 记录腾出来了多少空间
            int deltaQuota = (int) (deltaTs * leakingRate);
            // 间隔时间太长,整数数字过大溢出
            if (deltaQuota < 0) {
                this.leftQuota = capacity;
                this.leakingTs = nowTs;
                return;
            }
            // 腾的空间太少,最小单位是1,那就等下次吧
            if (deltaQuota < 1) {
                return;
            }
            // 增加剩余空间
            this.leftQuota += deltaQuota;
            // 记录漏水时间
            this.leakingTs = nowTs;
            // 剩余空间不得高于容量
            if (this.leftQuota > this.capacity) {
                this.leftQuota = this.capacity;
            }
        }

        boolean watering(int quota) {
            makeSpace();
            // 判断剩余空间是否足够
            if (this.leftQuota >= quota) {
                this.leftQuota -= quota;
                return true;
            }
            return false;
        }
    }

    //public static boolean tryAcquire(String key, int max,int rate) {
    //    List<String> keyList = new ArrayList<>(1);
    //    keyList.add(key);
    //    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    //    script.setResultType(Long.class);
    //    script.setScriptText(TEXT);
    //    return Long.valueOf(1).equals(stringRedisTemplate.execute(script,keyList,Integer.toString(max), Integer.toString(rate),
    //            Long.toString(System.currentTimeMillis())));
    //}

}
