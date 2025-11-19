package br.com.divulgaifback.modules.links.useCases;

import br.com.divulgaifback.modules.links.repositories.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteLinkUseCase {

    @Autowired
    private LinkRepository linkRepository;

    public void execute(Integer linkId) {
        linkRepository.deleteById(linkId);
    }
}