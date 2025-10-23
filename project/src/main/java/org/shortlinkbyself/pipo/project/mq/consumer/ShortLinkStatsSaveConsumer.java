package org.shortlinkbyself.pipo.project.mq.consumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.protobuf.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.shortlinkbyself.pipo.project.dao.entity.*;
import org.shortlinkbyself.pipo.project.dao.mapper.*;
import org.shortlinkbyself.pipo.project.dto.biz.ShortLinkStatsRecordDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.shortlinkbyself.pipo.project.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_TOPIC_KEY;
import static org.shortlinkbyself.pipo.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShortLinkStatsSaveConsumer {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final ShortLinkMapper shortLinkMapper;
    private final RedissonClient redissonClient;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    /**
     * 延迟消费短链接
     */
    public void get() {

        //stringRedisTemplate.opsForStream().createGroup(SHORT_LINK_STATS_STREAM_TOPIC_KEY, "stats-consumer-group");
        while (true) {
            List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream().read(
                    Consumer.from("stats-consumer-group", "stats-worker-1"),
                    StreamReadOptions.empty().count(1).block(Duration.ofSeconds(1)),
                    StreamOffset.create(SHORT_LINK_STATS_STREAM_TOPIC_KEY, ReadOffset.lastConsumed()));

            if (records == null || records.isEmpty()) {
                continue;
            }
            for (MapRecord<String, Object, Object> record : records) {
                String messageId = record.getId().getValue();

                Map<String, String> messageBody = new HashMap<>();
                record.getValue().forEach((field, value) -> {
                    messageBody.put(field.toString(), value.toString());
                });

                // 然后正常处理
                String json = messageBody.get("statsRecord");
                ShortLinkStatsRecordDTO dto = JSON.parseObject(json, ShortLinkStatsRecordDTO.class);
                actualSaveShortLinkStats(dto);
                //System.out.println(dto);

                //INSERT IN DB
                // if success
                stringRedisTemplate.opsForStream().acknowledge("stats-consumer-group", record);
            }
        }
    }

    @SneakyThrows
    public void actualSaveShortLinkStats(ShortLinkStatsRecordDTO shortLinkStatsRecordDTO) {
        if (BeanUtil.isEmpty(shortLinkStatsRecordDTO)) {
            throw new ServiceException("stats data null");
        }
        String fullShortUrl = shortLinkStatsRecordDTO.getFullShortUrl();
        LambdaQueryWrapper<ShortLinkGotoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
        ShortLinkGotoDO shortLinkGotoDo = shortLinkGotoMapper.selectOne(lambdaQueryWrapper);
        Date currentDate = shortLinkStatsRecordDTO.getCurrentDate();
        int hour = DateUtil.hour(currentDate, true);
        Week week = DateUtil.dayOfWeekEnum(currentDate);
        int weekValue = week.getIso8601Value();
        String gid = shortLinkGotoDo.getGid();
        LinkAccessStatsDO linkAccessStatsDO = new LinkAccessStatsDO().builder()
                .pv(1)
                .uv(shortLinkStatsRecordDTO.getUvFirstFlag() ? 1 : 0)
                .uip(shortLinkStatsRecordDTO.getUipFirstFlag() ? 1 : 0)
                .hour(hour)
                .weekday(weekValue)
                .fullShortUrl(fullShortUrl)
                .date(currentDate)
                .build();
        linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
        Map<String, Object> localeParamMap = new HashMap<>();
        localeParamMap.put("key", statsLocaleAmapKey);
        localeParamMap.put("ip", shortLinkStatsRecordDTO.getRemoteAddr());
        String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
        JSONObject localeResultObj = JSON.parseObject(localeResultStr);
        String infoCode = localeResultObj.getString("infocode");
        String actualProvince = "未知";
        String actualCity = "未知";
        if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
            String province = localeResultObj.getString("province");
            boolean unknownFlag = StrUtil.equals(province, "[]");
            LinkLocaleStatsDO linkLocaleStatsDO =
                    LinkLocaleStatsDO.builder()
                    .province(actualProvince = unknownFlag ? actualProvince : province)
                    .city(actualCity = unknownFlag ? actualCity : localeResultObj.getString("city"))
                    .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode"))
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .country("中国")
                    .date(currentDate)
                    .build();
            linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDO);
        }
        LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                .os(shortLinkStatsRecordDTO.getOs())
                .cnt(1)
                .fullShortUrl(fullShortUrl)
                .date(currentDate)
                .build();
        linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);
        LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                .browser(shortLinkStatsRecordDTO.getBrowser())
                .cnt(1)
                .fullShortUrl(fullShortUrl)
                .date(currentDate)
                .build();
        linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);
        LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                .device(shortLinkStatsRecordDTO.getDevice())
                .cnt(1)
                .fullShortUrl(fullShortUrl)
                .date(currentDate)
                .build();
        linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
        LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                .network(shortLinkStatsRecordDTO.getNetwork())
                .cnt(1)
                .fullShortUrl(fullShortUrl)
                .date(currentDate)
                .build();
        linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
        LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                .user(shortLinkStatsRecordDTO.getUv())
                .ip(shortLinkStatsRecordDTO.getRemoteAddr())
                .browser(shortLinkStatsRecordDTO.getBrowser())
                .os(shortLinkStatsRecordDTO.getOs())
                .network(shortLinkStatsRecordDTO.getNetwork())
                .device(shortLinkStatsRecordDTO.getDevice())
                .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                .fullShortUrl(fullShortUrl)
                .build();
        linkAccessLogsMapper.insert(linkAccessLogsDO);
        shortLinkMapper.incrementStats(gid, fullShortUrl, 1, shortLinkStatsRecordDTO.getUvFirstFlag() ? 1 : 0, shortLinkStatsRecordDTO.getUipFirstFlag() ? 1 : 0);
        LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                .todayPv(1)
                .todayUv(shortLinkStatsRecordDTO.getUvFirstFlag() ? 1 : 0)
                .todayUip(shortLinkStatsRecordDTO.getUipFirstFlag() ? 1 : 0)
                .fullShortUrl(fullShortUrl)
                .date(currentDate)
                .build();
        linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
    }
}