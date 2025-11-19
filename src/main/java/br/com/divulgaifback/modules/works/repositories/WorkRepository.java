package br.com.divulgaifback.modules.works.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.works.entities.Work;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkRepository extends BaseRepository<Work, Integer> {

    @Query("SELECT COUNT(w) FROM Work w " +
            "WHERE w.workStatus.id = :workStatusId " +
            "AND (:startDate IS NULL OR w.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR w.createdAt <= :endDate)")
    long countByWorkStatusIdFiltered(@Param("workStatusId") Integer workStatusId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(w) FROM Work w " +
            "JOIN w.labels l " +
            "JOIN w.workStatus ws " +
            "WHERE ws.name = :statusName " +
            "AND l.id = :labelId " +
            "AND (:startDate IS NULL OR w.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR w.createdAt <= :endDate)")
    long countByLabelIdFiltered(@Param("labelId") Integer labelId,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                @Param("statusName") String statusName);

    @Query("SELECT COUNT(w) FROM Work w JOIN w.authors a " +
            "JOIN w.workStatus ws " +
            "WHERE ws.name = :statusName " +
            "AND a.id = :authorId " +
            "AND (:startDate IS NULL OR w.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR w.createdAt <= :endDate)")
    long countByAuthorIdFiltered(@Param("authorId") Integer authorId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate,
                                 @Param("statusName") String statusName);

    @Query("SELECT COUNT(w) as total, s.name as name " +
            "FROM Work w " +
            "JOIN w.workStatus s " +
            "WHERE (:startDate IS NULL OR w.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR w.createdAt <= :endDate) " +
            "GROUP BY s.id, s.name")
    List<WorksByStatusProjection> getCountsByStatusGrouped(@Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    interface WorksByStatusProjection {
        Long getTotal();
        String getName();
    }

    @Query("SELECT COUNT(w) as total, l.name as name " +
            "FROM Work w " +
            "JOIN w.labels l " +
            "JOIN w.workStatus ws " +
            "WHERE ws.name = :statusName " +
            "AND (:startDate IS NULL OR w.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR w.createdAt <= :endDate) " +
            "GROUP BY l.id, l.name " +
            "ORDER BY COUNT(w) DESC")
    List<WorksByLabelProjection> getCountsByLabelGrouped(@Param("statusName") String statusName,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate,
                                                         Pageable pageable);

    interface WorksByLabelProjection {
        Long getTotal();
        String getName();
    }

    @Query("SELECT COUNT(w) as total, a.name as name " +
            "FROM Work w " +
            "JOIN w.authors a " +
            "JOIN w.workStatus ws " +
            "WHERE ws.name = :statusName " +
            "AND (:startDate IS NULL OR w.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR w.createdAt <= :endDate) " +
            "GROUP BY a.id, a.name " +
            "ORDER BY COUNT(w) DESC")
    List<WorksByAuthorProjection> getCountsByAuthorGrouped(@Param("statusName") String statusName,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate,
                                                           Pageable pageable);

    interface WorksByAuthorProjection {
        Long getTotal();
        String getEmail();
        String getName();
    }
}