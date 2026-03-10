package com.organization.school_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.organization.school_api.Entity.User;
import com.organization.school_api.repository.userRepository;

import java.util.Collections;


@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private userRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        
        // 1. Try finding by Email first (For Admins/Faculty)
        User user = userRepository.findByEmail(identifier).orElse(null);
        
        // 2. If not found by Email, try finding by Student ID (For Students)
        if (user == null) {
            user = userRepository.findByStudentId(identifier).orElse(null);
        }

        // 3. If STILL not found, throw an error
        if (user == null) {
            throw new UsernameNotFoundException("Invalid Email or Student ID");
        }

        // 4. Return the Spring Security User object
        return new org.springframework.security.core.userdetails.User(
                // We use the email as the main internal identifier for Spring Security, 
                // even if they logged in with a Student ID!
                user.getEmail(), 
                user.getPassword(),
                // Translate the string role to a Spring Security authority
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole())) 
        );
    }
}
