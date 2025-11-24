package com.deodev.User_Registration_System.util;

import com.deodev.User_Registration_System.config.CustomUserDetails;
import com.deodev.User_Registration_System.exception.TokenValidationException;
import com.deodev.User_Registration_System.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtSecretUtil jwtSecretUtil;
    private final UserDetailsService userDetailsService;

    public String generateAccessToken(String subject, Map<String, Object> extraClaims) {
        Date expiration = Date.from(Instant.now().plus(jwtSecretUtil.getExpiration().getAccess(), ChronoUnit.HOURS));
        return createToken(extraClaims, subject, expiration);
    }

    public String generateAccessToken(String subject) {
        Date expiration = Date.from(Instant.now().plus(jwtSecretUtil.getExpiration().getAccess(), ChronoUnit.HOURS));
        return createToken(subject, expiration);
    }

    public String generateRefreshToken(String subject) {
        Date expiration = Date.from(Instant.now().plus(jwtSecretUtil.getExpiration().getRefresh(), ChronoUnit.DAYS));
        return createToken(subject, expiration);
    }

    String createToken(Map<String, Object> extraClaims, String subject, Date expirationDate) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    String createToken(String subject, Date expirationDate) {
        Map<String, Object> extraClaims = new HashMap<>();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public List<String> getAuthoritiesFromToken(String token) {
        return getClaimFromToken(token, claims -> (List<String>) claims.get("authorities"));
    }

    public Authentication getAuthenticationFromToken(String token) {
        try {
            final String username = getUsernameFromToken(token);
            final CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } catch (SignatureException e) {
            throw new TokenValidationException("Invalid Token");
        }
    }


    Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            throw new TokenValidationException("Invalid Token");
        }
    }

    public  <T> T getClaimFromToken(String token, Function<Claims, T> function) {
        final Claims claims = getAllClaimsFromToken(token);
        return function.apply(claims);
    }

    public boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new TokenValidationException("Token Expired", e);
        } catch (SignatureException e) {
            throw new TokenValidationException("Invalid Signature", e);
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT: {}", token);
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT format: {}", token);
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT token is null or empty");
            return false;
        }
    }

    Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecretUtil.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
