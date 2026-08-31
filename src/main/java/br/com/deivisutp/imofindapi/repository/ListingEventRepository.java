package br.com.deivisutp.imofindapi.repository;

import br.com.deivisutp.imofindapi.dto.ReducaoPreco;
import br.com.deivisutp.imofindapi.dto.VariacaoResumo;
import br.com.deivisutp.imofindapi.entities.ListingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ListingEventRepository extends JpaRepository<ListingEvent, Long> {
    boolean existsByDedupKey(String dedupKey);

    /** Contagem de eventos por tipo nos ultimos :dias dias. */
    @Query(value = """
            SELECT
              COUNT(*) FILTER (WHERE event_type = 'NEW') AS "novos",
              COUNT(*) FILTER (WHERE event_type = 'PRICE_CHANGED' AND new_price < old_price) AS "reducoes",
              COUNT(*) FILTER (WHERE event_type = 'PRICE_CHANGED' AND new_price > old_price) AS "aumentos",
              COUNT(*) FILTER (WHERE event_type = 'REMOVED') AS "removidos",
              COUNT(*) FILTER (WHERE event_type = 'REACTIVATED') AS "reativados"
            FROM listing_event
            WHERE occurred_at >= now() - (:dias * interval '1 day')
            """, nativeQuery = true)
    VariacaoResumo resumoVariacoes(@Param("dias") int dias);

    /** Maiores reducoes de preco nos ultimos :dias dias, com dados do anuncio. */
    @Query(value = """
            SELECT i.titulo AS "titulo", i.neighborhood AS "neighborhood", i.city AS "city",
                   i.link AS "link", i.origem AS "origem",
                   e.old_price AS "oldPrice", e.new_price AS "newPrice"
            FROM listing_event e
            JOIN imovel i ON i.source_listing_id = e.source_listing_id
            WHERE e.event_type = 'PRICE_CHANGED' AND e.new_price < e.old_price
              AND e.occurred_at >= now() - (:dias * interval '1 day')
            ORDER BY (e.old_price - e.new_price) DESC
            LIMIT :limite
            """, nativeQuery = true)
    List<ReducaoPreco> reducoesRecentes(@Param("dias") int dias, @Param("limite") int limite);
}
