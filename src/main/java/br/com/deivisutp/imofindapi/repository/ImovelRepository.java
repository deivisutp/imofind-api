package br.com.deivisutp.imofindapi.repository;

import br.com.deivisutp.imofindapi.entities.Imovel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {
    Optional<Imovel> findByTitulo(String titulo);

}
