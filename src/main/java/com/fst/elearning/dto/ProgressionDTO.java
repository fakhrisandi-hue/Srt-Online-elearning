package com.fst.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressionDTO {
    private Long idInscription;
    private Long idModule;
    private Long idLecon;
    private boolean estTermine;
    private double pourcentageProgress;
}
