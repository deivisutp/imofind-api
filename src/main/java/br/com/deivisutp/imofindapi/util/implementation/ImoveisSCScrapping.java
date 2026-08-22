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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.deivisutp.imofindapi.util.ScrappingList.IMOVEIS_SC;

@Component
public class ImoveisSCScrapping implements IScrapping {

    private static final String LISTA_IMOVEIS_SC = "div[class=lista-imoveis]";
    private static final String LISTA_IMOVEIS_DETAIL_SC = "article[class=imovel]";
    private static final String IMOVEIS_SC_PRICE = "small[itemprop=price]";
    private static final String IMOVEIS_SC_TITULO = "h2[class=imovel-titulo]";
    private static final String IMOVEIS_SC_EXTRA = "div[class=imovel-extra]";
    private static final String IMOVEIS_SC_URL = "https://www.imoveis-sc.com.br/blumenau/comprar/apartamento/boa-vista_victor-konder_vila-nova?valor=-500000";

    private final DocumentFetcher fetcher;
    private final ScrapingProperties properties;

    public ImoveisSCScrapping(DocumentFetcher fetcher, ScrapingProperties properties) {
        this.fetcher = fetcher;
        this.properties = properties;
    }

    @Override
    public String sourceCode() {
        return IMOVEIS_SC;
    }

    @Override
    public String extractorVersion() {
        return "imoveis-sc-v1";
    }

    @Override
    public List<ImovelDTO> collect() throws IOException {
        List<ImovelDTO> result = new ArrayList<>();
        Document base = fetcher.get(IMOVEIS_SC_URL);
        List<Element> options = getFormParamsImoveisSC(base);
        if (options.isEmpty()) {
            result.addAll(extract(base));
            return result;
        }
        for (Element option : options) {
            String url = option.attr("value");
            if (url == null || url.isEmpty()) {
                continue;
            }
            result.addAll(extract(fetcher.get(url)));
        }
        return result;
    }

    @Override
    public List<ImovelDTO> extract(Document document) {
        List<ImovelDTO> list = new ArrayList<>();
        Elements imoveis = document.select(LISTA_IMOVEIS_SC).select(LISTA_IMOVEIS_DETAIL_SC);
        for (Element e : imoveis) {
            String extra = e.select(IMOVEIS_SC_EXTRA).text();
            String priceText = e.select(IMOVEIS_SC_PRICE).text();
            list.add(new ImovelDTO(
                    e.select(IMOVEIS_SC_TITULO).text(),
                    extra,
                    ScrappingUtil.convertStringToBigDecimal(priceText),
                    IMOVEIS_SC,
                    priceText,
                    firstAttr(e, "div[class=imovel-actions] a", "href"),
                    firstAttr(e, "img", "src"),
                    getDataExtraFromForm(extra, 0),
                    getDataExtraFromForm(extra, 1),
                    e.select(IMOVEIS_SC_TITULO).select("meta[itemprop=model]").attr("content")
            ));
        }
        return list;
    }

    private static List<Element> getFormParamsImoveisSC(final Document doc) {
        Element navigation = doc.select("div.navigation").first();
        if (navigation == null) {
            return Collections.emptyList();
        }
        return navigation.select("div[class=form-group form-select]")
                .select("select > option")
                .stream()
                .collect(Collectors.toList());
    }

    private static String getDataExtraFromForm(final String text, int type) {
        if (text == null || text.isEmpty() || text.indexOf(',') <= 0) {
            return text;
        }
        String cidade = text.substring(0, text.indexOf(','));
        String bairro;
        if (text.indexOf("Cód.:") <= 0) {
            bairro = text.substring(text.indexOf(',') + 2);
        } else {
            bairro = text.substring(text.indexOf(',') + 2, text.indexOf("Cód.:") - 1);
        }
        return type == 0 ? cidade : bairro;
    }

    private static String firstAttr(Element scope, String cssQuery, String attribute) {
        Element el = scope.select(cssQuery).first();
        return el != null ? el.attr(attribute) : "";
    }
}
