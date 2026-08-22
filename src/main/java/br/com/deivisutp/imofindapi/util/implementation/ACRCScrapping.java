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

import static br.com.deivisutp.imofindapi.util.ScrappingList.ACRC;

@Component
public class ACRCScrapping implements IScrapping {

    private static final String LISTA_ACRC = "div[class=resultado]";
    private static final String ACRC_TITULO = "div[class=info_imoveis]";
    private static final String ACRC_EXTRA = "div[class=detalhes]";
    private static final String ACRC_URL = "https://www.acrcimoveis.com.br/comprar/sc/blumenau_indaial_timbo/apartamento_casa/valor-0_500000/ordem-valor/resultado-crescente/";
    private static final String PAGE_ACRC = "pagina-";
    private static final String ACRC_WEBSITE = "https://www.acrcimoveis.com.br";
    private static final int MAX_EXTRA_LENGTH = 255;

    private final DocumentFetcher fetcher;
    private final ScrapingProperties properties;

    public ACRCScrapping(DocumentFetcher fetcher, ScrapingProperties properties) {
        this.fetcher = fetcher;
        this.properties = properties;
    }

    @Override
    public String sourceCode() {
        return ACRC;
    }

    @Override
    public String extractorVersion() {
        return "acrc-v1";
    }

    @Override
    public List<ImovelDTO> collect() throws IOException {
        List<ImovelDTO> result = new ArrayList<>();
        for (int page = 1; page < properties.getMaxPages(); page++) {
            Document document = fetcher.get(ACRC_URL + PAGE_ACRC + page);
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
        Elements imoveis = document.select(LISTA_ACRC);
        for (Element e : imoveis) {
            String priceText = e.select("div[class=valor]").select("h5").text();
            list.add(new ImovelDTO(
                    e.select(ACRC_TITULO).text(),
                    truncate(e.select(ACRC_EXTRA).text()),
                    ScrappingUtil.convertStringToBigDecimal(priceText),
                    ACRC,
                    priceText,
                    ACRC_WEBSITE + e.select("a").attr("href"),
                    e.select("img").attr("src"),
                    e.select(ACRC_TITULO).select("h4[class=cidade]").text(),
                    e.select(ACRC_TITULO).select("h4[class=bairro]").text(),
                    e.select(ACRC_TITULO).select("h3[class=tipo]").text()
            ));
        }
        return list;
    }

    private static String truncate(String text) {
        if (text == null || text.length() <= MAX_EXTRA_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_EXTRA_LENGTH);
    }
}
