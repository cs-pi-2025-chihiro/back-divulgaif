package br.com.divulgaifback.modules.works.controllers;

import br.com.divulgaifback.common.controllers.BaseController;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.entities.QAuthor;
import br.com.divulgaifback.modules.works.useCases.author.create.CreateAuthorRequest;
import br.com.divulgaifback.modules.works.useCases.author.create.CreateAuthorResponse;
import br.com.divulgaifback.modules.works.useCases.author.create.CreateAuthorUseCase;
import br.com.divulgaifback.modules.works.useCases.author.delete.DeleteAuthorUseCase;
import br.com.divulgaifback.modules.works.useCases.author.list.ListAuthorsResponse;
import br.com.divulgaifback.modules.works.useCases.author.list.ListAuthorsUseCase;
import br.com.divulgaifback.modules.works.useCases.author.update.UpdateAuthorRequest;
import br.com.divulgaifback.modules.works.useCases.author.update.UpdateAuthorResponse;
import br.com.divulgaifback.modules.works.useCases.author.update.UpdateAuthorUseCase;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
    private final ListAuthorsUseCase listAuthorsUseCase;
    private final CreateAuthorUseCase createAuthorUseCase;
    private final UpdateAuthorUseCase updateAuthorUseCase;
    private final DeleteAuthorUseCase deleteAuthorUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateAuthorResponse create(@Valid @RequestBody CreateAuthorRequest request) {
        return createAuthorUseCase.execute(request);
    }

    @PutMapping("/{authorId}")
    @ResponseStatus(HttpStatus.OK)
    public UpdateAuthorResponse update(
            @PathVariable @Positive Integer authorId,
            @Valid @RequestBody UpdateAuthorRequest request
    ) {
        return updateAuthorUseCase.execute(authorId, request);
    }

    @DeleteMapping("/{authorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Integer authorId) {
        deleteAuthorUseCase.execute(authorId);
    }

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