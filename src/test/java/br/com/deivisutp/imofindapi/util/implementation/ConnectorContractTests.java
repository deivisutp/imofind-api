package br.com.deivisutp.imofindapi.util.implementation;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Testes de contrato: extração pura contra HTML congelado por fonte. */
class ConnectorContractTests {

    @Test
    void imoveisScExtractsListingFromFrozenHtml() throws IOException {
        List<ImovelDTO> list = new ImoveisSCScrapping(null, null).extract(load("fixtures/imoveissc-list.html"));

        assertThat(list).hasSize(1);
        ImovelDTO dto = list.get(0);
        assertThat(dto.getTitulo()).isEqualTo("Apartamento Central");
        assertThat(dto.getPrice()).isEqualByComparingTo(new BigDecimal("350000.00"));
        assertThat(dto.getOrigem()).isEqualTo("IMOVEIS-SC");
        assertThat(dto.getCity()).isEqualTo("Blumenau");
        assertThat(dto.getNeighborhood()).isEqualTo("Victor Konder");
        assertThat(dto.getType()).isEqualTo("Apartamento");
        assertThat(dto.getLink()).isEqualTo("https://www.imoveis-sc.com.br/imovel/123");
    }

    @Test
    void acrcExtractsListingFromFrozenHtml() throws IOException {
        List<ImovelDTO> list = new ACRCScrapping(null, null).extract(load("fixtures/acrc-list.html"));

        assertThat(list).hasSize(1);
        ImovelDTO dto = list.get(0);
        assertThat(dto.getPrice()).isEqualByComparingTo(new BigDecimal("480000.00"));
        assertThat(dto.getOrigem()).isEqualTo("ACRC");
        assertThat(dto.getCity()).isEqualTo("Blumenau");
        assertThat(dto.getNeighborhood()).isEqualTo("Velha");
        assertThat(dto.getType()).isEqualTo("Casa");
        assertThat(dto.getLink()).isEqualTo("https://www.acrcimoveis.com.br/imovel/casa-1");
    }

    @Test
    void zapExtractsListingFromFrozenHtml() throws IOException {
        List<ImovelDTO> list = new ZapImoveisScrapping(null, null).extract(load("fixtures/zap-list.html"));

        assertThat(list).hasSize(1);
        ImovelDTO dto = list.get(0);
        assertThat(dto.getPrice()).isEqualByComparingTo(new BigDecimal("420000.00"));
        assertThat(dto.getOrigem()).isEqualTo("ZAP-IMOVEIS");
        assertThat(dto.getCity()).isEqualTo("Blumenau");
        assertThat(dto.getNeighborhood()).isEqualTo("Victor Konder");
        assertThat(dto.getType()).isEqualTo("Apartamento");
        assertThat(dto.getLink()).isEqualTo("https://www.zapimoveis.com.br/imovel/zap-1");
    }

    private Document load(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Fixture não encontrado: " + resource);
            }
            return Jsoup.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
