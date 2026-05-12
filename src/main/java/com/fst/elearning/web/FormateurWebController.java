package com.fst.elearning.web;

import com.fst.elearning.entity.*;
import com.fst.elearning.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/formateur")
public class FormateurWebController {

    private final UserService userService;
    private final CoursService coursService;
    private final ModuleService moduleService;
    private final LeconService leconService;
    private final QuizService quizService;
    private final InscriptionService inscriptionService;
    private final ProgressionService progressionService;

    public FormateurWebController(UserService userService, CoursService coursService,
                                   ModuleService moduleService, LeconService leconService,
                                   QuizService quizService, InscriptionService inscriptionService,
                                   ProgressionService progressionService) {
        this.userService = userService;
        this.coursService = coursService;
        this.moduleService = moduleService;
        this.leconService = leconService;
        this.quizService = quizService;
        this.inscriptionService = inscriptionService;
        this.progressionService = progressionService;
    }

    private User getCurrentUser(Principal principal) {
        return userService.getUserByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User formateur = getCurrentUser(principal);
        var mesCours = coursService.getCoursParFormateur(formateur.getId());

        long totalApprenants = 0;
        for (var cours : mesCours) {
            totalApprenants += inscriptionService.getNbInscrits(cours.getId());
        }

        model.addAttribute("formateur", formateur);
        model.addAttribute("totalCours", mesCours.size());
        model.addAttribute("totalApprenants", totalApprenants);
        model.addAttribute("mesCours", mesCours);
        return "formateur/dashboard";
    }

    @GetMapping("/cours")
    public String mesCours(Principal principal, Model model) {
        User formateur = getCurrentUser(principal);
        var mesCours = coursService.getCoursParFormateur(formateur.getId());

        List<Map<String, Object>> coursData = new ArrayList<>();
        for (var cours : mesCours) {
            Map<String, Object> data = new HashMap<>();
            data.put("cours", cours);
            data.put("nbInscrits", inscriptionService.getNbInscrits(cours.getId()));
            data.put("nbModules", moduleService.getModulesDuCours(cours.getId()).size());
            coursData.add(data);
        }

        model.addAttribute("coursData", coursData);
        return "formateur/cours-list";
    }

    @GetMapping("/cours/new")
    public String newCoursForm(Model model) {
        model.addAttribute("niveaux", NiveauEnum.values());
        return "formateur/cours-form";
    }

