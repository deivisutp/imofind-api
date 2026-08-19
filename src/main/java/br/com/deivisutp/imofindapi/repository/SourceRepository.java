package br.com.deivisutp.imofindapi.repository;

import br.com.deivisutp.imofindapi.entities.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SourceRepository extends JpaRepository<Source, Long> {
    Optional<Source> findByCode(String code);
}
