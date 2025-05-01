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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
                .cors().configurationSource(corsConfigurationSource()).and()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/h2-console/**").permitAll()
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/","/web/products/**").hasRole("ADMIN") // Apenas ADMIN pode acessar criar produtos
                        .requestMatchers(HttpMethod.GET, "/products/**").hasAnyRole("USER", "ADMIN") // Apenas ADMIN pode acessar produtos
                        .requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMIN") // POST permitido apenas para ADMIN
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN") // PUT permitido apenas para ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN") // DELETE permitido apenas para ADMIN
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Página customizada de login
                        .defaultSuccessUrl("/", true) // Redireciona após login bem-sucedido
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                )
                // Removido ou alterado para manter o estado da sessão
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .headers(headers -> headers.frameOptions().disable());

        // Adiciona o filtro JWT antes do UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        //configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200")); // URL do Angular
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true); // Permitir envio de cookies/autenticação

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
