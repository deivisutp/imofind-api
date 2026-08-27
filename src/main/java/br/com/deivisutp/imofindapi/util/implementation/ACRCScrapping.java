package br.com.deivisutp.imofindapi.util.implementation;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.util.DocumentFetcher;
import br.com.deivisutp.imofindapi.util.IScrapping;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static br.com.deivisutp.imofindapi.util.ScrappingList.ACRC;

/**
 * O ACRC virou SPA: o catalogo e servido em /api/data como `window.ACRC = {json};`.
 * Consumir esse JSON e mais estavel que raspar o HTML renderizado por JavaScript.
 */
@Component
public class ACRCScrapping implements IScrapping {

    private static final String ACRC_DATA_URL = "https://www.acrcimoveis.com.br/api/data?slim=1";
    private static final String ACRC_LISTING_BASE = "https://www.acrcimoveis.com.br/imovel/";
    private static final String SALE = "sale";
    private static final int MAX_EXTRA_LENGTH = 255;
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final DocumentFetcher fetcher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ACRCScrapping(DocumentFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public String sourceCode() {
        return ACRC;
    }

    @Override
    public String extractorVersion() {
        return "acrc-v2-json";
    }

    @Override
    public List<ImovelDTO> collect() throws IOException {
        return parseListings(fetcher.getText(ACRC_DATA_URL));
    }

    @Override
    public List<ImovelDTO> extract(Document listPage) {
        return parseListings(listPage.wholeText());
    }

    /** Extrai apenas anuncios de venda do payload `window.ACRC = {json};`. */
    List<ImovelDTO> parseListings(String rawBody) {
        List<ImovelDTO> result = new ArrayList<>();
        for (JsonNode listing : readRoot(rawBody).path("listings")) {
            if (SALE.equals(listing.path("transaction").asText())) {
                result.add(toDto(listing));
            }
        }
        return result;
    }

    private JsonNode readRoot(String rawBody) {
        String json = rawBody
                .replaceFirst("^\\s*window\\.ACRC\\s*=\\s*", "")
                .replaceFirst(";\\s*$", "");
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new IllegalStateException("JSON invalido do ACRC: " + e.getMessage(), e);
        }
    }

    private ImovelDTO toDto(JsonNode listing) {
        String id = listing.path("id").asText(null);
        BigDecimal price = listing.hasNonNull("price") ? new BigDecimal(listing.get("price").asText()) : null;
        ImovelDTO dto = new ImovelDTO(
                listing.path("title").asText(null),
                buildExtra(listing),
                price,
                ACRC,
                formatPrice(price),
                id != null ? ACRC_LISTING_BASE + id : null,
                firstImage(listing),
                listing.path("city").asText(null),
                listing.path("neighborhood").asText(null),
                listing.path("typePt").asText(null)
        );
        dto.setArea(extractArea(listing));
        return dto;
    }

    /** Area util quando disponivel; cai para a area do terreno. */
    private static BigDecimal extractArea(JsonNode listing) {
        double living = listing.path("livingArea").asDouble(0);
        double area = living > 0 ? living : listing.path("lotArea").asDouble(0);
        return area > 0 ? BigDecimal.valueOf(area) : null;
    }

    private static String buildExtra(JsonNode listing) {
        StringBuilder sb = new StringBuilder();
        appendCount(sb, listing.path("bedrooms").asInt(0), "quarto", "quartos");
        appendCount(sb, listing.path("suites").asInt(0), "suite", "suites");
        appendCount(sb, listing.path("garage").asInt(0), "vaga", "vagas");
        int area = listing.path("livingArea").asInt(0);
        if (area > 0) {
            appendSeparator(sb);
            sb.append(area).append("m2");
        }
        return truncate(sb.toString());
    }

    private static void appendCount(StringBuilder sb, int value, String singular, String plural) {
        if (value <= 0) {
            return;
        }
        appendSeparator(sb);
        sb.append(value).append(' ').append(value == 1 ? singular : plural);
    }

    private static void appendSeparator(StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
    }

    private static String firstImage(JsonNode listing) {
        JsonNode images = listing.path("images");
        return images.isArray() && !images.isEmpty() ? images.get(0).asText(null) : null;
    }

    private static String formatPrice(BigDecimal price) {
        return price == null ? null : NumberFormat.getCurrencyInstance(PT_BR).format(price);
    }

    private static String truncate(String text) {
        if (text == null || text.length() <= MAX_EXTRA_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_EXTRA_LENGTH);
    }
}