    @PostMapping("/cours")
    public String createCours(@RequestParam String titre,
                               @RequestParam String description,
                               @RequestParam String categorie,
                               @RequestParam String niveau,
                               @RequestParam(required = false) MultipartFile image,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            User formateur = getCurrentUser(principal);
            Cours cours = new Cours();
            cours.setTitre(titre);
            cours.setDescription(description);
            cours.setCategorie(categorie);
            cours.setNiveau(NiveauEnum.valueOf(niveau));

            if (image != null && !image.isEmpty()) {
                String imageUrl = coursService.uploadImage(image);
                cours.setImageUrl(imageUrl);
            }

            coursService.createCours(cours, formateur);
            redirectAttributes.addFlashAttribute("message", "Cours créé avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/formateur/cours";
    }

    @GetMapping("/cours/{id}/edit")
    public String editCoursForm(@PathVariable Long id, Model model, Principal principal) {
        User formateur = getCurrentUser(principal);
        var cours = coursService.getCoursById(id)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
        if (!cours.getFormateur().getId().equals(formateur.getId())) {
            return "redirect:/formateur/cours";
        }
        model.addAttribute("cours", cours);
        model.addAttribute("niveaux", NiveauEnum.values());
        return "formateur/cours-form";
    }

    @PostMapping("/cours/{id}/edit")
    public String updateCours(@PathVariable Long id,
                               @RequestParam String titre,
                               @RequestParam String description,
                               @RequestParam String categorie,
                               @RequestParam String niveau,
                               @RequestParam(required = false) MultipartFile image,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            User formateur = getCurrentUser(principal);
            var cours = coursService.getCoursById(id)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
            if (!cours.getFormateur().getId().equals(formateur.getId())) {
                redirectAttributes.addFlashAttribute("error", "Accès refusé");
                return "redirect:/formateur/cours";
            }

            Cours details = new Cours();
            details.setTitre(titre);
            details.setDescription(description);
            details.setCategorie(categorie);
            details.setNiveau(NiveauEnum.valueOf(niveau));
            details.setActif(cours.getActif());

            if (image != null && !image.isEmpty()) {
                String imageUrl = coursService.updateImageForCours(id, image);
                details.setImageUrl(imageUrl);
            } else {
                details.setImageUrl(cours.getImageUrl());
            }

            coursService.updateCours(id, details);
            redirectAttributes.addFlashAttribute("message", "Cours mis à jour !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/formateur/cours";
    }

    @GetMapping("/cours/{id}/modules")
    public String modulesManager(@PathVariable Long id, Model model, Principal principal) {
        User formateur = getCurrentUser(principal);
        var cours = coursService.getCoursById(id)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
        if (!cours.getFormateur().getId().equals(formateur.getId())) {
            return "redirect:/formateur/cours";
        }

        var modules = moduleService.getModulesDuCours(id);
        List<Map<String, Object>> modulesData = new ArrayList<>();
        for (var module : modules) {
            Map<String, Object> data = new HashMap<>();
            data.put("module", module);
            data.put("lecons", leconService.getLeconsDuModule(module.getId()));
            data.put("quizzes", quizService.getQuizzesParModule(module.getId()));
            modulesData.add(data);
        }

        model.addAttribute("cours", cours);
        model.addAttribute("modulesData", modulesData);
        return "formateur/modules-manager";
    }

    @PostMapping("/cours/{coursId}/modules")
    public String addModule(@PathVariable Long coursId,
                             @RequestParam String titre,
                             @RequestParam(required = false) String description,
                             @RequestParam Integer ordre,
                             RedirectAttributes redirectAttributes) {
        try {
            var cours = coursService.getCoursById(coursId)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
            com.fst.elearning.entity.Module module = new com.fst.elearning.entity.Module(titre, description, ordre, cours);
            moduleService.createModule(module);
            redirectAttributes.addFlashAttribute("message", "Module ajouté !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/formateur/cours/" + coursId + "/modules";
    }

    @PostMapping("/modules/{moduleId}/lecons")
    public String addLecon(@PathVariable Long moduleId,
                            @RequestParam String titre,
                            @RequestParam String contenu,
                            @RequestParam Integer ordre,
                            @RequestParam Integer dureeMin,
                            @RequestParam Long coursId,
                            RedirectAttributes redirectAttributes) {
        try {
            var module = moduleService.getModuleById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module non trouvé"));
            Lecon lecon = new Lecon(titre, contenu, ordre, dureeMin, module);
            leconService.createLecon(lecon);
            redirectAttributes.addFlashAttribute("message", "Leçon ajoutée !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/formateur/cours/" + coursId + "/modules";
    }

    @GetMapping("/cours/{id}/apprenants")
    public String apprenants(@PathVariable Long id, Model model, Principal principal) {
        User formateur = getCurrentUser(principal);
        var cours = coursService.getCoursById(id)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
        if (!cours.getFormateur().getId().equals(formateur.getId())) {
            return "redirect:/formateur/cours";
        }

        var inscriptions = inscriptionService.getApprenantsDuCours(id);
        List<Map<String, Object>> apprenantsData = new ArrayList<>();
        for (var inscription : inscriptions) {
            Map<String, Object> data = new HashMap<>();
            data.put("inscription", inscription);
            data.put("apprenant", inscription.getApprenant());
            data.put("progression", progressionService.getProgressionPourcentageCours(
                    inscription.getApprenant().getId(), id));
            apprenantsData.add(data);
        }

        model.addAttribute("cours", cours);
        model.addAttribute("apprenantsData", apprenantsData);
        return "formateur/apprenants";
    }
}
