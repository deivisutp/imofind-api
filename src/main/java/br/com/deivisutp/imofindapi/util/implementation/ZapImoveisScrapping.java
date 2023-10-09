package br.com.deivisutp.imofindapi.util.implementation;
import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.service.ImovelService;
import br.com.deivisutp.imofindapi.util.IScrapping;
import br.com.deivisutp.imofindapi.util.ScrappingUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static br.com.deivisutp.imofindapi.util.ScrappingList.ZAP_IMOVEIS;

@Component
@Qualifier(ZAP_IMOVEIS)
public class ZapImoveisScrapping implements IScrapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZapImoveisScrapping.class);

    @Autowired
    private ImovelService imovelService;

    private static final String LISTA_ZAP_DETAIL_SC = "div[class=result-card]";
    private static final String LISTA_ZAP ="div[class=listing-wrapper__content]";
    private static final String ZAP_URL="https://www.zapimoveis.com.br/venda/apartamentos/sc+blumenau++victor-konder/?onde=,Santa%20Catarina,Blumenau,,Victor%20Konder,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EVictor%20Konder,-26.90796,-49.07378,%3B,Santa%20Catarina,Blumenau,,Itoupava%20Seca,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EItoupava%20Seca,-26.889541,-49.087003,%3B,Santa%20Catarina,Blumenau,,Vila%20Nova,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EVila%20Nova,-26.902774,-49.089311,%3B,Santa%20Catarina,Blumenau,,Velha,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EVelha,-26.924159,-49.102428,%3B,Santa%20Catarina,Blumenau,,Itoupava%20Norte,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EItoupava%20Norte,-26.883239,-49.074135,&transacao=Venda&tipo=Im%C3%B3vel%20usado&tipoUnidade=Residencial,&precoMaximo=500000&ordem=Menor%20pre%C3%A7o";
    private static final String ZAP_URL2 ="https://www.zapimoveis.com.br/venda/apartamentos/sc+blumenau++victor-konder/?__ab=exp-aa-test:B,new-discover-zap:alert,vas-officialstore-social:enabled,deduplication:select&transacao=venda&onde=,Santa%20Catarina,Blumenau,,Victor%20Konder,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EVictor%20Konder,-26.90796,-49.07378,;,Santa%20Catarina,Blumenau,,Itoupava%20Seca,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EItoupava%20Seca,-26.889541,-49.087003,;,Santa%20Catarina,Blumenau,,Vila%20Nova,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EVila%20Nova,-26.902774,-49.089311,;,Santa%20Catarina,Blumenau,,Velha,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EVelha,-26.924159,-49.102428,;,Santa%20Catarina,Blumenau,,Itoupava%20Norte,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EItoupava%20Norte,-26.883239,-49.074135,&tipos=apartamento_residencial&precoMaximo=500000&ordem=Menor%20pre%C3%A7o";
    private static final String PAGE_ZAP= "&pagina=";

    @Override
    public void varrerImoveis() {
        Document document = null;
        List<ImovelDTO> listImoveis = new ArrayList<>();

        Elements imoveis = null;
        try {
            System.out.println(ZAP_URL2);
            LOGGER.info(ZAP_URL2);
            for (int i = 1; i < 30; i++) {
                document = Jsoup.connect(ZAP_URL2+PAGE_ZAP+i).get();

                String title = document.title();
                LOGGER.info(title);

                imoveis = document.select(LISTA_ZAP).select(LISTA_ZAP_DETAIL_SC);
                if (imoveis.size() == 0)
                    break;

                for (Element e : imoveis) {

                    listImoveis.add( new ImovelDTO( e.select("div[class=l-card__content]").text(),
                            e.select("div[class=l-card__content]").text(),
                            ScrappingUtil.convertStringToFloat(e.select("div[class=listing-price]").first().text()),
                            "ZAP-IMOVEIS",
                            e.select("div[class=listing-price]").first().text(),
                            e.select("a").attr("href"),
                            e.select("div[class=l-carousel-image__container]").select("ul[class=l-carousel-image__list]").select("li > img").first().attr("src")
                    ));
                }
            }
            imovelService.save(listImoveis);
        } catch (
        IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }
}
