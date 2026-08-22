package br.com.deivisutp.imofindapi.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "listing_event",
        uniqueConstraints = @UniqueConstraint(name = "uq_listing_event_dedup", columnNames = "dedup_key"))
@Getter
@Setter
@NoArgsConstructor
public class ListingEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_listing_id", nullable = false)
    private SourceListing sourceListing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crawl_run_id")
    private CrawlRun crawlRun;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private ListingEventType eventType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "old_price", precision = 15, scale = 2)
    private BigDecimal oldPrice;

    @Column(name = "new_price", precision = 15, scale = 2)
    private BigDecimal newPrice;

    @Column(name = "dedup_key", nullable = false, length = 200)
    private String dedupKey;
}
