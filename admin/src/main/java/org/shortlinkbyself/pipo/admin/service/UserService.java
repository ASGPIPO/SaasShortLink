package org.shortlinkbyself.pipo.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shortlinkbyself.pipo.admin.dao.entity.UserDO;
import org.shortlinkbyself.pipo.admin.dto.req.UserLoginReqDTO;
import org.shortlinkbyself.pipo.admin.dto.req.UserRegisterReqDTO;
import org.shortlinkbyself.pipo.admin.dto.req.UserUpdateReqDTO;
import org.shortlinkbyself.pipo.admin.dto.resp.UserLoginRespDTO;
import org.shortlinkbyself.pipo.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {
/**
 * 根据用户名获取用户信息
 *
 * @param username 用户名，用于查询的用户名
 * @return UserRespDTO 返回用户信息响应对象，包含用户的相关数据
 */
    public UserRespDTO getUserByUsername(String username);


    public Boolean isUsernameAvailable(String username);

    public void  register(UserRegisterReqDTO requestParam);

    public void  update(UserUpdateReqDTO requestParam);

    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    Boolean checkLogin(String username, String token);

    void logout(String username, String token);
}
