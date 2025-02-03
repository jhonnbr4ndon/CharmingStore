package com.charmosaPlus.Charmosa.controller;

import com.charmosaPlus.Charmosa.Service.UserService;
import com.charmosaPlus.Charmosa.config.JwtUtil;
import com.charmosaPlus.Charmosa.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            userService.saveUser(user, User.RoleName.ROLE_USER); // Role padrão
            // Retorna a mensagem no formato JSON
            return ResponseEntity.ok(Collections.singletonMap("message", "Usuário registrado com sucesso!"));
        } catch (IllegalArgumentException e) {
            // Retorna o erro no formato JSON também
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody User user) {
        try {
            userService.saveUser(user, User.RoleName.ROLE_ADMIN); // Role de admin
            return ResponseEntity.ok("Administrador registrado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String role = authentication.getAuthorities().iterator().next().getAuthority(); // Obtém o role do usuário

            // Remove o prefixo "ROLE_" do papel
            role = role.replace("ROLE_", "");

            String token = jwtUtil.generateToken(authentication.getName(), role);

            // Retorna o token e o papel simplificado
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", role); // Exemplo: retorna "USER" ou "ADMIN"

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Credenciais inválidas!"));
        }
    }
}


