package com.deodev.User_Registration_System.util;

import com.deodev.User_Registration_System.exception.TokenValidationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class JwtUtilTest {

    private JwtUtil jwtUtil;
    private JwtSecretUtil jwtSecretUtil;
    private String subject;
    private UUID userId;
    private Map<String, Object> extraClaims;
    private List<String> authorities;

    @BeforeEach
    void setup() {
        jwtSecretUtil = new JwtSecretUtil();
        jwtSecretUtil.setSecret("e85195ab341a234687f134ec27047803c92957d7e89fb3c49fab1e25de6b6bb00216ed7f56885dea278b38034ce091ac3cac59af468b5e4f27490cbbca13dadf");
        jwtSecretUtil.setExpiration(new JwtSecretUtil.Expiration());
        jwtSecretUtil.getExpiration().setAccess(1);
        jwtSecretUtil.getExpiration().setRefresh(7);
        jwtUtil = new JwtUtil(jwtSecretUtil);
        subject = "testSubject";
        userId = UUID.randomUUID();
        extraClaims = new HashMap<>();
        extraClaims.put("subjectId", "1234");
        authorities = List.of("USER", "ADMIN");
        extraClaims.put("authorities", authorities);
        extraClaims.put("userId", userId);
    }

    @Test
    void testIfExpiredTokenThrowsError() {
        // given
        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 10L))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        // when + then
        assertThrows(TokenValidationException.class, () -> {
            jwtUtil.validateToken(token);
        });
    }

    @Test
    void testIfMethodGetsAuthorities() {
        // given
        String token = jwtUtil.generateAccessToken(subject, extraClaims);

        // when
        List<String> result = jwtUtil.getAuthoritiesFromToken(token);

        // then
        assertThat(result).contains("USER", "ADMIN");
    }

    @Test
    void getClaimFromClaims() {
        // given
        String token = jwtUtil.generateAccessToken(subject, extraClaims);

        // when
        UUID id = UUID.fromString(jwtUtil.getClaimFromToken(token,
                claims -> (String) claims.get("userId")));

        // then
        assertThat(id).isEqualTo(userId);
    }

    @Test
    void testIfValidAuthenticationIsGenerated() {
        // given
        String token = jwtUtil.generateAccessToken(subject, extraClaims);

        // when
        Authentication authentication = jwtUtil.getAuthenticationFromToken(token);
        List<String> tokenAuthorities = authentication.getAuthorities().stream()
                .map(Object::toString)
                .toList();

        // then
        assertThat(subject).isEqualTo(authentication.getPrincipal());
        assertThat(tokenAuthorities).contains("USER", "ADMIN");
    }


    Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecretUtil.getSecret().getBytes(StandardCharsets.UTF_8));
    }

}