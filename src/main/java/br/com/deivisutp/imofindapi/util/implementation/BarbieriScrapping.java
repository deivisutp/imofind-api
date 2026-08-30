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

import static br.com.deivisutp.imofindapi.util.ScrappingList.BARBIERI;

/**
 * Barbieri Negocios Imobiliarios (plataforma zroo). robots permite /pt_br; HTML server-rendered.
 * MVP: pagina unica /venda (48 mais recentes); paginas seguintes sao carregadas via API JS (adiado).
 * Recorte do piloto: cidade Blumenau (h3 traz "Cidade, Bairro").
 */
@Component
public class BarbieriScrapping implements IScrapping {

    private static final String SEARCH = "https://www.barbieriimobiliaria.com.br/venda";
    private static final String CARD = "article.property-card";
    private static final int MAX_EXTRA_LENGTH = 255;

    private final DocumentFetcher fetcher;

    public BarbieriScrapping(DocumentFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public String sourceCode() {
        return BARBIERI;
    }

    @Override
    public String extractorVersion() {
        return "barbieri-v1";
    }

    @Override
    public List<ImovelDTO> collect() throws IOException {
        return extract(fetcher.get(SEARCH));
    }

    @Override
    public List<ImovelDTO> extract(Document listPage) {
        List<ImovelDTO> list = new ArrayList<>();
        for (Element card : listPage.select(CARD)) {
            Element priceEl = card.selectFirst("p.property-card__price");
            BigDecimal price = priceEl != null ? ScrappingUtil.convertStringToBigDecimal(priceEl.text()) : null;
            if (price == null || price.signum() <= 0) {
                continue;
            }
            String[] loc = splitLocation(textOf(card.selectFirst("h3.property-card__location")));
            String city = loc[0];
            String neighborhood = loc[1];
            if (city == null || !"blumenau".equalsIgnoreCase(city)) {
                continue; // recorte do piloto
            }
            String type = textOf(card.selectFirst("span.property-card__badge"));
            BigDecimal area = areaOf(card);
            list.add(build(card, priceEl, price, city, neighborhood, type, area));
        }
        return list;
    }

    private ImovelDTO build(Element card, Element priceEl, BigDecimal price,
                            String city, String neighborhood, String type, BigDecimal area) {
        ImovelDTO dto = new ImovelDTO(
                title(type, neighborhood, city),
                buildExtra(card, area),
                price,
                BARBIERI,
                priceEl.text().trim(),
                absHref(card.selectFirst("a.property-card__overlay")),
                imageOf(card),
                city,
                neighborhood,
                type);
        dto.setArea(area);
        return dto;
    }

    private static String[] splitLocation(String location) {
        if (location == null || location.isBlank()) {
            return new String[]{null, null};
        }
        int comma = location.indexOf(',');
        if (comma < 0) {
            return new String[]{location.trim(), null};
        }
        return new String[]{location.substring(0, comma).trim(), location.substring(comma + 1).trim()};
    }

    private static BigDecimal areaOf(Element card) {
        for (Element li : card.select("ul.property-card__specs li")) {
            String text = li.text();
            if (text.contains("m²") || text.contains("m2")) {
                BigDecimal area = ScrappingUtil.convertStringToBigDecimal(text);
                return (area != null && area.signum() > 0) ? area : null;
            }
        }
        return null;
    }

    private static String buildExtra(Element card, BigDecimal area) {
        StringBuilder sb = new StringBuilder();
        for (Element li : card.select("ul.property-card__specs li")) {
            String text = li.text().trim();
            if (text.isEmpty() || "-".equals(text)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(text.replace(" m²", "m²").replace(" m2", "m2"));
        }
        String value = sb.toString();
        return value.length() <= MAX_EXTRA_LENGTH ? value : value.substring(0, MAX_EXTRA_LENGTH);
    }

    private static String imageOf(Element card) {
        Element img = card.selectFirst("img.property-card__photo");
        if (img == null) {
            return null;
        }
        String abs = img.attr("abs:src");
        return !abs.isBlank() ? abs : img.attr("src");
    }

    private static String absHref(Element anchor) {
        if (anchor == null) {
            return null;
        }
        String abs = anchor.attr("abs:href");
        return !abs.isBlank() ? abs : anchor.attr("href");
    }

    private static String textOf(Element el) {
        return el != null ? el.text().trim() : null;
    }

    private static String title(String type, String neighborhood, String city) {
        StringBuilder sb = new StringBuilder();
        if (type != null && !type.isBlank()) {
            sb.append(type);
        }
        if (neighborhood != null && !neighborhood.isBlank()) {
            if (sb.length() > 0) {
                sb.append(" em ");
            }
            sb.append(neighborhood);
        }
        return sb.length() > 0 ? sb.toString() : city;
    }
}
