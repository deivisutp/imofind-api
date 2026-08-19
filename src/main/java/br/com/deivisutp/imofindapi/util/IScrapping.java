package br.com.deivisutp.imofindapi.util;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

public interface IScrapping {

    /** Código estável da fonte (deve casar com a tabela source). */
    String sourceCode();

    /** Versão do extrator, registrada em cada execução. */
    String extractorVersion();

    /** Coleta completa (descoberta + paginação + extração). Propaga falha para isolar a fonte. */
    List<ImovelDTO> collect() throws IOException;

    /** Extração pura de uma página de listagem; usada nos testes de contrato com HTML congelado. */
    List<ImovelDTO> extract(Document listPage);
}
