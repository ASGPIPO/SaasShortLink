package org.shortlinkbyself.pipo.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.shortlinkbyself.pipo.admin.common.convention.exception.ClientException;
import org.shortlinkbyself.pipo.admin.common.convention.exception.ServiceException;
import org.shortlinkbyself.pipo.admin.common.enums.UserErrorCodeEnum;
import org.shortlinkbyself.pipo.admin.dao.entity.UserDO;
import org.shortlinkbyself.pipo.admin.dao.mapper.UserMapper;
import org.shortlinkbyself.pipo.admin.dto.req.UserLoginReqDTO;
import org.shortlinkbyself.pipo.admin.dto.req.UserRegisterReqDTO;
import org.shortlinkbyself.pipo.admin.dto.req.UserUpdateReqDTO;
import org.shortlinkbyself.pipo.admin.dto.resp.UserLoginRespDTO;
import org.shortlinkbyself.pipo.admin.dto.resp.UserRespDTO;
import org.shortlinkbyself.pipo.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.shortlinkbyself.pipo.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static org.shortlinkbyself.pipo.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
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
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (isUsernameAvailable(requestParam.getUsername()) == false) {
            throw  new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);

        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        if (!lock.tryLock()) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }
        try {
            int insert = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
            if (insert < 1) {
                throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
            }
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
            
        }
        finally {
            lock.unlock();
        }
    }
    @Override
    public void update(UserUpdateReqDTO userUpdateReqDTO) {
        //TODO

        LambdaUpdateWrapper<UserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(UserDO::getUsername, userUpdateReqDTO.getUsername());
        baseMapper.update(BeanUtil.toBean(userUpdateReqDTO, UserDO.class), lambdaUpdateWrapper);

    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaUpdateWrapper<UserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(lambdaUpdateWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }
       Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(USER_LOGIN_KEY + requestParam.getUsername());
        if (CollUtil.isNotEmpty(hasLoginMap)) {
            stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES);
            String token = hasLoginMap.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new ClientException("用户登录错误"));
            return new UserLoginRespDTO(token);
        }
        /**
         * Hash
         * Key：login_用户名
         * Value：
         *  Key：token标识
         *  Val：JSON 字符串（用户信息）
         */
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + requestParam.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES);
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token) != null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + username);
            return;
        }
        throw new ClientException("用户Token不存在或用户未登录");
    }


}
