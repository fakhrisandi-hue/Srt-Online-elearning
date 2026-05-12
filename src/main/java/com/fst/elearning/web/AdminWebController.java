package com.fst.elearning.web;

import com.fst.elearning.entity.RoleEnum;
import com.fst.elearning.service.CoursService;
import com.fst.elearning.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final UserService userService;
    private final CoursService coursService;

    public AdminWebController(UserService userService, CoursService coursService) {
        this.userService = userService;
        this.coursService = coursService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("totalApprenants", userService.countByRole(RoleEnum.APPRENANT));
        model.addAttribute("totalFormateurs", userService.countByRole(RoleEnum.FORMATEUR));
        model.addAttribute("totalAdmins", userService.countByRole(RoleEnum.ADMIN));

        var allCours = coursService.getAllCoursPublics(PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "dateCreation")));
        model.addAttribute("totalCours", allCours.getTotalElements());
        model.addAttribute("recentCours", coursService.getAllCoursPublics(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dateCreation"))).getContent());
        model.addAttribute("recentUsers", userService.getAllUsers().stream()
                .sorted((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()))
                .limit(5).toList());

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String role, Model model) {
        if (role != null && !role.isEmpty()) {
            model.addAttribute("users", userService.getUsersByRole(RoleEnum.valueOf(role)));
            model.addAttribute("selectedRole", role);
        } else {
            model.addAttribute("users", userService.getAllUsers());
        }
        model.addAttribute("roles", RoleEnum.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            var user = userService.toggleActif(id);
            redirectAttributes.addFlashAttribute("message",
                    "Utilisateur " + (user.getActif() ? "activé" : "désactivé") + " avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "Utilisateur supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/cours")
    public String cours(Model model) {
        var allCours = coursService.getAllCoursPublics(
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "dateCreation")));
        model.addAttribute("coursList", allCours.getContent());
        return "admin/cours";
    }
}
