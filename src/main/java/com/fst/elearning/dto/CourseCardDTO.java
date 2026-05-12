package com.fst.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseCardDTO {
    private Long id;
    private String titre;
    private String description;
    private String imageUrl;
    private String formateurNom;
}
