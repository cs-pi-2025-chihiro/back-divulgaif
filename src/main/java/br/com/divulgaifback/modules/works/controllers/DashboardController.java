package br.com.divulgaifback.modules.works.controllers;

import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardRequest;
import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardResponse;
import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboards")
public class DashboardController {
    private final GetDashboardUseCase getDashboardUseCase;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public GetDashboardResponse get(@RequestParam(required = false) GetDashboardRequest request) {
        return getDashboardUseCase.execute(request);
    }
}
