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

import static br.com.deivisutp.imofindapi.util.ScrappingList.ALLES;

/**
 * Alles Imoveis (plataforma ImoGestao, tema proprio). robots ausente; HTML server-rendered.
 * Busca de venda paginada por &pagina=N; recorte do piloto: cidade Blumenau (card traz a cidade).
 * Imagem real vem em data-src (src e placeholder de lazy-load).
 */
@Component
public class AllesScrapping implements IScrapping {

    private static final String SEARCH = "https://www.allesimoveis.com.br/imoveis?pretensao=comprar";
    private static final String ANCHOR = "a:has(div.card-imovel)";
    private static final int MAX_PAGES = 12;

    private final DocumentFetcher fetcher;

    public AllesScrapping(DocumentFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public String sourceCode() {
        return ALLES;
    }

    @Override
    public String extractorVersion() {
        return "alles-v1";
    }

    @Override
    public List<ImovelDTO> collect() throws IOException {
        List<ImovelDTO> result = new ArrayList<>();
        for (int page = 1; page <= MAX_PAGES; page++) {
            Document doc = fetcher.get(SEARCH + "&pagina=" + page);
            if (doc.select(ANCHOR).isEmpty()) {
                break; // sem cards: fim da paginacao
            }
            result.addAll(extract(doc));
        }
        return result;
    }

    @Override
    public List<ImovelDTO> extract(Document listPage) {
        List<ImovelDTO> list = new ArrayList<>();
        for (Element anchor : listPage.select(ANCHOR)) {
            Element info = anchor.selectFirst("div.card-imovel-info");
            if (info == null) {
                continue;
            }
            Element saleEl = info.selectFirst("p.price span.property-type");
            if (saleEl != null && !"venda".equalsIgnoreCase(saleEl.text().trim())) {
                continue; // ignora locacao
            }
            String city = cityOf(info);
            if (city == null || !"blumenau".equalsIgnoreCase(city)) {
                continue; // recorte do piloto
            }
            Element priceEl = info.selectFirst("p.price");
            BigDecimal price = priceEl != null ? ScrappingUtil.convertStringToBigDecimal(priceEl.ownText()) : null;
            if (price == null || price.signum() <= 0) {
                continue;
            }
            String neighborhood = textOf(info.selectFirst("h1.local-name"));
            String type = textOf(anchor.selectFirst("div.tag"));
            list.add(new ImovelDTO(
                    title(type, neighborhood, city),
                    null,
                    price,
                    ALLES,
                    priceEl.ownText().trim(),
                    absHref(anchor),
                    imageOf(anchor),
                    city,
                    neighborhood,
                    type));
        }
        return list;
    }

    private static String cityOf(Element info) {
        String raw = textOf(info.selectFirst("h2.local-city")); // ex.: "Blumenau/SC"
        if (raw == null) {
            return null;
        }
        int slash = raw.indexOf('/');
        return (slash >= 0 ? raw.substring(0, slash) : raw).trim();
    }

    private static String imageOf(Element anchor) {
        Element img = anchor.selectFirst("div.card-imovel-image img");
        if (img == null) {
            return null;
        }
        String dataSrc = img.attr("data-src");
        return !dataSrc.isBlank() ? dataSrc : img.attr("src");
    }

    private static String absHref(Element anchor) {
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
