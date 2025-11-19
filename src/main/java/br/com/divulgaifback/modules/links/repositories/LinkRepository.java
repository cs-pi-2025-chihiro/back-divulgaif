package br.com.divulgaifback.modules.links.repositories;

import br.com.divulgaifback.modules.links.entities.Link;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkRepository extends JpaRepository<Link, Integer> {
}