package com.fst.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDTO {
    private Long quizId;
    private Long userId;
    private int score;
    private int totalQuestions;
    private boolean isPassed;
}
