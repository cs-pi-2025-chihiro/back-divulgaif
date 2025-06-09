package br.com.divulgaifback.modules.works.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.works.entities.WorkType;

import java.util.Optional;

public interface WorkTypeRepository extends BaseRepository<WorkType, Integer> {
    Optional<WorkType> findByName(String name);
}
