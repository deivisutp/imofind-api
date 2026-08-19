package br.com.deivisutp.imofindapi.util;

import org.jsoup.nodes.Document;

import java.io.IOException;

/** Abstração de obtenção de páginas; permite testar conectores com HTML congelado. */
public interface DocumentFetcher {
    Document get(String url) throws IOException;
}
