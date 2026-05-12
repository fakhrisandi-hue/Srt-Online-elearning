package com.fst.elearning.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "choix_questions")
public class ChoixQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String texte;

    @Column(nullable = false)
    private Boolean correct = false;

    @Column(nullable = false)
    private Integer ordre;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // Constructeurs
    public ChoixQuestion() {
    }

    public ChoixQuestion(String texte, Boolean correct, Integer ordre, Question question) {
        this.texte = texte;
        this.correct = correct;
        this.ordre = ordre;
        this.question = question;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
