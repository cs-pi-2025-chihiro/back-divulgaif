package br.com.divulgaifback.modules.works.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.works.entities.Label;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LabelRepository extends BaseRepository<Label, Integer> {
    Optional<Label> findByName(String name);

    @Query("SELECT COUNT(l) FROM Label l JOIN l.works w JOIN w.workStatus ws WHERE ws.name = :statusName")
    Long countAllLabelsByWorkStatus(String statusName);

    @Query("SELECT l FROM Label l " +
            "JOIN l.works w " +
            "JOIN w.workStatus ws " +
            "WHERE ws.name = :statusName " +
            "GROUP BY l.id " +
            "ORDER BY COUNT(w) DESC")
    Page<Label> findMostUsedLabel(Pageable pageable, String statusName);

    @Query("SELECT l FROM Label l " +
            "JOIN l.works w " +
            "JOIN w.workStatus ws " +
            "WHERE ws.name = :statusName " +
            "GROUP BY l.id " +
            "ORDER BY COUNT(w) ASC")
    Page<Label> findLeastUsedLabel(Pageable pageable, String statusName);
}
