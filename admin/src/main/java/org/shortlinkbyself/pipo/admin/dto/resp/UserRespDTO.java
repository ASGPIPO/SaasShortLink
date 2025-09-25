package org.shortlinkbyself.pipo.admin.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class UserRespDTO {
    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */

    private String phone;

    /**
     * 邮箱
     */
    private String mail;
}
