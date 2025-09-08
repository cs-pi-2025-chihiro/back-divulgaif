package br.com.divulgaifback.modules.works.controllers;

import br.com.divulgaifback.common.controllers.BaseController;
import br.com.divulgaifback.modules.works.entities.QWork;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkRequest;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkResponse;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkUseCase;
import br.com.divulgaifback.modules.works.useCases.work.get.GetWorkResponse;
import br.com.divulgaifback.modules.works.useCases.work.get.GetWorkUseCase;
import br.com.divulgaifback.modules.works.useCases.work.list.ListWorksResponse;
import br.com.divulgaifback.modules.works.useCases.work.list.ListWorksUseCase;
import br.com.divulgaifback.modules.works.useCases.work.publish.PublishWorkUseCase;
import br.com.divulgaifback.modules.works.useCases.work.reject.RejectWorkUseCase;
import br.com.divulgaifback.modules.works.useCases.work.requestChanges.RequestChangesRequest;
import br.com.divulgaifback.modules.works.useCases.work.requestChanges.RequestChangesUseCase;
import br.com.divulgaifback.modules.works.useCases.work.update.UpdateWorkRequest;
import br.com.divulgaifback.modules.works.useCases.work.update.UpdateWorkResponse;
import br.com.divulgaifback.modules.works.useCases.work.update.UpdateWorkUseCase;
import br.com.divulgaifback.modules.works.useCases.work.listMine.ListMyWorksResponse;
import br.com.divulgaifback.modules.works.useCases.work.listMine.ListMyWorksUseCase;
import com.querydsl.core.BooleanBuilder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/works")
public class WorkController extends BaseController {
    private final CreateWorkUseCase createWorkUseCase;
    private final ListWorksUseCase listWorksUseCase;
    private final ListMyWorksUseCase listMyWorksUseCase;
    private final GetWorkUseCase getWorkUseCase;
    private final UpdateWorkUseCase updateWorkUseCase;
    private final PublishWorkUseCase publishWorkUseCase;
    private final RejectWorkUseCase rejectWorkUseCase;
    private final RequestChangesUseCase requestChangesUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateWorkResponse create(@Valid @RequestBody CreateWorkRequest request) {
        return this.createWorkUseCase.execute(request);
    }

    @PutMapping("/{workId}")
    @ResponseStatus(HttpStatus.OK)
    public UpdateWorkResponse update(@Valid @RequestBody UpdateWorkRequest request, @PathVariable @Positive Integer workId) {
        return this.updateWorkUseCase.execute(request, workId);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public Page<ListWorksResponse> list(
            @RequestParam Map<String, String> params,
            @QuerydslPredicate(root = Work.class) Predicate basePredicate,
            Pageable pagination) {
        BooleanBuilder operatorPredicate = buildOperatorPredicate(params, QWork.work);
        return listWorksUseCase.execute(operatorPredicate, basePredicate, pagination);
    }

    @GetMapping("/list-my-works")
    @ResponseStatus(HttpStatus.OK)
    public Page<ListMyWorksResponse> listMyWorks(
            @RequestParam Map<String, String> params,
            @QuerydslPredicate(root = Work.class) Predicate basePredicate,
            Pageable pagination) {
        BooleanBuilder operatorPredicate = buildOperatorPredicate(params, QWork.work);
        return listMyWorksUseCase.execute(operatorPredicate, basePredicate, pagination);
    }

    @GetMapping("/{workId}")
    @ResponseStatus(HttpStatus.OK)
    public GetWorkResponse get(@Valid @Positive @PathVariable Integer workId) {
        return getWorkUseCase.execute(workId);
    }

    @PutMapping("/publish/{workId}")
    @ResponseStatus(HttpStatus.OK)
    public void publish(@Valid @Positive @PathVariable Integer workId) {
        this.publishWorkUseCase.execute(workId);
    }

    @PutMapping("/reject/{workId}")
    @ResponseStatus(HttpStatus.OK)
    public void reject(@Valid @Positive @PathVariable Integer workId) {
        this.rejectWorkUseCase.execute(workId);
    }

    @PutMapping("/request-changes/{workId}")
    @ResponseStatus(HttpStatus.OK)
    public void requestChanges(@Valid @RequestBody RequestChangesRequest request, @Valid @Positive @PathVariable Integer workId) {
        this.requestChangesUseCase.execute(request, workId);
    }
}
