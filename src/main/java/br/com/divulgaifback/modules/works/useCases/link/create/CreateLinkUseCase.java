package br.com.divulgaifback.modules.works.useCases.link.create;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.works.entities.Link;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.repositories.LinkRepository;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateLinkUseCase {
    private final LinkRepository linkRepository;
    private final WorkRepository workRepository;
    private final CreateLinkResponse createLinkResponse;

    @Transactional
    public CreateLinkResponse execute(CreateLinkRequest request) {
        Link link = CreateLinkRequest.toDomain(request);
        Work work = workRepository.findById(request.workId()).orElseThrow(() -> NotFoundException.with(Work.class, "id", request.workId()));
        link.getWorksAssociated().add(work);
        Link newLink = linkRepository.save(link);
        return createLinkResponse.toPresentation(newLink);
    }
}
