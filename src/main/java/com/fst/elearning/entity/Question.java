package com.fst.elearning.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String enonce;

    @Column(nullable = false)
    private Integer ordre;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // Relations OneToMany
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChoixQuestion> choix;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReponseApprenant> reponses;

    // Constructeurs
    public Question() {
    }

    public Question(String enonce, Integer ordre, Quiz quiz) {
        this.enonce = enonce;
        this.ordre = ordre;
        this.quiz = quiz;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public String getEnonce() {
        return enonce;
    }

    public void setEnonce(String enonce) {
        this.enonce = enonce;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public List<ChoixQuestion> getChoix() {
        return choix;
    }

    public void setChoix(List<ChoixQuestion> choix) {
        this.choix = choix;
    }

    public List<ReponseApprenant> getReponses() {
        return reponses;
    }

    public void setReponses(List<ReponseApprenant> reponses) {
        this.reponses = reponses;
    }
}
