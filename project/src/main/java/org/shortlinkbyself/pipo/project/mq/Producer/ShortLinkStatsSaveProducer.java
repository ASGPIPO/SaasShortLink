package org.shortlinkbyself.pipo.project.mq.Producer;


import cn.hutool.core.lang.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShortLinkStatsSaveProducer {
    private static final int MAX_TRIM_LENGTH = 1000;
    private final RocketMQTemplate rocketMQTemplate;
    @Value("${rocketmq.producer.topic}")
    private String statsSaveTopic;
    /**
     * 发送延迟消费短链接统计
     */
    public void send(Map<String, String> producerMap) {

        String keys = UUID.fastUUID().toString();
        producerMap.put("keys", keys);
        Message<Map<String, String>> build = MessageBuilder
                .withPayload(producerMap)
                .setHeader(MessageConst.PROPERTY_KEYS, keys)
                .build();

        rocketMQTemplate.asyncSend(statsSaveTopic, build, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                // 发送成功，记录日志即可
                log.debug("统计消息发送成功: {}", keys);
            }

            @Override
            public void onException(Throwable e) {
                // 发送失败，记录错误日志（统计可容忍少量丢失）
                log.error("统计消息发送失败: {}, 错误: {}", keys, e.getMessage());
            }
        });

    }
}