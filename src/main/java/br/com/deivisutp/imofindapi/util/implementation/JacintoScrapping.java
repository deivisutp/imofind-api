package br.com.deivisutp.imofindapi.util.implementation;

import br.com.deivisutp.imofindapi.util.DocumentFetcher;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static br.com.deivisutp.imofindapi.util.ScrappingList.JACINTO;

/** Jacinto Imoveis (ImoGestao), Blumenau. */
@Component
public class JacintoScrapping extends AbstractImoGestaoScrapping {

    public JacintoScrapping(DocumentFetcher fetcher) {
        super(fetcher);
    }

    @Override
    public String sourceCode() {
        return JACINTO;
    }

    @Override
    public String extractorVersion() {
        return "jacinto-v1";
    }

    @Override
    protected String searchUrl() {
        return "https://jacintoimoveis.com.br/imoveis?pretensao=comprar";
    }

    @Override
    protected String anchorSelector() {
        return "div.imovel-list a:has(div.prince-code)";
    }

    @Override
    protected BigDecimal priceOf(Element anchor) {
        return parsePrice(anchor, "div.prince-code span.prince");
    }

    @Override
    protected BigDecimal areaOf(Element anchor) {
        return parseArea(anchor, "span.vacancies.detalhe span.txt_icon");
    }

    @Override
    protected String imageOf(Element anchor) {
        return backgroundImage(anchor, "div.image div[style]");
    }

    @Override
    protected String titleOf(Element anchor) {
        return textOf(anchor, "div.title");
    }
}
