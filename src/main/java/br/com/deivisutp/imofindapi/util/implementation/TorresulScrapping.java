package br.com.deivisutp.imofindapi.util.implementation;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.util.DocumentFetcher;
import br.com.deivisutp.imofindapi.util.IScrapping;
import br.com.deivisutp.imofindapi.util.ScrappingUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static br.com.deivisutp.imofindapi.util.ScrappingList.TORRESUL;

/**
 * Torresul (Blumenau/Joinville/Indaial). robots.txt permite; HTML server-rendered.
 * MVP: coleta a pagina 1 de cada busca (paginas seguintes sao AJAX, adiadas).
 */
@Component
public class TorresulScrapping implements IScrapping {

    private static final String BASE = "https://torresulimobiliaria.com.br/venda/";
    // Recorte do piloto: Blumenau, venda, apartamentos e casas.
    private static final List<String> SEEDS = List.of("apartamento/blumenau/", "casa/blumenau/");
    private static final String CARD = "div.imovel-box-single";
    private static final int MAX_EXTRA_LENGTH = 255;
    private static final Pattern BG_URL = Pattern.compile("url\\((['\"]?)(.*?)\\1\\)");

    private final DocumentFetcher fetcher;

    public TorresulScrapping(DocumentFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public String sourceCode() {
        return TORRESUL;
    }

    @Override
    public String extractorVersion() {
        return "torresul-v1";
    }

    @Override
    public List<ImovelDTO> collect() throws IOException {
        List<ImovelDTO> result = new ArrayList<>();
        for (String seed : SEEDS) {
            result.addAll(extract(fetcher.get(BASE + seed)));
        }
        return result;
    }

    @Override
    public List<ImovelDTO> extract(Document document) {
        List<ImovelDTO> list = new ArrayList<>();
        for (Element card : document.select(CARD)) {
            Element priceEl = card.selectFirst("span.thumb-price[itemprop=price]");
            BigDecimal price = priceEl != null ? ScrappingUtil.convertStringToBigDecimal(priceEl.text()) : null;
            if (price == null) {
                continue; // sem preco de venda: ignora
            }
            String address = text(card.selectFirst("h3[itemprop=streetAddress]"));
            String title = text(card.selectFirst("h2.titulo-grid"));
            Amenities am = amenities(card);
            ImovelDTO dto = new ImovelDTO(
                    title,
                    buildExtra(am),
                    price,
                    TORRESUL,
                    priceEl.text().trim(),
                    link(card),
                    mainImage(card),
                    city(address),
                    neighborhood(address),
                    type(title)
            );
            dto.setArea(am.area);
            list.add(dto);
        }
        return list;
    }

    private static Amenities amenities(Element card) {
        Amenities am = new Amenities();
        for (Element div : card.select("div.amenities-main > div")) {
            Element icon = div.selectFirst("i");
            Element span = div.selectFirst("span");
            if (icon == null || span == null) {
                continue;
            }
            String value = span.text().trim();
            if (icon.hasClass("fa-bed")) {
                am.bedrooms = value;
            } else if (icon.hasClass("fa-car")) {
                am.garages = value;
            } else if (icon.hasClass("fa-compress-arrows-alt")) {
                am.areaText = value; // ex.: "53m²"
                am.area = ScrappingUtil.convertStringToBigDecimal(value);
            }
        }
        return am;
    }

    private static String buildExtra(Amenities am) {
        StringBuilder sb = new StringBuilder();
        appendCount(sb, am.bedrooms, "quarto", "quartos");
        appendCount(sb, am.garages, "vaga", "vagas");
        if (am.areaText != null && !am.areaText.isBlank()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(am.areaText.replace(" ", ""));
        }
        return truncate(sb.toString());
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

    private static String neighborhood(String address) {
        if (address == null) {
            return null;
        }
        int i = address.indexOf(" - ");
        return i > 0 ? address.substring(0, i).trim() : null;
    }

    private static String city(String address) {
        if (address == null) {
            return null;
        }
        int i = address.indexOf(" - ");
        String rest = i > 0 ? address.substring(i + 3) : address;
        int slash = rest.indexOf('/');
        return (slash > 0 ? rest.substring(0, slash) : rest).trim();
    }

    private static String type(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return title.trim().split("\\s+")[0];
    }

    private static String link(Element card) {
        Element anchor = card.selectFirst("a[href*=\"/imovel/\"]");
        return anchor != null ? anchor.attr("href") : null;
    }

    private static String mainImage(Element card) {
        Element slide = card.selectFirst("div.swiper-slide.foto-imovel");
        if (slide == null) {
            return null;
        }
        Matcher matcher = BG_URL.matcher(slide.attr("style"));
        if (matcher.find() && !matcher.group(2).isBlank()) {
            return matcher.group(2);
        }
        String background = slide.attr("data-background");
        return background.isBlank() ? null : background;
    }

    private static String text(Element element) {
        return element != null ? element.text().trim() : null;
    }

    private static int toInt(String value) {
        if (value == null) {
            return 0;
        }
        String digits = value.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private static String truncate(String text) {
        if (text == null || text.length() <= MAX_EXTRA_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_EXTRA_LENGTH);
    }

    private static final class Amenities {
        private String bedrooms;
        private String garages;
        private String areaText;
        private BigDecimal area;
    }
}
