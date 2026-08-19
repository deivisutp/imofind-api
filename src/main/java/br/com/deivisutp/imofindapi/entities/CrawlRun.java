package br.com.deivisutp.imofindapi.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "crawl_run")
@Getter
@Setter
@NoArgsConstructor
public class CrawlRun implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crawl_run_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private CrawlRunStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "listings_seen", nullable = false)
    private Integer listingsSeen = 0;

    @Column(name = "observations_created", nullable = false)
    private Integer observationsCreated = 0;

    @Column(name = "events_created", nullable = false)
    private Integer eventsCreated = 0;

    @Column(name = "errors", nullable = false)
    private Integer errors = 0;

    @Column(name = "extractor_version", length = 64)
    private String extractorVersion;

    @Column(name = "message")
    private String message;
}
