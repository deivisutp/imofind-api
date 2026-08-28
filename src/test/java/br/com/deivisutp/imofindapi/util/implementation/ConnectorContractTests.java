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
    void acrcExtractsSaleListingsFromDataEndpoint() throws IOException {
        List<ImovelDTO> list = new ACRCScrapping(null).parseListings(loadString("fixtures/acrc-data.js"));

        // filtra somente venda: o fixture tem 1 sale + 1 rent
        assertThat(list).hasSize(1);
        ImovelDTO dto = list.get(0);
        assertThat(dto.getTitulo()).isEqualTo("Casa em Velha, Blumenau");
        assertThat(dto.getPrice()).isEqualByComparingTo(new BigDecimal("480000"));
        assertThat(dto.getOrigem()).isEqualTo("ACRC");
        assertThat(dto.getCity()).isEqualTo("Blumenau");
        assertThat(dto.getNeighborhood()).isEqualTo("Velha");
        assertThat(dto.getType()).isEqualTo("Casa");
        assertThat(dto.getLink()).isEqualTo("https://www.acrcimoveis.com.br/imovel/CA03902");
        assertThat(dto.getExtra()).isEqualTo("3 quartos, 1 suite, 2 vagas, 120m2");
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

    @Test
    void portalExtractsOnlySaleListingsFromFrozenHtml() throws IOException {
        List<ImovelDTO> list = new ImoveisPortalScrapping(null, null).extract(load("fixtures/portal-list.html"));

        // fixture tem 1 venda + 1 locacao (sem valorvenda): so a venda entra
        assertThat(list).hasSize(1);
        ImovelDTO dto = list.get(0);
        assertThat(dto.getPrice()).isEqualByComparingTo(new BigDecimal("590000"));
        assertThat(dto.getOrigem()).isEqualTo("IMOVEIS-PORTAL");
        assertThat(dto.getCity()).isEqualTo("Blumenau");
        assertThat(dto.getNeighborhood()).isEqualTo("Victor Konder");
        assertThat(dto.getType()).isEqualTo("Apartamento");
        assertThat(dto.getArea()).isEqualByComparingTo(new BigDecimal("94.81"));
        assertThat(dto.getLink())
                .isEqualTo("https://www.imoveisportal.com/apartamento/venda/blumenau/victor-konder/35718433");
        assertThat(dto.getExtra()).isEqualTo("2 quartos, 2 banheiros, 1 vaga, 94.81m2");
    }

    @Test
    void torresulExtractsSaleListingFromFrozenHtml() throws IOException {
        List<ImovelDTO> list = new TorresulScrapping(null).extract(load("fixtures/torresul-list.html"));

        // fixture tem 1 venda com preco + 1 "Sob consulta" (sem preco): so a com preco entra
        assertThat(list).hasSize(1);
        ImovelDTO dto = list.get(0);
        assertThat(dto.getPrice()).isEqualByComparingTo(new BigDecimal("280000.00"));
        assertThat(dto.getOrigem()).isEqualTo("TORRESUL");
        assertThat(dto.getCity()).isEqualTo("Blumenau");
        assertThat(dto.getNeighborhood()).isEqualTo("Garcia");
        assertThat(dto.getType()).isEqualTo("Apartamento");
        assertThat(dto.getArea()).isEqualByComparingTo(new BigDecimal("53"));
        assertThat(dto.getLink())
                .isEqualTo("https://torresulimobiliaria.com.br/imovel/17971/apartamento-1-quarto-garcia-blumenau/");
        assertThat(dto.getImage())
                .isEqualTo("https://rocketstatic2.com.br/media/torresulimobiliaria/imo-fotos/17971/2d195c18e0469ea7.jpg.webp?v=9855");
        assertThat(dto.getExtra()).isEqualTo("1 quarto, 1 vaga, 53m²");
    }

    private Document load(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Fixture não encontrado: " + resource);
            }
            return Jsoup.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private String loadString(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Fixture não encontrado: " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
