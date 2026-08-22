package br.com.deivisutp.imofindapi.service;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.entities.CrawlRun;
import br.com.deivisutp.imofindapi.entities.Source;
import br.com.deivisutp.imofindapi.repository.ImovelRepository;
import br.com.deivisutp.imofindapi.repository.ListingEventRepository;
import br.com.deivisutp.imofindapi.repository.ListingObservationRepository;
import br.com.deivisutp.imofindapi.repository.SourceListingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class ListingIngestServiceTests {

    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:13-3.4").asCompatibleSubstituteFor("postgres"));

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private ListingIngestService ingestService;
    @Autowired
    private SourceListingRepository listingRepository;
    @Autowired
    private ListingObservationRepository observationRepository;
    @Autowired
    private ListingEventRepository eventRepository;
    @Autowired
    private ImovelRepository imovelRepository;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private CrawlRunService crawlRunService;

    @Test
    void ingestCreatesListingObservationProjectionAndNewEvent() {
        ingestService.ingest(dto("https://ex/1", "R$ 350.000,00", new BigDecimal("350000.00")));

        assertThat(listingRepository.count()).isEqualTo(1);
        assertThat(observationRepository.count()).isEqualTo(1);
        assertThat(imovelRepository.count()).isEqualTo(1);
        assertThat(eventRepository.count()).isEqualTo(1);
    }

    @Test
    void reingestingSameDataIsIdempotent() {
        ImovelDTO dto = dto("https://ex/2", "R$ 500.000,00", new BigDecimal("500000.00"));
        ingestService.ingest(dto);
        ingestService.ingest(dto);

        assertThat(listingRepository.count()).isEqualTo(1);
        assertThat(observationRepository.count()).isEqualTo(1);
        assertThat(eventRepository.count()).isEqualTo(1);
    }

    @Test
    void priceChangeCreatesNewObservationAndPriceChangedEvent() {
        ingestService.ingest(dto("https://ex/3", "R$ 400.000,00", new BigDecimal("400000.00")));
        ingestService.ingest(dto("https://ex/3", "R$ 380.000,00", new BigDecimal("380000.00")));

        assertThat(observationRepository.count()).isEqualTo(2);
        assertThat(eventRepository.count()).isEqualTo(2);
    }

    @Test
    void removedSweepDeactivatesUnseenListings() {
        IngestResult seenListing = ingestService.ingest(dto("https://ex/a", "R$ 100.000,00", new BigDecimal("100000.00")));
        ingestService.ingest(dto("https://ex/b", "R$ 200.000,00", new BigDecimal("200000.00")));

        Source source = sourceService.getOrCreate("IMOVEIS-SC");
        CrawlRun run = crawlRunService.start("IMOVEIS-SC", "test");
        int removed = ingestService.markRemoved(source, Set.of(seenListing.getExternalId()), run);

        assertThat(removed).isEqualTo(1);
        assertThat(listingRepository.findBySourceAndActiveTrue(source)).hasSize(1);
    }

    private ImovelDTO dto(String link, String priceText, BigDecimal price) {
        ImovelDTO dto = new ImovelDTO();
        dto.setOrigem("IMOVEIS-SC");
        dto.setTitulo("Apartamento");
        dto.setExtra("Descricao");
        dto.setPrice(price);
        dto.setPrice_varchar(priceText);
        dto.setLink(link);
        dto.setImage("https://ex/img.jpg");
        dto.setCity("Blumenau");
        dto.setNeighborhood("Victor Konder");
        dto.setType("Apartamento");
        return dto;
    }
}
