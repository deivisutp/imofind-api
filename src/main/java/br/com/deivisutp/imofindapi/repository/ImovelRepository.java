package br.com.deivisutp.imofindapi.repository;

import br.com.deivisutp.imofindapi.dto.NeighborhoodAggregate;
import br.com.deivisutp.imofindapi.entities.Imovel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface ImovelRepository extends JpaRepository<Imovel, Long> {
    List<Imovel> findByTitulo(String titulo);
    List<Imovel> findByTituloContaining(String titulo);

    Optional<Imovel> findBySourceListingId(Long sourceListingId);

    @Query("SELECT im FROM Imovel im WHERE LOWER(im.titulo) like %:titulo%")
    List<Imovel> searchImoveisLikeTitulo(@Param("titulo") String titulo);

    /** Agregados de ofertas ativas por bairro (estoque, mediana e faixa de preco). */
    @Query(value = """
            SELECT city AS "city", neighborhood AS "neighborhood",
                   COUNT(*) AS "count",
                   CAST(percentile_cont(0.5) WITHIN GROUP (ORDER BY price) AS numeric(15,2)) AS "medianPrice",
                   MIN(price) AS "minPrice",
                   MAX(price) AS "maxPrice",
                   CAST(ROUND(AVG(price), 2) AS numeric(15,2)) AS "avgPrice",
                   CAST(percentile_cont(0.5) WITHIN GROUP (ORDER BY price / NULLIF(area, 0)) AS numeric(15,2)) AS "medianPricePerSqm",
                   COUNT(*) FILTER (WHERE area > 0) AS "sampleWithArea"
            FROM imovel
            WHERE active = true AND price IS NOT NULL
              AND (CAST(:city AS text) IS NULL OR LOWER(city) = LOWER(CAST(:city AS text)))
              AND (CAST(:type AS text) IS NULL OR LOWER(type) = LOWER(CAST(:type AS text)))
            GROUP BY city, neighborhood
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<NeighborhoodAggregate> aggregateByNeighborhood(@Param("city") String city, @Param("type") String type);

    @Query("SELECT DISTINCT im.origem FROM Imovel im WHERE im.active = true AND im.origem IS NOT NULL ORDER BY im.origem")
    List<String> findDistinctSources();

    @Query("SELECT DISTINCT im.type FROM Imovel im WHERE im.active = true AND im.type IS NOT NULL AND im.type <> '' ORDER BY im.type")
    List<String> findDistinctTypes();
}

