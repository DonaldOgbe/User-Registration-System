package com.deodev.User_Registration_System.service;


import com.deodev.User_Registration_System.exception.ResourceNotFoundException;
import com.deodev.User_Registration_System.model.Role;
import com.deodev.User_Registration_System.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role getDefaultRole() {
        return roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
    }
}
