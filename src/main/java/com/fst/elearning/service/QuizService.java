package com.fst.elearning.service;

import com.fst.elearning.repository.ChoixQuestionRepository;
import com.fst.elearning.repository.QuestionRepository;
import com.fst.elearning.repository.QuizRepository;
import com.fst.elearning.entity.ChoixQuestion;
import com.fst.elearning.entity.Question;
import com.fst.elearning.entity.Quiz;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ChoixQuestionRepository choixRepository;

    public QuizService(QuizRepository quizRepository,
            QuestionRepository questionRepository,
            ChoixQuestionRepository choixRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.choixRepository = choixRepository;
    }

    // ===== QUIZ =====
    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    public Optional<Quiz> getQuizById(Long id) {
        return quizRepository.findById(id);
    }

    public List<Quiz> getQuizzesParModule(Long moduleId) {
        return quizRepository.findByModuleId(moduleId);
    }

    public Quiz updateQuiz(Long id, Quiz quizDetails) {
        return quizRepository.findById(id).map(quiz -> {
            quiz.setTitre(quizDetails.getTitre());
            quiz.setDescription(quizDetails.getDescription());
            quiz.setDureeMin(quizDetails.getDureeMin());
            quiz.setPublique(quizDetails.getPublique());
            return quizRepository.save(quiz);
        }).orElseThrow(() -> new RuntimeException("Quiz non trouvé"));
    }

    public void deleteQuiz(Long id) {
        quizRepository.deleteById(id);
    }

    // ===== QUESTIONS =====
    public Question createQuestion(Question question) {
        return questionRepository.save(question);
    }

    public List<Question> getQuestionsDuQuiz(Long quizId) {
        return questionRepository.findByQuizIdOrderByOrdre(quizId);
    }

    public Question updateQuestion(Long id, Question questionDetails) {
        return questionRepository.findById(id).map(question -> {
            question.setEnonce(questionDetails.getEnonce());
            question.setOrdre(questionDetails.getOrdre());
            return questionRepository.save(question);
        }).orElseThrow(() -> new RuntimeException("Question non trouvée"));
    }

    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    // ===== CHOIX =====
    public ChoixQuestion createChoix(ChoixQuestion choix) {
        return choixRepository.save(choix);
    }

    public List<ChoixQuestion> getChoixDeLaQuestion(Long questionId) {
        return choixRepository.findByQuestionIdOrderByOrdre(questionId);
    }

    public ChoixQuestion updateChoix(Long id, ChoixQuestion choixDetails) {
        return choixRepository.findById(id).map(choix -> {
            choix.setTexte(choixDetails.getTexte());
            choix.setCorrect(choixDetails.getCorrect());
            choix.setOrdre(choixDetails.getOrdre());
            return choixRepository.save(choix);
        }).orElseThrow(() -> new RuntimeException("Choix non trouvé"));
    }

    public void deleteChoix(Long id) {
        choixRepository.deleteById(id);
    }

    // ===== LOGIQUE DE QUIZ =====
    public Integer getTotalQuestions(Long quizId) {
        return getQuestionsDuQuiz(quizId).size();
    }

    public ChoixQuestion getBonneReponse(Long questionId) {
        List<ChoixQuestion> choix = getChoixDeLaQuestion(questionId);
        return choix.stream()
                .filter(ChoixQuestion::getCorrect)
                .findFirst()
                .orElse(null);
    }

    public Boolean verifierReponse(Long questionId, Long choixId) {
        Optional<ChoixQuestion> choix = choixRepository.findById(choixId);
        if (choix.isEmpty()) {
            return false;
        }
        return choix.get().getCorrect();
    }
}
