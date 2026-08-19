package br.com.deivisutp.imofindapi.util.implementation;

import br.com.deivisutp.imofindapi.config.ScrapingProperties;
import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.util.DocumentFetcher;
import br.com.deivisutp.imofindapi.util.IScrapping;
import br.com.deivisutp.imofindapi.util.ScrappingUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static br.com.deivisutp.imofindapi.util.ScrappingList.ZAP_IMOVEIS;

@Component
public class ZapImoveisScrapping implements IScrapping {

    private static final String LISTA_ZAP_DETAIL = "div[class=result-card]";
    private static final String LISTA_ZAP = "div[class=listing-wrapper__content]";
    private static final String ZAP_CARD_CONTENT = "div[class=l-card__content]";
    private static final String ZAP_PRICE = "div[class=listing-price]";
    private static final String ZAP_URL = "https://www.zapimoveis.com.br/venda/apartamentos/sc+blumenau++victor-konder/?__ab=exp-aa-test:B,new-discover-zap:alert,vas-officialstore-social:enabled,deduplication:select&transacao=venda&onde=,Santa%20Catarina,Blumenau,,Victor%20Konder,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EVictor%20Konder,-26.90796,-49.07378,;,Santa%20Catarina,Blumenau,,Itoupava%20Seca,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EItoupava%20Seca,-26.889541,-49.087003,;,Santa%20Catarina,Blumenau,,Vila%20Nova,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EVila%20Nova,-26.902774,-49.089311,;,Santa%20Catarina,Blumenau,,Velha,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EVelha,-26.924159,-49.102428,;,Santa%20Catarina,Blumenau,,Itoupava%20Norte,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EItoupava%20Norte,-26.883239,-49.074135,&tipos=apartamento_residencial&precoMaximo=500000&ordem=Menor%20pre%C3%A7o";
    private static final String PAGE_ZAP = "&pagina=";

    private final DocumentFetcher fetcher;
    private final ScrapingProperties properties;

    public ZapImoveisScrapping(DocumentFetcher fetcher, ScrapingProperties properties) {
        this.fetcher = fetcher;
        this.properties = properties;
    }

    @Override
    public String sourceCode() {
        return ZAP_IMOVEIS;
    }

    @Override
    public String extractorVersion() {
        return "zap-v1";
    }

    @Override
    public List<ImovelDTO> collect() throws IOException {
        List<ImovelDTO> result = new ArrayList<>();
        for (int page = 1; page < properties.getMaxPages(); page++) {
            Document document = fetcher.get(ZAP_URL + PAGE_ZAP + page);
            List<ImovelDTO> pageListings = extract(document);
            if (pageListings.isEmpty()) {
                break;
            }
            result.addAll(pageListings);
        }
        return result;
    }

    @Override
    public List<ImovelDTO> extract(Document document) {
        List<ImovelDTO> list = new ArrayList<>();
        Elements imoveis = document.select(LISTA_ZAP).select(LISTA_ZAP_DETAIL);
        for (Element e : imoveis) {
            String content = e.select(ZAP_CARD_CONTENT).text();
            String priceText = firstText(e, ZAP_PRICE);
            String address = e.select("div[data-cy=card__address]").select("h2").attr("title");
            list.add(new ImovelDTO(
                    content,
                    content,
                    ScrappingUtil.convertStringToBigDecimal(priceText),
                    ZAP_IMOVEIS,
                    priceText,
                    e.select("a").attr("href"),
                    firstAttr(e, "div[class=l-carousel-image__container] ul[class=l-carousel-image__list] li > img", "src"),
                    getDataExtraFromForm(address, 0),
                    getDataExtraFromForm(address, 1),
                    "Apartamento"
            ));
        }
        return list;
    }

    private static String getDataExtraFromForm(final String text, int type) {
        if (text == null || text.isEmpty() || text.indexOf(',') <= 0) {
            return text;
        }
        String cidade = text.substring(text.indexOf(',') + 2);
        String bairro = text.substring(0, text.indexOf(','));
        return type == 0 ? cidade : bairro;
    }

    private static String firstText(Element scope, String cssQuery) {
        Element el = scope.select(cssQuery).first();
        return el != null ? el.text() : "";
    }

    private static String firstAttr(Element scope, String cssQuery, String attribute) {
        Element el = scope.select(cssQuery).first();
        return el != null ? el.attr(attribute) : "";
    }
}
