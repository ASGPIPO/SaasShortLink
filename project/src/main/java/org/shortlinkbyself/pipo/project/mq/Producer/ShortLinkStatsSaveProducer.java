package org.shortlinkbyself.pipo.project.mq.Producer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.shortlinkbyself.pipo.project.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_TOPIC_KEY;

@Component
@RequiredArgsConstructor
public class ShortLinkStatsSaveProducer {
    private static final int MAX_TRIM_LENGTH = 1000;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 发送延迟消费短链接统计
     */
    public void send(Map<String, String> producerMap) {

        stringRedisTemplate.opsForStream().add(SHORT_LINK_STATS_STREAM_TOPIC_KEY, producerMap);

    }
}