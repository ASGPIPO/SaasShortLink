package org.shortlinkbyself.pipo.project.common.config;


import lombok.RequiredArgsConstructor;
import org.shortlinkbyself.pipo.project.mq.consumer.ShortLinkStatsSaveConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.Subscription;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

import static org.shortlinkbyself.pipo.project.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_GROUP_KEY;
import static org.shortlinkbyself.pipo.project.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_TOPIC_KEY;

@Configuration
@RequiredArgsConstructor
public class RedisStreamConfiguration {
    private final RedisConnectionFactory redisConnectionFactory;
    private final ShortLinkStatsSaveConsumer shortLinkStatsSaveConsumer;

    @Bean
    public Subscription shortLinkStatsSaveConsumerSubscription() {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .batchSize(10)
                        .pollTimeout(Duration.ofSeconds(5))
                        .build();
        StreamMessageListenerContainer.StreamReadRequest<String> streamReadRequest =
                StreamMessageListenerContainer.StreamReadRequest
                        .builder(StreamOffset.create(SHORT_LINK_STATS_STREAM_TOPIC_KEY, ReadOffset.lastConsumed()))
                        .cancelOnError(throwable -> false)
                        .consumer(Consumer.from(SHORT_LINK_STATS_STREAM_GROUP_KEY, "stats-consumer"))
                        .build();
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, options);
        org.springframework.data.redis.stream.Subscription subscription = listenerContainer.register(streamReadRequest, (StreamListener<String, MapRecord<String, String, String>>) shortLinkStatsSaveConsumer);
    }
}
