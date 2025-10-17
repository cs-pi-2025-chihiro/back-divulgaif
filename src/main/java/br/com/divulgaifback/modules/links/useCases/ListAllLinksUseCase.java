package br.com.divulgaifback.modules.links.useCases;

import br.com.divulgaifback.modules.links.entities.Link;
import br.com.divulgaifback.modules.links.repositories.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListAllLinksUseCase {

    @Autowired
    private LinkRepository linkRepository;

    public List<Link> execute() {
        return linkRepository.findAll();
    }
}