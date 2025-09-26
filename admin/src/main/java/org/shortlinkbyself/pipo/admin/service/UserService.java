package org.shortlinkbyself.pipo.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shortlinkbyself.pipo.admin.dao.entity.UserDO;
import org.shortlinkbyself.pipo.admin.dto.resp.UserRespDTO;
import org.springframework.stereotype.Service;

/**
 * 用户接口层
 */
@Service
public interface UserService extends IService<UserDO> {
/**
 * 根据用户名获取用户信息
 *
 * @param username 用户名，用于查询的用户名
 * @return UserRespDTO 返回用户信息响应对象，包含用户的相关数据
 */
    public UserRespDTO getUserByUsername(String username);


    public Boolean isUsernameAvailable(String username);
}
