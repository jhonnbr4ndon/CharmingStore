package com.charmosaPlus.Charmosa.config;

import com.charmosaPlus.Charmosa.Service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          UserService userService,
                          PasswordEncoder passwordEncoder) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("USER", "ADMIN") // GET permitido para USER e ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN") // POST permitido apenas para ADMIN
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN") // PUT permitido apenas para ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN") // DELETE permitido apenas para ADMIN
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions().disable());

        // Adiciona o filtro JWT antes do UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}
