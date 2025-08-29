package com.intervale.diagnostictool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Disable CSRF protection
        http = http.csrf(csrf -> csrf.disable());
        
        // Enable CORS if needed
        http = http.cors(cors -> cors.disable());
        
        // Configure request authorization
        http.authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll()
        );
        
        // Disable frame options for H2 console if needed
        http.headers(headers -> headers
            .frameOptions(frame -> frame.disable())
        );
        
        // Disable default login form and HTTP basic authentication
        http.formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
    
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}