package org.shortlinkbyself.pipo.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.shortlinkbyself.pipo.admin.common.convention.result.Result;
import org.shortlinkbyself.pipo.admin.dto.resp.UserActualRespDTO;
import org.shortlinkbyself.pipo.admin.dto.resp.UserRespDTO;
import org.shortlinkbyself.pipo.admin.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.shortlinkbyself.pipo.admin.service.impl.UserServiceImpl;
import org.shortlinkbyself.pipo.admin.common.convention.result.Results;
/**
 * 用户管理控制层
 */
@RestController
@RequestMapping("http://127.0.0.1:8000/api/short-link/admin/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));

    }

    @GetMapping("/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable("username") String username) {
        return Results.success(BeanUtil.toBean(userService.getUserByUsername(username), UserActualRespDTO.class));
    }

    @GetMapping("user/has-username")
    public Result<Boolean> hasUsername(@PathVariable("username") String username) {
        return Results.success(userService.hasUsername(username));
    }


}
