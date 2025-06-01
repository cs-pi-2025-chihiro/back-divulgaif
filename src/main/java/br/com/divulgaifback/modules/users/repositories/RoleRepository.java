package br.com.divulgaifback.modules.users.repositories;

import br.com.divulgaifback.common.repositories.BaseRepository;
import br.com.divulgaifback.modules.users.entities.Role;

import java.util.Optional;

public interface RoleRepository extends BaseRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
