package br.com.divulgaifback.modules.users.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.users.entities.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRa(String ra);
    Optional<User> findByCpf(String cpf);
    Optional<User> findByForgotPasswordToken(String token);
}