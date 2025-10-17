package br.com.divulgaifback.modules.works.useCases.label.update;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.works.entities.Label;
import br.com.divulgaifback.modules.works.repositories.LabelRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateLabelUseCase {

    private final LabelRepository labelRepository;

    @Transactional
    @Secured("IS_ADMIN")
    public void execute(Long id, UpdateLabelRequest request) {
        Label label = labelRepository.findById(id.intValue())
                .orElseThrow(() -> new NotFoundException("Label not found"));
        label.setName(request.name());
        labelRepository.save(label);
    }
}