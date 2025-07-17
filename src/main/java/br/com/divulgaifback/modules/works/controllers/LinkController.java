package br.com.divulgaifback.modules.works.controllers;

import br.com.divulgaifback.common.controllers.BaseController;
import br.com.divulgaifback.modules.works.entities.Link;
import br.com.divulgaifback.modules.works.entities.QLink;
import br.com.divulgaifback.modules.works.useCases.link.create.CreateLinkRequest;
import br.com.divulgaifback.modules.works.useCases.link.create.CreateLinkResponse;
import br.com.divulgaifback.modules.works.useCases.link.create.CreateLinkUseCase;
import br.com.divulgaifback.modules.works.useCases.link.list.ListLinksResponse;
import br.com.divulgaifback.modules.works.useCases.link.list.ListLinksUseCase;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/links")
public class LinkController extends BaseController {
    private CreateLinkUseCase createLinkUseCase;
    private ListLinksUseCase listLinksUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateLinkResponse create(@Valid @RequestBody CreateLinkRequest request) {
        return createLinkUseCase.execute(request);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public Page<ListLinksResponse> list(
            @RequestParam Map<String, String> params,
            @QuerydslPredicate(root = Link.class) Predicate basePredicate,
            Pageable pagination
    ) {
        BooleanBuilder operatorPredicate = buildOperatorPredicate(params, QLink.link);
        return listLinksUseCase.execute(operatorPredicate, basePredicate, pagination);
    }

}
