package br.com.deivisutp.imofindapi.repository;

import br.com.deivisutp.imofindapi.entities.ListingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingEventRepository extends JpaRepository<ListingEvent, Long> {
    boolean existsByDedupKey(String dedupKey);
}
