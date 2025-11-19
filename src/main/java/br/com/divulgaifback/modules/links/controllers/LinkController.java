package br.com.divulgaifback.modules.links.controllers;

import br.com.divulgaifback.modules.links.dtos.CreateLinkDTO;
import br.com.divulgaifback.modules.links.dtos.UpdateLinkDTO;
import br.com.divulgaifback.modules.links.entities.Link;
import br.com.divulgaifback.modules.links.useCases.CreateLinkUseCase;
import br.com.divulgaifback.modules.links.useCases.DeleteLinkUseCase;
import br.com.divulgaifback.modules.links.useCases.ListAllLinksUseCase;
import br.com.divulgaifback.modules.links.useCases.UpdateLinkUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import br.com.divulgaifback.modules.auth.entities.AuthenticatedUser;

import java.util.List;

@RestController
@RequestMapping("/links")
@PreAuthorize("hasRole(\'PROFESSOR\')")
public class LinkController {

    @Autowired
    private CreateLinkUseCase createLinkUseCase;

    @Autowired
    private ListAllLinksUseCase listAllLinksUseCase;

    @Autowired
    private UpdateLinkUseCase updateLinkUseCase;

    @Autowired
    private DeleteLinkUseCase deleteLinkUseCase;

    @PostMapping
    public ResponseEntity<Link> create(@Valid @RequestBody CreateLinkDTO createLinkDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        createLinkDTO.setUserId(authenticatedUser.getId());
        Link newLink = createLinkUseCase.execute(createLinkDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newLink);
    }

    @GetMapping
    public ResponseEntity<List<Link>> listAll() {
        List<Link> links = listAllLinksUseCase.execute();
        return ResponseEntity.ok(links);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Link> update(@PathVariable Integer id, @Valid @RequestBody UpdateLinkDTO updateLinkDTO) {
        return updateLinkUseCase.execute(id, updateLinkDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        deleteLinkUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}