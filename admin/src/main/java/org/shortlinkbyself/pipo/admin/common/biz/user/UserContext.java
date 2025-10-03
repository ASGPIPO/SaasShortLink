package org.shortlinkbyself.pipo.admin.common.biz.user;

import com.alibaba.ttl.TransmittableThreadLocal;
import java.util.Optional;

/**
 * 用户上下文信息管理工具类
 * 基于 TransmittableThreadLocal 实现父子线程间上下文传递
 */
public final class UserContext {
    private static final ThreadLocal<UserInfoDTO> USER_THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 设置用户信息到上下文
     *
     * @param user 用户信息
     */
    public static void setUser(UserInfoDTO user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 从上下文中获取用户ID
     *
     * @return 用户ID，如果上下文中没有用户信息则返回null
     */
    public static String getUserId() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUserId).orElse(null);
    }
    
    /**
     * 从上下文中获取用户名
     *
     * @return 用户名，如果上下文中没有用户信息则返回null
     */
    public static String getUsername() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUsername).orElse(null);
    }
    
    /**
     * 从上下文中获取用户真实姓名
     *
     * @return 用户真实姓名，如果上下文中没有用户信息则返回null
     */
    public static String getRealName() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getRealName).orElse(null);
    }
    
//    /**
//     * 获取完整的用户信息
//     *
//     * @return 用户信息DTO，如果上下文中没有用户信息则返回null
//     */
//    public static UserInfoDTO getUser() {
//        return USER_THREAD_LOCAL.get();
//    }
//
    /**
     * 清理当前线程的用户上下文信息
     * 防止内存泄漏，建议在请求处理完成后调用
     */
    public static void removeUser() {
        USER_THREAD_LOCAL.remove();
    }
}
