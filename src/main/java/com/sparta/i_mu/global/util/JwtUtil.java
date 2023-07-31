package com.sparta.i_mu.global.util;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    public final String HEADER_ACCESS_TOKEN = "AccessToken";
    private final String BEARER = "Bearer ";
    private final Long ACCESS_TOKEN_EXPIRATION_TIME = 60 * 60 * 3000L; // 1시간
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private Key key;
    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    /**
     *  AccessToken 생성 메서드
     * @param email
     * @return
     */
    public String createAccessToken(String email) {
        Date date = new Date();
        return BEARER +
                Jwts.builder()
                        .setSubject(email) // 토큰(사용자) 식별자 값
                        .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_EXPIRATION_TIME)) // 만료일
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘, 시크릿 키
                        .compact();
    }


    /**
     * 헤더에 토큰을 추가 합니다
     * @param accessToken
     * @param response
     */
    public void addTokenToHeader(String accessToken, HttpServletResponse response) {
        response.setHeader(HEADER_ACCESS_TOKEN, accessToken);
    }

    /**
     * 헤더에서 AccessToken 추출
     * @param request
     * @return subString으로 추출된 token
     */
    public String getAccessTokenFromHeader(HttpServletRequest request) {
        String token = request.getHeader(HEADER_ACCESS_TOKEN);
        if (StringUtils.hasText(token)) {
            return substringToken(token);
        }
        return null;
    }

    /**
     *JWT Bearer Substirng 메서드
     * @param token
     * @return subString으로 추출된 token 값
     */
    public String substringToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith(BEARER)) {
            return token.substring(7);
        }
        throw new NullPointerException("토큰의 값이 존재하지 않습니다.");
    }


    /**
     * JWT 토큰의 사용자 정보 가져오는 메서드
     * @param tokenValue
     * @return 추출된 사용자 정보
     */
    public Claims getUserInfo(String tokenValue) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(tokenValue)
                .getBody();
    }


    /**
     *  JWT 검증 메서드
     * @param accessToken
     * @return 토큰 검증 여부
     */
    public boolean validateToken(String accessToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken); // key로 accessToken 검증
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT accessToken, 만료된 JWT accessToken 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT accessToken, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }



}
