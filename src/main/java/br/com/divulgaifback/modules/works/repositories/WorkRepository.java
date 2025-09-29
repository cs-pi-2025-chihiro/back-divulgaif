package br.com.divulgaifback.modules.works.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.works.entities.Work;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface WorkRepository extends BaseRepository<Work, Integer> {
    Page<Work> findAll(Predicate predicate, Pageable pageable);

    @Query("SELECT w FROM Work w " +
            "JOIN w.workStatus ws " +
            "JOIN w.authors a " +
            "JOIN a.user u " +
            "WHERE ws.name IN :worksToList " +
            "AND u.id = :myId")
    Page<Work> findMyWorks(String[] worksToList, int myId, Pageable pageable);
}
