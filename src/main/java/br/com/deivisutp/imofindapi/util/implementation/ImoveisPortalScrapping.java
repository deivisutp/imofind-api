package br.com.deivisutp.imofindapi.util.implementation;

import br.com.deivisutp.imofindapi.config.ScrapingProperties;
import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.util.DocumentFetcher;
import br.com.deivisutp.imofindapi.util.IScrapping;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static br.com.deivisutp.imofindapi.util.ScrappingList.IMOVEIS_PORTAL;

/**
 * Imoveis Portal (Blumenau). robots.txt permite; cada card embute um dump estruturado
 * (print_r) em um {@code <pre>} oculto, do qual extraimos os campos.
 */
@Component
public class ImoveisPortalScrapping implements IScrapping {

    private static final String BASE = "https://www.imoveisportal.com/busca/";
    // Recorte do piloto: Blumenau, venda, apartamentos e casas.
    private static final List<String> SEEDS = List.of(
            "apartamento/venda/blumenau",
            "casa/venda/blumenau"
    );
    private static final String CARD = "div.box-imovel";
    private static final int MAX_EXTRA_LENGTH = 255;
    private static final int MAX_TITLE_LENGTH = 4000;
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    // [ \t] (nao \s) para um valor vazio nao "engolir" a proxima linha do dump.
    private static final Pattern FIELD = Pattern.compile("\\[([a-zA-Z0-9_]+)\\][ \\t]*=>[ \\t]*([^\\r\\n]*)");

    private final DocumentFetcher fetcher;
    private final ScrapingProperties properties;

    public ImoveisPortalScrapping(DocumentFetcher fetcher, ScrapingProperties properties) {
        this.fetcher = fetcher;
        this.properties = properties;
    }

    @Override
    public String sourceCode() {
        return IMOVEIS_PORTAL;
    }

    @Override
    public String extractorVersion() {
        return "portal-v1";
    }

    @Override
    public List<ImovelDTO> collect() throws IOException {
        List<ImovelDTO> result = new ArrayList<>();
        for (String seed : SEEDS) {
            for (int page = 1; page < properties.getMaxPages(); page++) {
                List<ImovelDTO> pageListings = extract(fetcher.get(BASE + seed + "?page=" + page));
                if (pageListings.isEmpty()) {
                    break;
                }
                result.addAll(pageListings);
            }
        }
        return result;
    }

    @Override
    public List<ImovelDTO> extract(Document document) {
        List<ImovelDTO> list = new ArrayList<>();
        Elements cards = document.select(CARD);
        for (Element card : cards) {
            Element pre = card.selectFirst("pre");
            if (pre == null) {
                continue;
            }
            Map<String, String> data = parseFields(pre.wholeText());
            BigDecimal price = toBigDecimal(data.get("valores_valorvenda"));
            if (price == null) {
                continue; // sem valor de venda: ignora (ex.: apenas locacao)
            }
            ImovelDTO dto = new ImovelDTO(
                    title(data),
                    buildExtra(data),
                    price,
                    IMOVEIS_PORTAL,
                    formatPrice(price),
                    link(card),
                    emptyToNull(data.get("imagem")),
                    emptyToNull(data.get("ficha_cadastral_cidade")),
                    emptyToNull(data.get("ficha_cadastral_bairro")),
                    emptyToNull(data.get("ficha_cadastral_tipo"))
            );
            dto.setArea(toBigDecimal(data.get("caracteristicas_areatotal")));
            list.add(dto);
        }
        return list;
    }

    private static Map<String, String> parseFields(String preText) {
        Map<String, String> data = new LinkedHashMap<>();
        Matcher matcher = FIELD.matcher(preText);
        while (matcher.find()) {
            data.put(matcher.group(1), matcher.group(2).trim());
        }
        return data;
    }

    private static String title(Map<String, String> data) {
        String curta = data.get("ficha_cadastral_curta");
        if (curta != null && !curta.isBlank()) {
            return truncate(curta, MAX_TITLE_LENGTH);
        }
        String tipo = orEmpty(data.get("ficha_cadastral_tipo"));
        String bairro = orEmpty(data.get("ficha_cadastral_bairro"));
        return (tipo + " em " + bairro).trim();
    }

    private static String buildExtra(Map<String, String> data) {
        StringBuilder sb = new StringBuilder();
        appendCount(sb, data.get("caracteristicas_dormitorios"), "quarto", "quartos");
        appendCount(sb, data.get("caracteristicas_banheiros"), "banheiro", "banheiros");
        appendCount(sb, data.get("caracteristicas_garagens"), "vaga", "vagas");
        String area = data.get("caracteristicas_areatotal");
        if (area != null && !area.isBlank()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(area).append("m2");
        }
        return truncate(sb.toString(), MAX_EXTRA_LENGTH);
    }

    private static void appendCount(StringBuilder sb, String value, String singular, String plural) {
        int n = toInt(value);
        if (n <= 0) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(n).append(' ').append(n == 1 ? singular : plural);
    }

    private static String link(Element card) {
        Element anchor = card.selectFirst("div.box-img a[href]");
        return anchor != null ? anchor.attr("href") : null;
    }

    private static BigDecimal toBigDecimal(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.trim().replaceAll("[^0-9.]", "");
        if (digits.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int toInt(String value) {
        if (value == null) {
            return 0;
        }
        String digits = value.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private static String formatPrice(BigDecimal price) {
        return price == null ? null : NumberFormat.getCurrencyInstance(PT_BR).format(price);
    }

    private static String truncate(String text, int max) {
        if (text == null || text.length() <= max) {
            return text;
        }
        return text.substring(0, max);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
