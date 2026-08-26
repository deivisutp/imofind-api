package br.com.deivisutp.imofindapi.util;

import org.jsoup.nodes.Document;

import java.io.IOException;

/** Abstração de obtenção de páginas; permite testar conectores com HTML congelado. */
public interface DocumentFetcher {
    Document get(String url) throws IOException;

    /** Corpo bruto de um recurso não-HTML (ex.: endpoint JSON/JS), com a mesma política de coleta. */
    String getText(String url) throws IOException;
}
