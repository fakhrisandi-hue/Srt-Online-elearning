package com.fst.elearning.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reponses_apprenants")
public class ReponseApprenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "apprenant_id", nullable = false)
    private User apprenant;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "choix_id")
    private ChoixQuestion choixSelectionne;

    @Column(nullable = false)
    private Boolean correcte = false;

    @Column(nullable = false)
    private LocalDateTime dateReponse = LocalDateTime.now();

    // Constructeurs
    public ReponseApprenant() {
    }

    public ReponseApprenant(User apprenant, Quiz quiz, Question question, ChoixQuestion choixSelectionne) {
        this.apprenant = apprenant;
        this.quiz = quiz;
        this.question = question;
        this.choixSelectionne = choixSelectionne;
        if (choixSelectionne != null) {
            this.correcte = choixSelectionne.getCorrect();
        }
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public User getApprenant() {
        return apprenant;
    }

    public void setApprenant(User apprenant) {
        this.apprenant = apprenant;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public ChoixQuestion getChoixSelectionne() {
        return choixSelectionne;
    }

    public void setChoixSelectionne(ChoixQuestion choixSelectionne) {
        this.choixSelectionne = choixSelectionne;
        if (choixSelectionne != null) {
            this.correcte = choixSelectionne.getCorrect();
        }
    }

    public Boolean getCorrecte() {
        return correcte;
    }

    public void setCorrecte(Boolean correcte) {
        this.correcte = correcte;
    }

    public LocalDateTime getDateReponse() {
        return dateReponse;
    }

    public void setDateReponse(LocalDateTime dateReponse) {
        this.dateReponse = dateReponse;
    }
}
