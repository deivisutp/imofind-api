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

import static br.com.deivisutp.imofindapi.util.ScrappingList.RAYMUNDI;

/**
 * Raymundi Imobiliaria (plataforma ImoGestao, tema Tailwind). robots permite tudo; HTML server-rendered.
 * O card nao traz a cidade, entao usamos a rota filtrada ?cidade=Blumenau (Blumenau fixo, recorte do piloto).
 * Essa rota nao pagina: coletamos a pagina unica. Filtra venda pelo selo do card.
 * OBS: tema com classes utilitarias (sem hooks semanticos) -> seletores mais frageis.
 */
@Component
public class RaymundiScrapping implements IScrapping {

    private static final String SEARCH = "https://www.raymundi.com.br/imoveis?cidade=Blumenau";
    private static final String CITY = "Blumenau";
    private static final String ANCHOR = "a[href*=referencia]:has(div.p-5)";

    private final DocumentFetcher fetcher;

    public RaymundiScrapping(DocumentFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public String sourceCode() {
        return RAYMUNDI;
    }

    @Override
    public String extractorVersion() {
        return "raymundi-v1";
    }

    @Override
    public List<ImovelDTO> collect() throws IOException {
        return extract(fetcher.get(SEARCH));
    }

    @Override
    public List<ImovelDTO> extract(Document listPage) {
        List<ImovelDTO> list = new ArrayList<>();
        for (Element anchor : listPage.select(ANCHOR)) {
            Element badge = anchor.selectFirst("span.absolute");
            if (badge != null && !"venda".equalsIgnoreCase(badge.text().trim())) {
                continue; // ignora locacao
            }
            Element info = anchor.selectFirst("div.p-5");
            if (info == null) {
                continue;
            }
            Element priceEl = info.selectFirst("p:contains(R$)");
            BigDecimal price = priceEl != null ? ScrappingUtil.convertStringToBigDecimal(priceEl.text()) : null;
            if (price == null || price.signum() <= 0) {
                continue;
            }
            String type = textOf(info.selectFirst("span.inline-block"));
            String neighborhood = textOf(info.selectFirst("h5"));
            list.add(new ImovelDTO(
                    title(type, neighborhood, CITY),
                    null,
                    price,
                    RAYMUNDI,
                    priceEl.text().trim(),
                    absHref(anchor),
                    imageOf(anchor),
                    CITY,
                    neighborhood,
                    type));
        }
        return list;
    }

    private static String imageOf(Element anchor) {
        Element img = anchor.selectFirst("img");
        if (img == null) {
            return null;
        }
        String abs = img.attr("abs:src");
        return !abs.isBlank() ? abs : img.attr("src");
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
