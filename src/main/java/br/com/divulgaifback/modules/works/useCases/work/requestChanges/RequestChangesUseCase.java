package br.com.divulgaifback.modules.works.useCases.work.requestChanges;

import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import br.com.divulgaifback.modules.works.repositories.WorkStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestChangesUseCase {
    private WorkRepository workRepository;
    private WorkStatusRepository workStatusRepository;

    @Transactional
    public void execute(RequestChangesRequest request, Integer workId) {

    }
}
