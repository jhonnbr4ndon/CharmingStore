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

@RestController
@RequestMapping("/api/auth")
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
    public ResponseEntity<String> register(@RequestBody User user) {
        userService.saveUser(user, User.RoleName.ROLE_USER); // Role padrão
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    @PostMapping("/register/admin")
    public ResponseEntity<String> registerAdmin(@RequestBody User user) {
        userService.saveUser(user, User.RoleName.ROLE_ADMIN); // Role de admin
        return ResponseEntity.ok("Administrador registrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = authentication.getAuthorities().iterator().next().getAuthority(); // Obtem o role do usuário
        String token = jwtUtil.generateToken(authentication.getName(), role);

        return ResponseEntity.ok(token);
    }
}

