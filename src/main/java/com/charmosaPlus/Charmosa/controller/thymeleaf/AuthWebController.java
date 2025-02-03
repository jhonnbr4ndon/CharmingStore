package com.charmosaPlus.Charmosa.controller.thymeleaf;

import org.springframework.ui.Model;
import com.charmosaPlus.Charmosa.Service.UserService;
import com.charmosaPlus.Charmosa.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class AuthWebController {

    private final UserService userService;

    public AuthWebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Thymeleaf template login.html
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User()); // Passa um objeto User para o formulário
        return "register"; // Thymeleaf template register.html
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register"; // Retorna para o formulário se houver erros
        }
        try {
            userService.saveUser(user, User.RoleName.ROLE_ADMIN); // Salva como ADMIN
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage()); // Adiciona erro ao modelo
            return "register";
        }
        return "redirect:/login"; // Redireciona para a página de login
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("content", "default-content");
        return "home"; // Renderiza o layout principal
    }
}