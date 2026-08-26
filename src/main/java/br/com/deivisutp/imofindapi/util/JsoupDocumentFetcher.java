package br.com.deivisutp.imofindapi.util;

import br.com.deivisutp.imofindapi.config.ScrapingProperties;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;

/** Coleta responsável: user-agent, timeout e backoff exponencial em 429/5xx e timeouts. */
@Component
public class JsoupDocumentFetcher implements DocumentFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsoupDocumentFetcher.class);

    private final ScrapingProperties properties;

    public JsoupDocumentFetcher(ScrapingProperties properties) {
        this.properties = properties;
    }

    @Override
    public Document get(String url) throws IOException {
        ScrapingProperties.Fetcher cfg = properties.getFetcher();
        IOException lastError = null;
        for (int attempt = 0; attempt <= cfg.getMaxRetries(); attempt++) {
            try {
                return Jsoup.connect(url)
                        .userAgent(cfg.getUserAgent())
                        .timeout(cfg.getTimeoutMs())
                        .followRedirects(true)
                        .method(Connection.Method.GET)
                        .get();
            } catch (HttpStatusException e) {
                if (isRetryable(e.getStatusCode())) {
                    lastError = e;
                    backoff(attempt, url, e);
                } else {
                    throw e;
                }
            } catch (SocketTimeoutException e) {
                lastError = e;
                backoff(attempt, url, e);
            }
        }
        throw lastError != null ? lastError : new IOException("Falha ao obter " + url);
    }

    @Override
    public String getText(String url) throws IOException {
        ScrapingProperties.Fetcher cfg = properties.getFetcher();
        IOException lastError = null;
        for (int attempt = 0; attempt <= cfg.getMaxRetries(); attempt++) {
            try {
                return Jsoup.connect(url)
                        .userAgent(cfg.getUserAgent())
                        .timeout(cfg.getTimeoutMs())
                        .followRedirects(true)
                        .ignoreContentType(true)
                        .maxBodySize(0)
                        .method(Connection.Method.GET)
                        .execute()
                        .body();
            } catch (HttpStatusException e) {
                if (isRetryable(e.getStatusCode())) {
                    lastError = e;
                    backoff(attempt, url, e);
                } else {
                    throw e;
                }
            } catch (SocketTimeoutException e) {
                lastError = e;
                backoff(attempt, url, e);
            }
        }
        throw lastError != null ? lastError : new IOException("Falha ao obter " + url);
    }

    private static boolean isRetryable(int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }

    private void backoff(int attempt, String url, Exception cause) {
        long waitMs = properties.getFetcher().getBackoffMs() * (attempt + 1L);
        LOGGER.warn("Falha ao obter {} (tentativa {}): {}. Aguardando {}ms.", url, attempt + 1, cause.getMessage(), waitMs);
        try {
            Thread.sleep(waitMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
