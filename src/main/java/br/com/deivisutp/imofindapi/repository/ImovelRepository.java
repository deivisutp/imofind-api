package br.com.deivisutp.imofindapi.repository;

import br.com.deivisutp.imofindapi.entities.Imovel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {
    List<Imovel> findByTitulo(String titulo);
    List<Imovel> findByTituloContaining(String titulo);

    @Query("SELECT im FROM Imovel im WHERE LOWER(im.titulo) like %:titulo%")
    List<Imovel> searchImoveisLikeTitulo(@Param("titulo") String titulo);
}
