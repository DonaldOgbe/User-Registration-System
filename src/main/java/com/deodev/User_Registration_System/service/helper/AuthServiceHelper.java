package com.deodev.User_Registration_System.service.helper;

import com.deodev.User_Registration_System.model.Role;
import com.deodev.User_Registration_System.model.User;
import com.deodev.User_Registration_System.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceHelper {

    private final JwtUtil jwtUtil;

    public Map<String, String> generateTokens(List<String> authorities, User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", authorities);
        claims.put("userId", user.getId());

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    public List<String> getAuthoritiesFromRoles(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .toList();
    }

    public List<String> getAuthorities(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}
