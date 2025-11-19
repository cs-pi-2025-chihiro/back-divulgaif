package br.com.divulgaifback.modules.works.controllers;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import br.com.divulgaifback.common.controllers.BaseController;
import br.com.divulgaifback.modules.works.entities.Label;
import br.com.divulgaifback.modules.works.entities.QLabel;
import br.com.divulgaifback.modules.works.useCases.label.create.CreateLabelRequest;
import br.com.divulgaifback.modules.works.useCases.label.create.CreateLabelResponse;
import br.com.divulgaifback.modules.works.useCases.label.create.CreateLabelUseCase;
import br.com.divulgaifback.modules.works.useCases.label.delete.DeleteLabelUseCase;
import br.com.divulgaifback.modules.works.useCases.label.list.ListLabelsResponse;
import br.com.divulgaifback.modules.works.useCases.label.list.ListLabelsUseCase;
import br.com.divulgaifback.modules.works.useCases.label.update.UpdateLabelRequest;
import br.com.divulgaifback.modules.works.useCases.label.update.UpdateLabelUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/labels")
public class LabelController extends BaseController {
    private final ListLabelsUseCase listLabelsUseCase;
    private final CreateLabelUseCase createLabelUseCase;
    private final UpdateLabelUseCase updateLabelUseCase;
    private final DeleteLabelUseCase deleteLabelUseCase;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateLabelResponse create(@Valid @RequestBody CreateLabelRequest request) {
        return createLabelUseCase.execute(request);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public Page<ListLabelsResponse> list(
            @RequestParam Map<String, String> params,
            @QuerydslPredicate(root = Label.class) Predicate basePredicate,
            Pageable pagination
    ) {
        BooleanBuilder operatorPredicate = buildOperatorPredicate(params, QLabel.label);
        return listLabelsUseCase.execute(operatorPredicate, basePredicate, pagination);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @Valid @RequestBody UpdateLabelRequest request) {
        updateLabelUseCase.execute(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteLabelUseCase.execute(id);
    }
}