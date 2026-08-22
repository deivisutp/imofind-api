package br.com.deivisutp.imofindapi.service;

import br.com.deivisutp.imofindapi.entities.CrawlRun;
import br.com.deivisutp.imofindapi.entities.CrawlRunStatus;
import br.com.deivisutp.imofindapi.entities.Source;
import br.com.deivisutp.imofindapi.repository.CrawlRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CrawlRunService {

    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final SourceService sourceService;
    private final CrawlRunRepository crawlRunRepository;

    public CrawlRunService(SourceService sourceService, CrawlRunRepository crawlRunRepository) {
        this.sourceService = sourceService;
        this.crawlRunRepository = crawlRunRepository;
    }

    @Transactional
    public CrawlRun start(String sourceCode, String extractorVersion) {
        Source source = sourceService.getOrCreate(sourceCode);
        CrawlRun run = new CrawlRun();
        run.setSource(source);
        run.setStatus(CrawlRunStatus.RUNNING);
        run.setStartedAt(Instant.now());
        run.setExtractorVersion(extractorVersion);
        return crawlRunRepository.save(run);
    }

    @Transactional
    public void finish(CrawlRun run, CrawlRunStatus status, int listingsSeen,
                       int observationsCreated, int eventsCreated, int errors, String message) {
        run.setStatus(status);
        run.setFinishedAt(Instant.now());
        run.setListingsSeen(listingsSeen);
        run.setObservationsCreated(observationsCreated);
        run.setEventsCreated(eventsCreated);
        run.setErrors(errors);
        run.setMessage(truncate(message));
        crawlRunRepository.save(run);
    }

    private static String truncate(String message) {
        if (message == null || message.length() <= MAX_MESSAGE_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_MESSAGE_LENGTH);
    }
}
