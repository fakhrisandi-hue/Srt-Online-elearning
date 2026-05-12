package com.fst.elearning.service;

import com.fst.elearning.repository.LeconRepository;
import com.fst.elearning.entity.Lecon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LeconService {

    private final LeconRepository leconRepository;

    public LeconService(LeconRepository leconRepository) {
        this.leconRepository = leconRepository;
    }

    public Lecon createLecon(Lecon lecon) {
        return leconRepository.save(lecon);
    }

    public Optional<Lecon> getLeconById(Long id) {
        return leconRepository.findById(id);
    }

    public List<Lecon> getLeconsDuModule(Long moduleId) {
        return leconRepository.findByModuleIdOrderByOrdre(moduleId);
    }

    public Lecon updateLecon(Long id, Lecon leconDetails) {
        return leconRepository.findById(id).map(lecon -> {
            lecon.setTitre(leconDetails.getTitre());
            lecon.setContenu(leconDetails.getContenu());
            lecon.setOrdre(leconDetails.getOrdre());
            lecon.setDureeMin(leconDetails.getDureeMin());
            return leconRepository.save(lecon);
        }).orElseThrow(() -> new RuntimeException("Leçon non trouvée"));
    }

    public void deleteLecon(Long id) {
        leconRepository.deleteById(id);
    }
}
