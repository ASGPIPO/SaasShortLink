package org.shortlinkbyself.pipo.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.shortlinkbyself.pipo.admin.common.convention.result.Result;
import org.shortlinkbyself.pipo.admin.common.convention.result.Results;
import org.shortlinkbyself.pipo.admin.dto.req.UserLoginReqDTO;
import org.shortlinkbyself.pipo.admin.dto.req.UserRegisterReqDTO;
import org.shortlinkbyself.pipo.admin.dto.req.UserUpdateReqDTO;
import org.shortlinkbyself.pipo.admin.dto.resp.UserActualRespDTO;
import org.shortlinkbyself.pipo.admin.dto.resp.UserLoginRespDTO;
import org.shortlinkbyself.pipo.admin.dto.resp.UserRespDTO;
import org.shortlinkbyself.pipo.admin.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
@RestController
@RequestMapping("/api/short-link/admin/v1")
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
    public Result<Boolean> isUsernameAvailable(@PathVariable("username") String username) {
        return Results.success(userService.isUsernameAvailable(username));
    }

    @PostMapping("/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return  Results.success();
    }


    @PutMapping("/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO requestParam) {
        userService.update(requestParam);
        return Results.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam) {
        return Results.success(userService.login(requestParam));
    }

    /**
     * 检查用户是否登录
     */
    @GetMapping("/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username, @RequestParam("token") String token) {
        return Results.success(userService.checkLogin(username, token));
    }

    /**
     * 用户退出登录
     */
    @DeleteMapping("/user/logout")
    public Result<Void> logout(@RequestParam("username") String username, @RequestParam("token") String token) {
        userService.logout(username, token);
        return Results.success();
    }

}
