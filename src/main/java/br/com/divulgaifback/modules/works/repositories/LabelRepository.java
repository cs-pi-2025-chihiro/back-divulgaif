package br.com.divulgaifback.modules.works.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.works.entities.Label;

import java.util.Optional;

public interface LabelRepository extends BaseRepository<Label, Integer> {
    Optional<Label> findByName(String name);
}
