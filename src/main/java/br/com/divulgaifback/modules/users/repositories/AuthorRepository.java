package br.com.divulgaifback.modules.users.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.users.entities.Author;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AuthorRepository extends BaseRepository<Author, Integer> {
    Optional<Author> findByEmail(String email);

    @Modifying
    @Query("UPDATE Author a SET a.user = :userId WHERE a.id = :id")
    void updateAuthorsUserId(int id, int userId);
}
