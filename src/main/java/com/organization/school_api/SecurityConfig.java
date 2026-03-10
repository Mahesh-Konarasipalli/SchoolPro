package com.organization.school_api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                // ADDED "/error" so if Thymeleaf crashes, it actually shows you the error instead of looping
                .requestMatchers("/login", "/signup", "/verify-otp", "/forgot-password", "/reset-password", "/css/**", "/images/**", "/js/**", "/error").permitAll()
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/student/**").hasAuthority("STUDENT")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login") // Matches your HTML action
                .usernameParameter("username")    
                .passwordParameter("password")
                .failureUrl("/login?error=true")
                .successHandler((request, response, authentication) -> {
                    request.getSession().setAttribute("email", authentication.getName());
                    boolean isAdmin = authentication.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ADMIN"));
                    
                    if (isAdmin) response.sendRedirect("/admin/dashboard");
                    else response.sendRedirect("/student/dashboard");
                })
                .permitAll() 
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }
}