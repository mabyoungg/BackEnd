package com.sparta.i_mu.filter;

import com.sparta.i_mu.global.errorCode.ErrorCode;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.global.util.RedisUtil;
import com.sparta.i_mu.security.UserDetailsServiceImpl;
import com.sparta.i_mu.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthService authService;
    private final RedisUtil redisUtil;

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtUtil.getAccessTokenFromRequest(request);

        if (StringUtils.hasText(accessToken)) { // accessToken이 없을때
            try {
                // 블랙리스트 확인
                if (!redisUtil.isBlacklisted(accessToken).equals(accessToken)) {
                    ResponseResource.error(ErrorCode.BLACKLISTED.getMessage(), ErrorCode.BLACKLISTED.getStatus());
                    return;
                }
                // 토큰 확인
                if (!authService.isAccessTokenValid(accessToken)) {
                    log.warn("AccessToken 토큰 검증 실패 -> RefreshToken 요청");
                    ResponseResource.error(ErrorCode.TOKEN_INVALID.getMessage(), ErrorCode.TOKEN_INVALID.getStatus());
                    return;
                }
                String userNickname = jwtUtil.getUserInfoFromToken(accessToken).getSubject();
                log.info("현재 유저 :{}", userNickname);
                setAuthentication(userNickname);
                //7일간격으로 refreshToken을 자동으로 재발급
                authService.refreshTokenRegularly(accessToken, response);
                log.info("현재 유저 :{}", userNickname);

            } catch (Exception e) {
                log.error(e.getMessage());
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(e.getMessage());
            }
        }
        // accessToken 이 없을때
        filterChain.doFilter(request, response);
    }

    // 인증 처리
    private void setAuthentication(String nickname) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(nickname);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
    //인증 객체 생성
    private Authentication createAuthentication(String nickname) {
        UserDetails jwtUserDetails = userDetailsService.loadUserByUsername(nickname);
        return new UsernamePasswordAuthenticationToken(jwtUserDetails, null, jwtUserDetails.getAuthorities());
    }
}