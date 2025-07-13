package br.com.divulgaifback.modules.works.controllers;

import br.com.divulgaifback.common.controllers.BaseController;
import br.com.divulgaifback.modules.works.entities.Label;
import br.com.divulgaifback.modules.works.entities.QLabel;
import br.com.divulgaifback.modules.works.entities.QWork;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.useCases.label.create.CreateLabelRequest;
import br.com.divulgaifback.modules.works.useCases.label.create.CreateLabelResponse;
import br.com.divulgaifback.modules.works.useCases.label.create.CreateLabelUseCase;
import br.com.divulgaifback.modules.works.useCases.label.list.ListLabelsResponse;
import br.com.divulgaifback.modules.works.useCases.label.list.ListLabelsUseCase;
import br.com.divulgaifback.modules.works.useCases.work.list.ListWorksUseCase;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/labels")
public class LabelController extends BaseController {
    private ListLabelsUseCase listLabelsUseCase;
    private CreateLabelUseCase createLabelUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateLabelResponse create(CreateLabelRequest request) {
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
}
