package br.com.divulgaifback.modules.works.controllers;

import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkRequest;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkResponse;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkUseCase;
import br.com.divulgaifback.modules.works.useCases.work.list.ListWorksResponse;
import br.com.divulgaifback.modules.works.useCases.work.list.ListWorksUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/works")
public class WorkController {
    private final CreateWorkUseCase createWorkUseCase;
    private final ListWorksUseCase listWorksUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateWorkResponse create(@Valid @RequestBody CreateWorkRequest request) {
        return this.createWorkUseCase.execute(request);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public Page<ListWorksResponse> list(@QuerydslPredicate(root = Work.class) Predicate predicate, Pageable pagination) {
        return listWorksUseCase.execute(predicate, pagination);
    }
}
