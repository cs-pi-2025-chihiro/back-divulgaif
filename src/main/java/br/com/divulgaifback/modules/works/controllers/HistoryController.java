package br.com.divulgaifback.modules.works.controllers;

import br.com.divulgaifback.modules.works.useCases.history.create.CreateHistoryRequest;
import br.com.divulgaifback.modules.works.useCases.history.create.CreateHistoryResponse;
import br.com.divulgaifback.modules.works.useCases.history.create.CreateHistoryUseCase;
import br.com.divulgaifback.modules.works.useCases.label.create.CreateLabelRequest;
import br.com.divulgaifback.modules.works.useCases.label.create.CreateLabelResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/history")
public class HistoryController {
    public final CreateHistoryUseCase createHistoryUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateHistoryResponse create(@Valid @RequestBody CreateHistoryRequest request) {
        return createHistoryUseCase.execute(request);
    }
}
