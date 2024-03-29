package com.xiaho.ratelimiter.limiter.slidinglog;

import com.xiaho.ratelimiter.limiter.RateLimiter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.TreeMap;

/**
 * description: 滑动日志算法
 *
 * @author wanghaoxin
 * date     2023/5/30 14:28
 * @version 1.0
 */
public class SlidingLogLimiter implements RateLimiter {
    /**
     * 每秒钟限制请求数
     */
    private static final long PERMITS_PER_MINUTE = 10;
    /**
     * 请求日志计数器, k-为请求的时间（秒），value当前时间的请求数量
     */
    private final TreeMap<Long, Integer> requestLogCountMap = new TreeMap<>();

    @Override
    public synchronized boolean acquire() {
        // 最小时间粒度为s
        long currentTimestamp = Instant.now().toEpochMilli();
        // 获取当前窗口的请求总数
        int currentWindowCount = getCurrentWindowCount(currentTimestamp);
        if (currentWindowCount >= PERMITS_PER_MINUTE) {
            return false;
        }
        // 请求成功，将当前请求日志加入到日志中
        requestLogCountMap.merge(currentTimestamp, 1, Integer::sum);
        return true;
    }

    /**
     * 统计当前时间窗口内的请求数
     *
     * @param currentTime 当前时间
     * @return -
     */
    private int getCurrentWindowCount(long currentTime) {
        // 计算出窗口的开始位置时间
        long startTime = currentTime - 1000;
        // 遍历当前存储的计数器，删除无效的子窗口计数器，并累加当前窗口中的所有计数器之和
        return requestLogCountMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey() >= startTime)
                .mapToInt(Map.Entry::getValue)
                .sum();
    }
}
