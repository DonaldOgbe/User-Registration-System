package com.deodev.User_Registration_System.util;

import com.deodev.User_Registration_System.config.CustomUserDetails;
import com.deodev.User_Registration_System.exception.TokenValidationException;
import com.deodev.User_Registration_System.model.Role;
import com.deodev.User_Registration_System.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class JwtUtilTest {

    private JwtUtil jwtUtil;
    private JwtSecretUtil jwtSecretUtil;
    private String subject;
    private UUID userId;
    private Map<String, Object> extraClaims;
    private List<String> authorities;
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setup() {
        jwtSecretUtil = new JwtSecretUtil();
        jwtSecretUtil.setSecret("e85195ab341a234687f134ec27047803c92957d7e89fb3c49fab1e25de6b6bb00216ed7f56885dea278b38034ce091ac3cac59af468b5e4f27490cbbca13dadf");
        jwtSecretUtil.setExpiration(new JwtSecretUtil.Expiration());
        jwtSecretUtil.getExpiration().setAccess(1);
        jwtSecretUtil.getExpiration().setRefresh(7);
        userDetailsService = Mockito.mock(UserDetailsService.class);
        jwtUtil = new JwtUtil(jwtSecretUtil, userDetailsService);
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
        User user = User.builder()
                .email(subject)
                .roles(Set.of(Role.builder().name("USER").build(), Role.builder().name("ADMIN").build()))
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);

        // when
        Authentication authentication = jwtUtil.getAuthenticationFromToken(token);
        List<String> tokenAuthorities = authentication.getAuthorities().stream()
                .map(Object::toString)
                .toList();

        // then
        assertThat(userDetails).isEqualTo(authentication.getPrincipal());
        assertThat(tokenAuthorities).contains("USER", "ADMIN");
    }


    Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecretUtil.getSecret().getBytes(StandardCharsets.UTF_8));
    }

}