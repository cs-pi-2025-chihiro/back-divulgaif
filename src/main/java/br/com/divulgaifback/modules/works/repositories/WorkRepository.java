package br.com.divulgaifback.modules.works.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.works.entities.Work;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkRepository extends BaseRepository<Work, Integer> {
    long countAllByWorkStatusId(Integer workStatusId);

    long countAllByLabelsId(Integer labelId);

    long countAllByAuthorsId(Integer authorId);

    @Query("SELECT COUNT(w) as total, s.name as name " +
            "FROM Work w JOIN w.workStatus s " +
            "GROUP BY s.id, s.name")
    List<WorksByStatusProjection> getCountsByStatusGrouped();

    interface WorksByStatusProjection {
        Long getTotal();
        String getName();
    }

    @Query("SELECT COUNT(w) as total, l.name as name " +
            "FROM Work w " +
            "JOIN w.labels l " +
            "JOIN w.workStatus ws " +
            "WHERE ws.name = :statusName " +
            "GROUP BY l.id, l.name " +
            "ORDER BY COUNT(w) DESC")
    List<WorksByLabelProjection> getCountsByLabelGrouped(String statusName, Pageable pageable);

    interface WorksByLabelProjection {
        Long getTotal();
        String getName();
    }

    @Query("SELECT COUNT(w) as total, a.name as name " +
            "FROM Work w " +
            "JOIN w.authors a " +
            "JOIN w.workStatus ws " +
            "WHERE ws.name = :statusName " +
            "GROUP BY a.id, a.name " +
            "ORDER BY COUNT(w) DESC")
    List<WorksByAuthorProjection> getCountsByAuthorGrouped(String statusName, Pageable pageable);

    interface WorksByAuthorProjection {
        Long getTotal();
        String getEmail();
        String getName();
    }
}
