package org.shortlinkbyself.pipo.project.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shortlinkbyself.pipo.project.dao.enity.ShortLinkDO;
import org.shortlinkbyself.pipo.project.dto.ShortLinkCreateReqDTO;
import org.shortlinkbyself.pipo.project.dto.ShortLinkCreateRespDTO;
import org.shortlinkbyself.pipo.project.service.ShortLinkService;
import org.shortlinkbyself.pipo.project.tookit.HashUtils;

public class ShortLinkServiceImpl implements ShortLinkService {

    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {

        return null;
    }

    private String generateSuffixByLock(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
        String shorUrl;

//        while (customGenerateCount < 10) {
//            customGenerateCount++;
//            String originUrl = requestParam.getOriginUrl();
//            originUrl += UUID.randomUUID().toString();
//            shorUrl = HashUtils.hashToBase62(originUrl);
//            // check
//            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
//                    .eq(ShortLinkDO::getGid, requestParam.getGid())
//                    .eq(ShortLinkDO::getFullShortUrl, createShortLinkDefaultDomain + "/" + shorUri)
//                    .eq(ShortLinkDO::getDelFlag, 0);
//            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
//        }
    }

}
