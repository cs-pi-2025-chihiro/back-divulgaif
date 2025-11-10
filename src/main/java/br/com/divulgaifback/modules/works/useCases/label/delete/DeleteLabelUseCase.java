package br.com.divulgaifback.modules.works.useCases.label.delete;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.works.entities.Label;
import br.com.divulgaifback.modules.works.repositories.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteLabelUseCase {

    private final LabelRepository labelRepository;

    @Transactional
    @Secured({"IS_ADMIN", "IS_TEACHER"})
    public void execute(Long id) {
        Label label = labelRepository.findById(id.intValue())
                .orElseThrow(() -> new NotFoundException("Label not found"));
        labelRepository.delete(label);
    }
}