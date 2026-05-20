// JwtAuthenticationFilter.java
package com.pcadvisor.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcadvisor.common.Result;
import com.pcadvisor.common.enums.ErrorCode;
import com.pcadvisor.common.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                // 直接解析token，不通过validateToken（它吞掉了ExpiredJwtException）
                var claims = jwtUtil.parseToken(token);
                String userId = claims.get("userId", String.class);
                String userType = claims.get("userType", String.class);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userType))
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("用户认证成功: {}", userId);
            } catch (ExpiredJwtException e) {
                log.warn("JWT已过期: {}", e.getMessage());
                writeUnauthorizedResponse(response, "登录已过期，请重新登录");
                return;
            } catch (Exception e) {
                log.error("JWT认证失败: {}", e.getMessage());
                writeUnauthorizedResponse(response, "Token无效");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<Void> result = Result.error(ErrorCode.UNAUTHORIZED.getCode(), message);
        String json = new ObjectMapper().writeValueAsString(result);
        response.getWriter().write(json);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
