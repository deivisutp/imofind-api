package br.com.deivisutp.imofindapi.repository;

import br.com.deivisutp.imofindapi.entities.ListingObservation;
import br.com.deivisutp.imofindapi.entities.SourceListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListingObservationRepository extends JpaRepository<ListingObservation, Long> {
    Optional<ListingObservation> findTopBySourceListingOrderByIdDesc(SourceListing sourceListing);
}
