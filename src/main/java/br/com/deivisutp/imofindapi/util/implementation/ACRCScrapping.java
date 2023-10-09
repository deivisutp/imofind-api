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

import static br.com.deivisutp.imofindapi.util.ScrappingList.ACRC;

@Component
@Qualifier(ACRC)
public class ACRCScrapping implements IScrapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrappingUtil.class);

    @Autowired
    private ImovelService imovelService;

    private static final String LISTA_ACRC = "div[class=resultado]"; //"div[class=todos_imoveis]";
    private static final String ACRC_TITULO = "div[class=info_imoveis]";
    private static final String ACRC_EXTRA = "div[class=detalhes]";
    private static final String ACRC_URL ="https://www.acrcimoveis.com.br/comprar/sc/blumenau_indaial_timbo/apartamento_casa/valor-0_500000/ordem-valor/resultado-crescente/";
    private static final String PAGE_ACRC = "pagina-";
    private static final String ACRC_WEBSITE = "https://www.acrcimoveis.com.br";
    private static final String ACRC_TEXTO = "div[id=ctrl_sticky]";
    @Override
    public void varrerImoveis() {
        Document document = null;
        List<ImovelDTO> listImoveis = new ArrayList<>();

        Elements imoveis = null;
        try {
            System.out.println(ACRC_URL);
            LOGGER.info(ACRC_URL);
            for (int i = 1; i < 30; i++) {
                document = Jsoup.connect(ACRC_URL + PAGE_ACRC + i).get();

                imoveis = document.select(LISTA_ACRC);

                if (imoveis.size() == 0)
                    break;

                for (Element e : imoveis) {
                    Document doc = Jsoup.connect(String.join("", ACRC_WEBSITE, e.select("a").attr("href"))).get();

                    String extra = doc.select(ACRC_TEXTO).select("div[class=texto]").first().text();
                    listImoveis.add(new ImovelDTO(
                            e.select(ACRC_TITULO).text(),
                            extra.substring(0, 250),
                            ScrappingUtil.convertStringToFloat(e.select("div[class=valor]").select("h5").text()),
                            "ACRC",
                            e.select("div[class=valor]").select("h5").text(),
                            String.join("", ACRC_WEBSITE, e.select("a").attr("href")),
                            e.select("img").attr("src")
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
