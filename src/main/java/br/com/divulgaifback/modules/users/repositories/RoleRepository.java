package br.com.divulgaifback.modules.users.repositories;

import java.util.Optional;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.users.entities.Role;

public interface RoleRepository extends BaseRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
