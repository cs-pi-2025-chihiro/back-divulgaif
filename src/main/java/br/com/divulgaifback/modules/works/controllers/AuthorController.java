package br.com.divulgaifback.modules.works.controllers;

import br.com.divulgaifback.common.controllers.BaseController;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.entities.QAuthor;
import br.com.divulgaifback.modules.works.useCases.author.list.ListAuthorsResponse;
import br.com.divulgaifback.modules.works.useCases.author.list.ListAuthorsUseCase;
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
@RequestMapping("/authors")
public class AuthorController extends BaseController {
    private ListAuthorsUseCase listAuthorsUseCase;

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public Page<ListAuthorsResponse> list(
            @RequestParam Map<String, String> params,
            @QuerydslPredicate(root = Author.class) Predicate basePredicate,
            Pageable pagination
    ) {
        BooleanBuilder operatorPredicate = buildOperatorPredicate(params, QAuthor.author);
        return listAuthorsUseCase.execute(operatorPredicate, basePredicate, pagination);
    }
}
