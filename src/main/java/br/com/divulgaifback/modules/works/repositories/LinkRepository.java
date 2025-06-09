package br.com.divulgaifback.modules.works.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.works.entities.Link;

import java.util.Optional;

public interface LinkRepository extends BaseRepository<Link, Integer> {
    Optional<Link> findByUrl(String url);
}
