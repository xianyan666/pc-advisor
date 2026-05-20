// JwtUtil.java
package com.pcadvisor.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:pcAdvisorSecretKey2024}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration; // 24小时

    @Value("${jwt.issuer:pcAdvisor}")
    private String issuer;

    // 生成密钥（使用SHA-256哈希确保密钥长度满足HS256最低256位要求）
    private SecretKey getSigningKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }

    // 生成token
    public String generateToken(String userId, String userType, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("username", username);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析token（会抛出ExpiredJwtException等异常）
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 验证token
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            log.error("JWT验证失败: {}", e.getMessage());
            return false;
        }
    }

    // 从token中获取用户ID
    public String getUserIdFromToken(String token) {
        return parseToken(token).get("userId", String.class);
    }

    // 从token中获取用户类型
    public String getUserTypeFromToken(String token) {
        return parseToken(token).get("userType", String.class);
    }

    // 刷新token
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        String userId = claims.get("userId", String.class);
        String userType = claims.get("userType", String.class);
        String username = claims.get("username", String.class);

        return generateToken(userId, userType, username);
    }
}