package br.com.deivisutp.imofindapi.repository;

import br.com.deivisutp.imofindapi.entities.Source;
import br.com.deivisutp.imofindapi.entities.SourceListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SourceListingRepository extends JpaRepository<SourceListing, Long> {
    Optional<SourceListing> findBySourceAndExternalId(Source source, String externalId);

    List<SourceListing> findBySourceAndActiveTrue(Source source);
}
