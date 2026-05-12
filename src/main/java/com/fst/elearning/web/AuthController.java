package com.fst.elearning.web;

import com.fst.elearning.entity.RoleEnum;
import com.fst.elearning.entity.User;
import com.fst.elearning.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Email ou mot de passe incorrect");
        }
        if (logout != null) {
            model.addAttribute("message", "Déconnexion réussie");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam String role,
                           RedirectAttributes redirectAttributes) {
        try {
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas");
                return "redirect:/register";
            }

            if (userService.getUserByEmail(email).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Cet email est déjà utilisé");
                return "redirect:/register";
            }

            User user = new User(name, email, password);
            user.setRole(RoleEnum.valueOf(role));
            userService.createUser(user);

            redirectAttributes.addFlashAttribute("message", "Compte créé avec succès ! Connectez-vous.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'inscription: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/")
    public String home(Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        var user = userService.getUserByEmail(principal.getName());
        if (user.isPresent()) {
            return switch (user.get().getRole()) {
                case ADMIN -> "redirect:/admin/dashboard";
                case FORMATEUR -> "redirect:/formateur/dashboard";
                case APPRENANT -> "redirect:/apprenant/dashboard";
            };
        }
        return "redirect:/login";
    }
}
