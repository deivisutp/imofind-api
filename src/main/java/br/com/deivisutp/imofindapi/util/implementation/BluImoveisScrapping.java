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

import static br.com.deivisutp.imofindapi.util.ScrappingList.BLU_IMOVEIS;

/**
 * Blu Imoveis (plataforma MSYS Imob), Blumenau. robots.txt permite.
 * A busca e servida por POST /api/service/consult (Solr): response.docs[] com os imoveis.
 */
@Component
public class BluImoveisScrapping implements IScrapping {

    private static final String CONSULT_URL = "https://www.blumenauimoveis.com.br/api/service/consult";
    private static final String LISTING_BASE = "https://www.blumenauimoveis.com.br/imovel/";
    private static final int PAGE_SIZE = 48;
    private static final int MAX_PAGES = 50;
    private static final int MAX_EXTRA_LENGTH = 255;
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    // Corpo observado da busca de venda (type "S"); apenas start/numRows variam por pagina.
    private static final String BODY_TEMPLATE = "{\"start\":%d,\"numRows\":%d,\"type\":\"S\",\"place\":null,"
            + "\"idtCityList\":[],\"idtDistrictList\":[],\"idtCondominiumList\":[],\"idtsCategories\":[],"
            + "\"idtsSubCategories\":[],\"mapSubCategories\":{},\"rooms\":null,\"bathrooms\":null,\"garages\":null,"
            + "\"characteristics\":[],\"condominiumCharacteristics\":[],\"fromPrice\":null,\"toPrice\":null,"
            + "\"minArea\":null,\"maxArea\":null,\"usefulArea\":false,\"namStreet\":null,\"searchTotal\":true,"
            + "\"flgRentByPeriod\":false,\"getAccess\":true,\"post\":true,\"sortList\":[\"moreRecentsSales\"],"
            + "\"fieldList\":[\"idtProperty\",\"jsonPhotos\",\"namDistrict\",\"namCity\",\"namState\",\"namCategory\","
            + "\"namSubCategory\",\"prop_char_1\",\"prop_char_2\",\"valSales\",\"totalRooms\",\"totalGarages\","
            + "\"indType\",\"flgHideValSaleSite\",\"desTitleSite\"],\"jsonPhotosNum\":1}";

    private final DocumentFetcher fetcher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BluImoveisScrapping(DocumentFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public String sourceCode() {
        return BLU_IMOVEIS;
    }

    @Override
    public String extractorVersion() {
        return "blu-v1";
    }

    @Override
    public List<ImovelDTO> collect() throws IOException {
        List<ImovelDTO> result = new ArrayList<>();
        int start = 0;
        for (int page = 0; page < MAX_PAGES; page++) {
            JsonNode response = readRoot(fetcher.postJson(CONSULT_URL, BODY_TEMPLATE.formatted(start, PAGE_SIZE)))
                    .path("response");
            JsonNode docs = response.path("docs");
            if (!docs.isArray() || docs.isEmpty()) {
                break;
            }
            for (JsonNode doc : docs) {
                toDto(doc).ifPresent(result::add);
            }
            start += docs.size();
            if (start >= response.path("numFound").asInt(0)) {
                break;
            }
        }
        return result;
    }

    @Override
    public List<ImovelDTO> extract(Document listPage) {
        return parseListings(listPage.wholeText());
    }

    /** Extrai os imoveis de uma resposta consult (response.docs). */
    List<ImovelDTO> parseListings(String rawBody) {
        List<ImovelDTO> result = new ArrayList<>();
        for (JsonNode doc : readRoot(rawBody).path("response").path("docs")) {
            toDto(doc).ifPresent(result::add);
        }
        return result;
    }

    private JsonNode readRoot(String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
        } catch (IOException e) {
            throw new IllegalStateException("JSON invalido do Blu Imoveis: " + e.getMessage(), e);
        }
    }

    private java.util.Optional<ImovelDTO> toDto(JsonNode doc) {
        if (doc.path("flgHideValSaleSite").asInt(0) == 1) {
            return java.util.Optional.empty();
        }
        BigDecimal price = doc.hasNonNull("valSales") ? new BigDecimal(doc.get("valSales").asText()) : null;
        if (price == null || price.signum() <= 0) {
            return java.util.Optional.empty();
        }
        String id = doc.path("idtProperty").asText(null);
        BigDecimal area = toBigDecimal(doc.path("prop_char_1"));
        ImovelDTO dto = new ImovelDTO(
                title(doc),
                buildExtra(doc, area),
                price,
                BLU_IMOVEIS,
                formatPrice(price),
                id != null ? LISTING_BASE + id : null,
                firstPhoto(doc),
                emptyToNull(doc.path("namCity").asText(null)),
                emptyToNull(doc.path("namDistrict").asText(null)),
                emptyToNull(doc.path("namCategory").asText(null))
        );
        dto.setArea(area);
        return java.util.Optional.of(dto);
    }

    private String title(JsonNode doc) {
        String desTitle = doc.path("desTitleSite").asText(null);
        if (desTitle != null && !desTitle.isBlank()) {
            return desTitle;
        }
        return (orEmpty(doc.path("namCategory").asText(null)) + " em " + orEmpty(doc.path("namDistrict").asText(null))).trim();
    }

    private static String buildExtra(JsonNode doc, BigDecimal area) {
        StringBuilder sb = new StringBuilder();
        appendCount(sb, doc.path("totalRooms").asInt(0), "quarto", "quartos");
        appendCount(sb, doc.path("totalGarages").asInt(0), "vaga", "vagas");
        if (area != null && area.signum() > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(area.stripTrailingZeros().toPlainString()).append("m2");
        }
        return truncate(sb.toString());
    }

    private static void appendCount(StringBuilder sb, int value, String singular, String plural) {
        if (value <= 0) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(value).append(' ').append(value == 1 ? singular : plural);
    }

    private String firstPhoto(JsonNode doc) {
        String jsonPhotos = doc.path("jsonPhotos").asText(null);
        if (jsonPhotos == null || jsonPhotos.isBlank()) {
            return null;
        }
        try {
            JsonNode photos = objectMapper.readTree(jsonPhotos);
            if (photos.isArray() && !photos.isEmpty()) {
                return emptyToNull(photos.get(0).path("urlPhoto").asText(null));
            }
        } catch (IOException ignored) {
            // sem foto utilizavel
        }
        return null;
    }

    private static BigDecimal toBigDecimal(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String text = node.asText("").replaceAll("[^0-9.]", "");
        if (text.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
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

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
