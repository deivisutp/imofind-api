package br.com.deivisutp.imofindapi.util.implementation;

import br.com.deivisutp.imofindapi.util.DocumentFetcher;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static br.com.deivisutp.imofindapi.util.ScrappingList.ABELARDO;

/** Abelardo Imoveis (ImoGestao), recorte de Blumenau. */
@Component
public class AbelardoScrapping extends AbstractImoGestaoScrapping {

    public AbelardoScrapping(DocumentFetcher fetcher) {
        super(fetcher);
    }

    @Override
    public String sourceCode() {
        return ABELARDO;
    }

    @Override
    public String extractorVersion() {
        return "abelardo-v1";
    }

    @Override
    protected String searchUrl() {
        return "https://www.abelardoimoveis.com.br/imoveis?pretensao=comprar";
    }

    @Override
    protected String anchorSelector() {
        return "a:has(div.block-imovel-box-lista)";
    }

    @Override
    protected BigDecimal priceOf(Element anchor) {
        return parsePrice(anchor, "div.valor-imovel-lista span");
    }

    @Override
    protected BigDecimal areaOf(Element anchor) {
        return null; // tema do abelardo nao expoe area estruturada na lista
    }

    @Override
    protected String imageOf(Element anchor) {
        return backgroundImage(anchor, "div.img-imovel[style]");
    }

    @Override
    protected String titleOf(Element anchor) {
        return textOf(anchor, "div.titulo-imovel-lista h3");
    }
}
