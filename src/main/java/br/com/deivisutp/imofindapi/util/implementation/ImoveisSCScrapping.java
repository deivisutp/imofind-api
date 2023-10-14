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
import java.util.stream.Collectors;

import static br.com.deivisutp.imofindapi.util.ScrappingList.IMOVEIS_SC;

@Component
@Qualifier(IMOVEIS_SC)
public class ImoveisSCScrapping implements IScrapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImoveisSCScrapping.class);

    @Autowired
    private ImovelService imovelService;

    private static final String LISTA_IMOVEIS_SC = "div[class=lista-imoveis]";
    private static final String LISTA_IMOVEIS_DETAIL_SC = "article[class=imovel]";
    private static final String IMOVEIS_SC_PRICE = "small[itemprop=price]";
    private static final String IMOVEIS_SC_TITULO = "h2[class=imovel-titulo]";
    private static final String IMOVEIS_SC_EXTRA = "div[class=imovel-extra]";
    private static final String IMOVEIS_SC_URL= "https://www.imoveis-sc.com.br/blumenau/comprar/apartamento/boa-vista_victor-konder_vila-nova?valor=-500000";

    public static List<Element> getFormParamsImoveisSC(final Document doc) {
        return doc.select("div.navigation")
                .first()
                .select("div[class=form-group form-select]")
                .select("select > option")
                .stream()
                .collect(Collectors.toList());
    }

    private static String getDataExtrafromForm(final String text, int type) {
        try {
            if (text == null || text.isEmpty())
                return text;

            if (text.indexOf(",") <= 0)
                return text;

            String cidade = text.substring(0, text.indexOf(","));
            String bairro;

            if (text.indexOf("Cód.:") <= 0)
                bairro = text.substring(text.indexOf(",")+2, text.length());
            else
                bairro = text.substring(text.indexOf(",")+2, text.indexOf("Cód.:")-1);

            return type == 0 ? cidade : bairro;
        } catch (Exception e ) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }

        return text;
    }

    @Override
    public void varrerImoveis() {
        System.out.println(IMOVEIS_SC);
        LOGGER.info(IMOVEIS_SC);

        Document document = null;
        List<ImovelDTO> listImoveis = new ArrayList<>();

        Elements imoveis = null;
        try {
            LOGGER.info(IMOVEIS_SC_URL);
            Document doc = Jsoup.connect(IMOVEIS_SC_URL).get();
            List<Element> els = getFormParamsImoveisSC(doc);

            for (Element el : els ) {
                document = Jsoup.connect(el.attr("value")).get();

                String title = document.title();
                LOGGER.info(title);

                document.select(LISTA_IMOVEIS_SC);

                imoveis = document.select(LISTA_IMOVEIS_SC).select(LISTA_IMOVEIS_DETAIL_SC);
                for (Element e : imoveis) {
                    listImoveis.add( new ImovelDTO( e.select(IMOVEIS_SC_TITULO).text(),
                            e.select(IMOVEIS_SC_EXTRA).text(),
                            ScrappingUtil.convertStringToFloat(e.select(IMOVEIS_SC_PRICE).text()),
                            "IMOVEIS-SC",
                            e.select(IMOVEIS_SC_PRICE).text(),
                            e.select("div[class=imovel-actions]").select("a").first().attr("href"),
                            e.select("img").first().attr("src"),
                            getDataExtrafromForm(e.select(IMOVEIS_SC_EXTRA).text(),0),
                            getDataExtrafromForm(e.select(IMOVEIS_SC_EXTRA).text(),1),
                            e.select("h2[class=imovel-titulo]").select("meta[itemprop=model]").attr("content")
                            ));
                }
            }
            imovelService.save(listImoveis);

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }
}
