package br.com.divulgaifback.modules.works.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.works.entities.Work;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkRepository extends BaseRepository<Work, Integer> {
    Page<Work> findAll(Predicate predicate, Pageable pageable);
}
