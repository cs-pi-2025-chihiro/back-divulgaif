package br.com.divulgaifback.modules.users.repositories;

import java.util.Optional;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.users.entities.User;

public interface UserRepository extends BaseRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRa(String ra);
    Optional<User> findByCpf(String cpf);

    
}