package br.com.deivisutp.imofindapi.util.implementation;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.util.DocumentFetcher;
import br.com.deivisutp.imofindapi.util.IScrapping;
import br.com.deivisutp.imofindapi.util.ScrappingUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base para imobiliarias na plataforma ImoGestao (backend/imagens compartilhados,
 * mas tema de front proprio por site). A URL /imovel/{cod}/comprar/{tipo}/{cidade}/{bairro}
 * e comum, entao tipo/cidade/bairro saem da URL; preco/area/imagem/titulo sao por tema.
 * MVP: recorte de Blumenau (filtra pela cidade na URL).
 */
public abstract class AbstractImoGestaoScrapping implements IScrapping {

    private static final int MAX_PAGES = 12;
    private static final int MAX_EXTRA_LENGTH = 255;
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final Pattern BG_URL = Pattern.compile("url\\((['\"]?)(.*?)\\1\\)");
    private static final Pattern LINK = Pattern.compile("/imovel/(\\d+)/[^/]+/([^/]+)/([^/]+)/([^/?\"]+)");

    protected final DocumentFetcher fetcher;

    protected AbstractImoGestaoScrapping(DocumentFetcher fetcher) {
        this.fetcher = fetcher;
    }

    /** URL base de busca de venda (ex.: https://site/imoveis?pretensao=comprar). */
    protected abstract String searchUrl();

    /** Seletor das ancoras de cada anuncio (uma por card, contendo o href /imovel/). */
    protected abstract String anchorSelector();

    protected abstract BigDecimal priceOf(Element anchor);

    protected abstract BigDecimal areaOf(Element anchor);

    protected abstract String imageOf(Element anchor);

    protected abstract String titleOf(Element anchor);

    @Override
    public List<ImovelDTO> collect() throws IOException {
        List<ImovelDTO> result = new ArrayList<>();
        for (int page = 1; page <= MAX_PAGES; page++) {
            List<ImovelDTO> pageListings = extract(fetcher.get(searchUrl() + "&pagina=" + page));
            if (pageListings.isEmpty()) {
                break;
            }
            result.addAll(pageListings);
        }
        return result;
    }

    @Override
    public List<ImovelDTO> extract(Document listPage) {
        List<ImovelDTO> list = new ArrayList<>();
        for (Element anchor : listPage.select(anchorSelector())) {
            Matcher matcher = LINK.matcher(anchor.attr("href"));
            if (!matcher.find()) {
                continue;
            }
            if (!"blumenau".equalsIgnoreCase(matcher.group(3))) {
                continue; // recorte do piloto
            }
            BigDecimal price = priceOf(anchor);
            if (price == null || price.signum() <= 0) {
                continue;
            }
            BigDecimal area = areaOf(anchor);
            String href = anchor.attr("href");
            ImovelDTO dto = new ImovelDTO(
                    titleOf(anchor),
                    buildExtra(area),
                    price,
                    sourceCode(),
                    formatPrice(price),
                    href.startsWith("//") ? "https:" + href : href,
                    imageOf(anchor),
                    slugToName(matcher.group(3)),
                    slugToName(matcher.group(4)),
                    slugToName(matcher.group(2))
            );
            dto.setArea(area);
            list.add(dto);
        }
        return list;
    }

    protected BigDecimal parsePrice(Element anchor, String cssQuery) {
        Element el = anchor.selectFirst(cssQuery);
        return el != null ? ScrappingUtil.convertStringToBigDecimal(el.text()) : null;
    }

    protected BigDecimal parseArea(Element anchor, String cssQuery) {
        Element el = anchor.selectFirst(cssQuery);
        if (el == null) {
            return null;
        }
        BigDecimal area = ScrappingUtil.convertStringToBigDecimal(el.text());
        return (area != null && area.signum() > 0) ? area : null;
    }

    protected String backgroundImage(Element anchor, String cssQuery) {
        Element el = anchor.selectFirst(cssQuery);
        if (el == null) {
            return null;
        }
        Matcher matcher = BG_URL.matcher(el.attr("style"));
        return matcher.find() && !matcher.group(2).isBlank() ? matcher.group(2) : null;
    }

    protected String textOf(Element anchor, String cssQuery) {
        Element el = anchor.selectFirst(cssQuery);
        return el != null ? el.text().trim() : null;
    }

    private static String buildExtra(BigDecimal area) {
        if (area == null || area.signum() <= 0) {
            return null;
        }
        String value = area.stripTrailingZeros().toPlainString() + "m2";
        return value.length() <= MAX_EXTRA_LENGTH ? value : value.substring(0, MAX_EXTRA_LENGTH);
    }

    private static String formatPrice(BigDecimal price) {
        return price == null ? null : NumberFormat.getCurrencyInstance(PT_BR).format(price);
    }

    private static String slugToName(String slug) {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        String[] parts = slug.split("-");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }
}
