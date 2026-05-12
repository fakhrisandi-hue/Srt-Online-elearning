package com.fst.elearning.service;

import com.fst.elearning.repository.ModuleRepository;
import com.fst.elearning.entity.Cours;
import com.fst.elearning.entity.Module;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    public Module createModule(Module module) {
        return moduleRepository.save(module);
    }

    public Optional<Module> getModuleById(Long id) {
        return moduleRepository.findById(id);
    }

    public List<Module> getModulesDuCours(Long coursId) {
        return moduleRepository.findByCoursIdOrderByOrdre(coursId);
    }

    public Module updateModule(Long id, Module moduleDetails) {
        return moduleRepository.findById(id).map(module -> {
            module.setTitre(moduleDetails.getTitre());
            module.setDescription(moduleDetails.getDescription());
            module.setOrdre(moduleDetails.getOrdre());
            return moduleRepository.save(module);
        }).orElseThrow(() -> new RuntimeException("Module non trouvé"));
    }

    public void deleteModule(Long id) {
        moduleRepository.deleteById(id);
    }
}
