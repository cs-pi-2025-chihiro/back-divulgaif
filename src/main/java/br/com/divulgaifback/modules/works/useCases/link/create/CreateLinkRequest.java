package br.com.divulgaifback.modules.works.useCases.link.create;

import br.com.divulgaifback.modules.works.entities.Link;
import jakarta.validation.constraints.NotNull;

public record CreateLinkRequest(
    String name,
    @NotNull(message = "{createlink.url.required}") String url,
    String description,
    @NotNull(message = "{createlink.url.required}") Integer workId
) {
    public static Link toDomain(CreateLinkRequest request) {
        Link link = new Link();
        link.setName(request.name);
        link.setUrl(request.url);
        link.setDescription(request.description);
        return link;
    }
}
