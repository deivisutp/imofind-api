package br.com.deivisutp.imofindapi.service;

import br.com.deivisutp.imofindapi.entities.Source;
import br.com.deivisutp.imofindapi.repository.SourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SourceService {

    private static final String UNKNOWN_SOURCE = "UNKNOWN";

    private final SourceRepository sourceRepository;

    public SourceService(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @Transactional
    public Source getOrCreate(String code) {
        String resolved = (code == null || code.isEmpty()) ? UNKNOWN_SOURCE : code;
        return sourceRepository.findByCode(resolved).orElseGet(() -> {
            Source source = new Source();
            source.setCode(resolved);
            source.setName(resolved);
            source.setActive(Boolean.TRUE);
            source.setCreatedAt(Instant.now());
            source.setUpdatedAt(Instant.now());
            return sourceRepository.save(source);
        });
    }
}
