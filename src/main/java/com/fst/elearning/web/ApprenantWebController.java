package com.fst.elearning.web;

import com.fst.elearning.entity.*;
import com.fst.elearning.service.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/apprenant")
public class ApprenantWebController {

    private final UserService userService;
    private final CoursService coursService;
    private final ModuleService moduleService;
    private final LeconService leconService;
    private final QuizService quizService;
    private final InscriptionService inscriptionService;
    private final ProgressionService progressionService;
    private final ReponseApprenantService reponseService;

    public ApprenantWebController(UserService userService, CoursService coursService,
                                   ModuleService moduleService, LeconService leconService,
                                   QuizService quizService, InscriptionService inscriptionService,
                                   ProgressionService progressionService,
                                   ReponseApprenantService reponseService) {
        this.userService = userService;
        this.coursService = coursService;
        this.moduleService = moduleService;
        this.leconService = leconService;
        this.quizService = quizService;
        this.inscriptionService = inscriptionService;
        this.progressionService = progressionService;
        this.reponseService = reponseService;
    }

    private User getCurrentUser(Principal principal) {
        return userService.getUserByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User apprenant = getCurrentUser(principal);
        var mesCours = inscriptionService.getMesCours(apprenant.getId());

        long coursEnCours = mesCours.stream()
                .filter(i -> i.getStatut() == StatutInscriptionEnum.EN_COURS).count();
        long coursTermines = mesCours.stream()
                .filter(i -> i.getStatut() == StatutInscriptionEnum.TERMINE).count();

        double progressionMoyenne = mesCours.stream()
                .mapToDouble(i -> progressionService.getProgressionPourcentageCours(
                        apprenant.getId(), i.getCours().getId()))
                .average().orElse(0.0);

        model.addAttribute("apprenant", apprenant);
        model.addAttribute("totalCours", mesCours.size());
        model.addAttribute("coursEnCours", coursEnCours);
        model.addAttribute("coursTermines", coursTermines);
        model.addAttribute("progressionMoyenne", Math.round(progressionMoyenne));

        // Recent courses with progression
        List<Map<String, Object>> recentCours = new ArrayList<>();
        for (var inscription : mesCours.stream().limit(4).toList()) {
            Map<String, Object> data = new HashMap<>();
            data.put("cours", inscription.getCours());
            data.put("progression", progressionService.getProgressionPourcentageCours(
                    apprenant.getId(), inscription.getCours().getId()));
            data.put("statut", inscription.getStatut());
            recentCours.add(data);
        }
        model.addAttribute("recentCours", recentCours);

        return "apprenant/dashboard";
    }

