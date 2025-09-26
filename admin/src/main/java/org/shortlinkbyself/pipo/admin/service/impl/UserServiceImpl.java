package org.shortlinkbyself.pipo.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.shortlinkbyself.pipo.admin.common.convention.exception.ServiceException;
import org.shortlinkbyself.pipo.admin.common.enums.UserErrorCodeEnum;
import org.shortlinkbyself.pipo.admin.dao.entity.UserDO;
import org.shortlinkbyself.pipo.admin.dao.mapper.UserMapper;
import org.shortlinkbyself.pipo.admin.dto.resp.UserRespDTO;
import org.shortlinkbyself.pipo.admin.service.UserService;
import org.springframework.beans.BeanUtils;

@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ServiceException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO respDTO = new UserRespDTO();
        BeanUtils.copyProperties(userDO, respDTO);
        return respDTO;
    }

    @Override
    public Boolean isUsernameAvailable(String username)
        {
            return !userRegisterCachePenetrationBloomFilter.contains(username);
        }
}
