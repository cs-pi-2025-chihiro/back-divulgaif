package br.com.divulgaifback.modules.works.controllers;

import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardRequest;
import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardResponse;
import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardUseCase;
import br.com.divulgaifback.modules.works.useCases.dashboard.getAuthors.GetAuthorsDashboardResponse;
import br.com.divulgaifback.modules.works.useCases.dashboard.getAuthors.GetAuthorsDashboardUseCase;
import br.com.divulgaifback.modules.works.useCases.dashboard.getLabels.GetLabelsDashboardResponse;
import br.com.divulgaifback.modules.works.useCases.dashboard.getLabels.GetLabelsDashboardUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboards")
public class DashboardController {
    private final GetDashboardUseCase getDashboardUseCase;
    private final GetAuthorsDashboardUseCase getAuthorsDashboardUseCase;
    private final GetLabelsDashboardUseCase getLabelsDashboardUseCase;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public GetDashboardResponse get(@RequestParam(required = false) GetDashboardRequest request) {
        return getDashboardUseCase.execute(request);
    }

    @GetMapping("/authors")
    @ResponseStatus(HttpStatus.OK)
    public GetAuthorsDashboardResponse getAuthors() {
        return getAuthorsDashboardUseCase.execute();
    }

    @GetMapping("/labels")
    @ResponseStatus(HttpStatus.OK)
    public GetLabelsDashboardResponse getLabels() {
        return getLabelsDashboardUseCase.execute();
    }
}
