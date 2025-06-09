package br.com.divulgaifback.modules.works.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.works.entities.WorkStatus;

import java.util.Optional;

public interface WorkStatusRepository extends BaseRepository<WorkStatus, Integer> {
    Optional<WorkStatus> findByName(String name);
}
