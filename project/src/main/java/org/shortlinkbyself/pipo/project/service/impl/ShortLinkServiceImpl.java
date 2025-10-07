package org.shortlinkbyself.pipo.project.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.protobuf.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.shortlinkbyself.pipo.project.dao.enity.ShortLinkDO;
import org.shortlinkbyself.pipo.project.dao.mapper.ShortLinkMapper;
import org.shortlinkbyself.pipo.project.dto.req.ShortLinkCreateReqDTO;
import org.shortlinkbyself.pipo.project.dto.resp.ShortLinkCreateRespDTO;
import org.shortlinkbyself.pipo.project.service.ShortLinkService;
import org.shortlinkbyself.pipo.project.tookit.HashUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {
    private final RBloomFilter<String> shortLinkCreateCachePenetrationBloomFilter;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) throws ServiceException {

        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .fullShortUrl(fullShortUrl)
                .enableStatus(0)
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
                .delTime(0L)
//                .favicon(getFavicon(requestParam.getOriginUrl()))
                .build();

        try {
            baseMapper.insert(shortLinkDO);

        } catch (DuplicateKeyException ex) {
            // 首先判断是否存在布隆过滤器，如果不存在直接新增
            if (!shortLinkCreateCachePenetrationBloomFilter.contains(fullShortUrl)) {
                shortLinkCreateCachePenetrationBloomFilter.add(fullShortUrl);
            }
            throw new ServiceException(String.format("短链接：%s 生成重复", fullShortUrl));
        }
        shortLinkCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestParam) throws ServiceException {
        String domain = requestParam.getDomain();
        String originUrl = requestParam.getOriginUrl();

        for (int i = 0; i < 10; i++) {
            String input = originUrl + UUID.randomUUID().toString();
            String shortUri = HashUtils.hashToBase62(input);
            if (!shortLinkCreateCachePenetrationBloomFilter.contains(domain + "/" + shortUri)) {
                return shortUri; // 成功，直接返回
            }
        }

        throw new ServiceException("短链接生成失败，请稍后再试");
    }

    private String generateSuffixByLock(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
//        String shorUrl;
//        String createShortLinkDefaultDomain = requestParam.getDomain();
//        while (customGenerateCount < 10) {
//            customGenerateCount++;
//            String originUrl = requestParam.getOriginUrl();
//            originUrl += UUID.randomUUID().toString();
//            shorUrl = HashUtils.hashToBase62(originUrl);
//            // check
//            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
//                    .eq(ShortLinkDO::getGid, requestParam.getGid())
//                    .eq(ShortLinkDO::getFullShortUrl, createShortLinkDefaultDomain + "/" + shorUrl)
//                    .eq(ShortLinkDO::getDelFlag, 0);
//            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
//        }
        return "";
    }

}
