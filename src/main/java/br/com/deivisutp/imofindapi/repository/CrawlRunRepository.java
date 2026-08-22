package br.com.deivisutp.imofindapi.repository;

import br.com.deivisutp.imofindapi.entities.CrawlRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlRunRepository extends JpaRepository<CrawlRun, Long> {
}
