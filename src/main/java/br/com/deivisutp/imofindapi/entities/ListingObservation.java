package br.com.deivisutp.imofindapi.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "listing_observation")
@Getter
@Setter
@NoArgsConstructor
public class ListingObservation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_observation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_listing_id", nullable = false)
    private SourceListing sourceListing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crawl_run_id")
    private CrawlRun crawlRun;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @Column(name = "title", length = 4000)
    private String title;

    @Column(name = "extra", length = 4000)
    private String extra;

    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "price_text")
    private String priceText;

    @Column(name = "city")
    private String city;

    @Column(name = "neighborhood")
    private String neighborhood;

    @Column(name = "type")
    private String type;

    @Column(name = "image_url", length = 2000)
    private String imageUrl;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;
}
