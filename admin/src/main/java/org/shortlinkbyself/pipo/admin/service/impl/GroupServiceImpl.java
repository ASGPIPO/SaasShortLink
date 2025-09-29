package org.shortlinkbyself.pipo.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.shortlinkbyself.pipo.admin.common.convention.exception.ClientException;
import org.shortlinkbyself.pipo.admin.common.convention.exception.ServiceException;
import org.shortlinkbyself.pipo.admin.dao.entity.GroupDO;
import org.shortlinkbyself.pipo.admin.dao.entity.GroupUniqueDO;
import org.shortlinkbyself.pipo.admin.dao.mapper.GroupMapper;
import org.shortlinkbyself.pipo.admin.dao.mapper.GroupUniqueMapper;
import org.shortlinkbyself.pipo.admin.service.GroupService;
import org.shortlinkbyself.pipo.admin.tookit.RandomGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.shortlinkbyself.pipo.admin.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Value("${short-link.group.max-num:20}")
    private Integer groupMaxNum;
    private final RBloomFilter<String> gidRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final GroupUniqueMapper groupUniqueMapper;


    @Override
    public void saveGroup(String groupName) {
        saveGroup("test", groupName);
        //TODO saveGroup(UserContext.getUsername(), groupName);
    }

    @Override
    public void saveGroup(String username, String groupName) {
        RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, username));
        lock.lock();
        try {
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getUsername, username)
                    .eq(GroupDO::getDelFlag, 0);
            List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
            if (CollUtil.isNotEmpty(groupDOList) && groupDOList.size() == groupMaxNum) {
                throw new ClientException(String.format("已超出最大分组数：%d", groupMaxNum));
            }
            int retryCount = 0;
            int maxRetries = 10;
            String gid = null;
            while (retryCount < maxRetries) {
                gid = saveGroupUniqueReturnGid();
                if (StrUtil.isNotEmpty(gid)) {
                    GroupDO groupDO = GroupDO.builder()
                            .gid(gid)
                            .sortOrder(0)
                            .username(username)
                            .name(groupName)
                            .build();
                    baseMapper.insert(groupDO);
                    gidRegisterCachePenetrationBloomFilter.add(gid);
                    break;
                }
                retryCount++;
            }
            if (StrUtil.isEmpty(gid)) {
                throw new ServiceException("生成分组标识频繁");
            }
        } finally {
            lock.unlock();
        }
    }






    private String saveGroupUniqueReturnGid() {
        String gid = RandomGenerator.generateRandom();
        if (gidRegisterCachePenetrationBloomFilter.contains(gid)) {
            return null;
        }
        GroupUniqueDO groupUniqueDO = GroupUniqueDO.builder()
                .gid(gid)
                .build();
        try {
            groupUniqueMapper.insert(groupUniqueDO);
        } catch (DuplicateKeyException e) {
            return null;
        }
        return gid;
    }
}