    @GetMapping("/catalogue")
    public String catalogue(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String niveau,
                             @RequestParam(defaultValue = "0") int page,
                             Principal principal, Model model) {
        User apprenant = getCurrentUser(principal);
        var pageable = PageRequest.of(page, 12, Sort.by(Sort.Direction.DESC, "dateCreation"));

        var cours = (search != null && !search.isEmpty())
                ? coursService.searchCoursByTitre(search, pageable)
                : (niveau != null && !niveau.isEmpty())
                    ? coursService.filterByNiveau(NiveauEnum.valueOf(niveau), pageable)
                    : coursService.getAllCoursPublics(pageable);

        model.addAttribute("coursList", cours.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", cours.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("niveau", niveau);
        model.addAttribute("niveaux", NiveauEnum.values());
        model.addAttribute("apprenantId", apprenant.getId());
        return "apprenant/catalogue";
    }

    @GetMapping("/cours/{id}")
    public String coursDetail(@PathVariable Long id, Principal principal, Model model) {
        User apprenant = getCurrentUser(principal);
        var cours = coursService.getCoursById(id)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        var modules = moduleService.getModulesDuCours(id);
        List<Map<String, Object>> modulesData = new ArrayList<>();
        for (var module : modules) {
            Map<String, Object> data = new HashMap<>();
            data.put("module", module);
            data.put("lecons", leconService.getLeconsDuModule(module.getId()));
            data.put("quizzes", quizService.getQuizzesParModule(module.getId()));
            modulesData.add(data);
        }

        var inscription = inscriptionService.getInscription(apprenant.getId(), id);
        boolean estInscrit = inscription.isPresent();
        Double progression = estInscrit
                ? progressionService.getProgressionPourcentageCours(apprenant.getId(), id) : 0.0;

        model.addAttribute("cours", cours);
        model.addAttribute("modulesData", modulesData);
        model.addAttribute("estInscrit", estInscrit);
        model.addAttribute("progression", progression);
        model.addAttribute("apprenantId", apprenant.getId());
        return "apprenant/cours-detail";
    }

    @PostMapping("/cours/{id}/inscrire")
    public String inscrire(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User apprenant = getCurrentUser(principal);
            var cours = coursService.getCoursById(id)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
            inscriptionService.inscrireApprenant(apprenant, cours);
            redirectAttributes.addFlashAttribute("message", "Inscription réussie !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/apprenant/cours/" + id;
    }

    @GetMapping("/mes-cours")
    public String mesCours(Principal principal, Model model) {
        User apprenant = getCurrentUser(principal);
        var inscriptions = inscriptionService.getMesCours(apprenant.getId());

        List<Map<String, Object>> coursData = new ArrayList<>();
        for (var inscription : inscriptions) {
            Map<String, Object> data = new HashMap<>();
            data.put("cours", inscription.getCours());
            data.put("inscription", inscription);
            data.put("progression", progressionService.getProgressionPourcentageCours(
                    apprenant.getId(), inscription.getCours().getId()));
            coursData.add(data);
        }

        model.addAttribute("coursData", coursData);
        return "apprenant/mes-cours";
    }

    @GetMapping("/lecon/{id}")
    public String lecon(@PathVariable Long id, Principal principal, Model model) {
        User apprenant = getCurrentUser(principal);
        var lecon = leconService.getLeconById(id)
                .orElseThrow(() -> new RuntimeException("Leçon non trouvée"));

        var progression = progressionService.getProgressionLecon(apprenant.getId(), id);
        boolean completee = progression.isPresent() && progression.get().getCompletee();

        // Get sibling lecons for navigation
        var siblingLecons = leconService.getLeconsDuModule(lecon.getModule().getId());

        model.addAttribute("lecon", lecon);
        model.addAttribute("completee", completee);
        model.addAttribute("siblingLecons", siblingLecons);
        model.addAttribute("module", lecon.getModule());
        model.addAttribute("cours", lecon.getModule().getCours());
        return "apprenant/lecon";
    }

    @PostMapping("/lecon/{id}/complete")
    public String completeLecon(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User apprenant = getCurrentUser(principal);
            var lecon = leconService.getLeconById(id)
                    .orElseThrow(() -> new RuntimeException("Leçon non trouvée"));
            progressionService.marquerLeconCompletee(apprenant, lecon);
            redirectAttributes.addFlashAttribute("message", "Leçon marquée comme terminée !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/apprenant/lecon/" + id;
    }

    @GetMapping("/quiz/{id}")
    public String quiz(@PathVariable Long id, Principal principal, Model model) {
        User apprenant = getCurrentUser(principal);
        var quiz = quizService.getQuizById(id)
                .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));

        var questions = quizService.getQuestionsDuQuiz(id);
        List<Map<String, Object>> questionsData = new ArrayList<>();
        for (var question : questions) {
            Map<String, Object> data = new HashMap<>();
            data.put("question", question);
            data.put("choix", quizService.getChoixDeLaQuestion(question.getId()));
            questionsData.add(data);
        }

        // Check if already answered
        var existingReponses = reponseService.getReponsesApprenant(apprenant.getId(), id);
        boolean dejaRepondu = !existingReponses.isEmpty();

        model.addAttribute("quiz", quiz);
        model.addAttribute("questionsData", questionsData);
        model.addAttribute("dejaRepondu", dejaRepondu);
        model.addAttribute("apprenantId", apprenant.getId());

        if (dejaRepondu) {
            Integer score = reponseService.calculerScoreQuiz(apprenant.getId(), id);
            Long bonnes = reponseService.getNbReponsesCorrectes(apprenant.getId(), id);
            Long total = reponseService.getNbReponsesTotal(apprenant.getId(), id);
            model.addAttribute("score", score);
            model.addAttribute("bonnesReponses", bonnes);
            model.addAttribute("totalReponses", total);
        }

        return "apprenant/quiz";
    }

    @PostMapping("/quiz/{id}/soumettre")
    public String soumettreQuiz(@PathVariable Long id,
                                 @RequestParam Map<String, String> allParams,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            User apprenant = getCurrentUser(principal);
            var quiz = quizService.getQuizById(id)
                    .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));
            var questions = quizService.getQuestionsDuQuiz(id);

            for (var question : questions) {
                String choixIdStr = allParams.get("question_" + question.getId());
                if (choixIdStr != null && !choixIdStr.isEmpty()) {
                    Long choixId = Long.parseLong(choixIdStr);
                    reponseService.soumettreReponse(apprenant, quiz, question, choixId);
                } else {
                    reponseService.soumettreReponseNulle(apprenant, quiz, question);
                }
            }

            Integer score = reponseService.calculerScoreQuiz(apprenant.getId(), id);
            redirectAttributes.addFlashAttribute("message",
                    "Quiz soumis ! Votre score : " + score + "%");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/apprenant/quiz/" + id;
    }

    @GetMapping("/resultats")
    public String resultats(Principal principal, Model model) {
        User apprenant = getCurrentUser(principal);
        var toutesReponses = reponseService.getToutesReponsesApprenant(apprenant.getId());

        Map<Long, Map<String, Object>> resultatsMap = new HashMap<>();
        for (var reponse : toutesReponses) {
            Long quizId = reponse.getQuiz().getId();
            if (!resultatsMap.containsKey(quizId)) {
                Map<String, Object> data = new HashMap<>();
                data.put("quiz", reponse.getQuiz());
                data.put("score", reponseService.calculerScoreQuiz(apprenant.getId(), quizId));
                data.put("bonnesReponses", reponseService.getNbReponsesCorrectes(apprenant.getId(), quizId));
                data.put("totalQuestions", reponseService.getNbReponsesTotal(apprenant.getId(), quizId));
                data.put("reussi", reponseService.calculerScoreQuiz(apprenant.getId(), quizId) >= 60);
                resultatsMap.put(quizId, data);
            }
        }

        model.addAttribute("resultats", new ArrayList<>(resultatsMap.values()));
        return "apprenant/resultats";
    }
}
