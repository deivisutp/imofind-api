package br.com.deivisutp.imofindapi.service;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.entities.CrawlRun;
import br.com.deivisutp.imofindapi.entities.CrawlRunStatus;
import br.com.deivisutp.imofindapi.entities.Source;
import br.com.deivisutp.imofindapi.util.IScrapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ScrappingImoFindService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrappingImoFindService.class);

    private final List<IScrapping> connectors;
    private final CrawlRunService crawlRunService;
    private final ListingIngestService ingestService;
    private final SourceService sourceService;

    public ScrappingImoFindService(List<IScrapping> connectors,
                                   CrawlRunService crawlRunService,
                                   ListingIngestService ingestService,
                                   SourceService sourceService) {
        this.connectors = connectors;
        this.crawlRunService = crawlRunService;
        this.ingestService = ingestService;
        this.sourceService = sourceService;
    }

    public void executeScrappingService() {
        for (IScrapping connector : connectors) {
            runConnector(connector);
        }
    }

    /** Cada fonte roda isolada: uma falha registra crawl_run FAILED e não interrompe as demais. */
    private void runConnector(IScrapping connector) {
        CrawlRun run = crawlRunService.start(connector.sourceCode(), connector.extractorVersion());
        int observations = 0;
        int events = 0;
        int seen = 0;
        try {
            List<ImovelDTO> listings = connector.collect();
            Set<String> seenExternalIds = new HashSet<>();
            for (ImovelDTO dto : listings) {
                IngestResult result = ingestService.ingest(dto, run);
                if (result.getExternalId() != null) {
                    seenExternalIds.add(result.getExternalId());
                }
                if (result.isObservationCreated()) {
                    observations++;
                }
                events += result.getEventsCreated();
            }
            seen = seenExternalIds.size();
            // REMOVED só após coleta bem-sucedida, para não marcar remoções em coleta parcial.
            Source source = sourceService.getOrCreate(connector.sourceCode());
            events += ingestService.markRemoved(source, seenExternalIds, run);
            crawlRunService.finish(run, CrawlRunStatus.SUCCESS, seen, observations, events, 0, null);
            LOGGER.info("Fonte {} coletada: {} anuncios, {} observacoes, {} eventos.",
                    connector.sourceCode(), seen, observations, events);
        } catch (Exception e) {
            LOGGER.error("Falha na coleta da fonte {}: {}", connector.sourceCode(), e.getMessage(), e);
            crawlRunService.finish(run, CrawlRunStatus.FAILED, seen, observations, events, 1, e.getMessage());
        }
    }
}
