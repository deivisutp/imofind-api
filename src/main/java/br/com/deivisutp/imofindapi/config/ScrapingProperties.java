package br.com.deivisutp.imofindapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "scraping")
@Getter
@Setter
public class ScrapingProperties {

    private int maxPages = 30;

    private final Fetcher fetcher = new Fetcher();

    @Getter
    @Setter
    public static class Fetcher {
        private String userAgent = "ImoFindBot/1.0 (+https://imofind.example.com/bot)";
        private int timeoutMs = 15000;
        private int maxRetries = 3;
        private long backoffMs = 1000;
    }
}
