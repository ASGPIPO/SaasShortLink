package org.shortlinkbyself.pipo.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.shortlinkbyself.pipo.admin.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final UserService userService;

    private static final Set<String> PERMIT_ALL_PATHS = Set.of(
            "/api/short-link/admin/v1/user/login",
            "/api/short-link/admin/v1/user"


    );

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        String requestURI = httpServletRequest.getRequestURI();
        if (isPermitAll(requestURI)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String username = httpServletRequest.getHeader("username");
        String token = httpServletRequest.getHeader("token");

        if (StrUtil.isBlank(username) || StrUtil.isBlank(token)) {
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpServletResponse.getWriter().write("Unauthorized");
            return;
        }
        boolean isLogin = userService.checkLogin(username, token);
        if (!isLogin) {
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpServletResponse.getWriter().write("Unauthorized");
            return;
        }
        if (StrUtil.isNotBlank(username)) {
            String userId = httpServletRequest.getHeader("userId");

            UserInfoDTO userInfoDTO = new UserInfoDTO(userId, username, token);
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
    private boolean isPermitAll(String requestURI) {
        return PERMIT_ALL_PATHS.stream().anyMatch(requestURI::startsWith);
    }


}
