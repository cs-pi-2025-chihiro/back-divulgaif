package br.com.divulgaifback.modules.users.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends BaseRepository<Author, Integer> {
    Optional<Author> findByEmail(String email);
    List<Author> findAllByEmail(String email);

    @Query("SELECT a.user FROM Author a WHERE a.id = :id")
    Optional<User> findUserIdById(int id);

    @Modifying
    @Query("UPDATE Author a SET a.user = :userId WHERE a.id = :id")
    void updateAuthorsUserId(int id, int userId);

    @Query("SELECT COUNT(a.id) FROM Author a WHERE a.user IS NOT NULL")
    long countAllByUserExists();

    @Query("SELECT a FROM Author a GROUP BY a.id ORDER BY COUNT(a.user) DESC")
    Page<Author> findMostCited(Pageable pageable);
}
