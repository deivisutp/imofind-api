package br.com.deivisutp.imofindapi.util;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrappingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrappingUtil.class);


    ///Imoveis SC
    private static final String LISTA_IMOVEIS_SC = "div[class=lista-imoveis]";
    private static final String LISTA_IMOVEIS_DETAIL_SC = "article[class=imovel]";
    private static final String IMOVEIS_SC_PRICE = "small[itemprop=price]";
    private static final String IMOVEIS_SC_TITULO = "h2[class=imovel-titulo]";
    private static final String IMOVEIS_SC_EXTRA = "div[class=imovel-extra]";
    private static final String IMOVEIS_SC_URL= "https://www.imoveis-sc.com.br/blumenau/comprar/apartamento/boa-vista_victor-konder_vila-nova?valor=-250000";
    private static final String PAGE_IMOVEIS_SC= "&page=";

    //ACRC Imoveis
    private static final String LISTA_ACRC = "div[class=resultado]"; //"div[class=todos_imoveis]";
    private static final String ACRC_TITULO = "div[class=info_imoveis]";
    private static final String ACRC_EXTRA = "div[class=detalhes]";
    private static final String ACRC_PRICE = "div[class=valor]";

    private static final String ACRC_URL="https://www.acrcimoveis.com.br/comprar/sc/blumenau/boa-vista_itoupava-seca_ponta-aguda_victor-konder_vila-nova/apartamento/valor-0_250000/ordem-valor/resultado-crescente/quantidade-24/";
    private static final String PAGE_ACRC= "pagina-1/";

    //ZAP imoveis
    private static final String ZAP_COND = "li[class=card-price__item condominium text-regular]";
    private static final String ZAP_PRICE = "p[class=simple-card__price js-price color-darker heading-regular heading-regular__bolder align-left]";
    private static final String ZAP_EXTRA ="h2[class=simple-card__address color-dark text-regular]";
    private static final String ZAP_EXTRA2 ="ul[class=feature__container simple-card__amenities]";
    private static final String ZAP_TITULO = "span[class=simple-card__text text-regular]";
    private static final String LISTA_ZAP_DETAIL_SC = "div[class=card-container js-listing-card]";
    private static final String LISTA_ZAP ="div[class=listings__container]";
    private static final String ZAP_URL="https://www.zapimoveis.com.br/venda/apartamentos/sc+blumenau++victor-konder/?onde=,Santa%20Catarina,Blumenau,,Victor%20Konder,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EVictor%20Konder,-26.90796,-49.07378,%3B,Santa%20Catarina,Blumenau,,Itoupava%20Seca,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EItoupava%20Seca,-26.889541,-49.087003,%3B,Santa%20Catarina,Blumenau,,Vila%20Nova,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EVila%20Nova,-26.902774,-49.089311,%3B,Santa%20Catarina,Blumenau,,Velha,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EVelha,-26.924159,-49.102428,%3B,Santa%20Catarina,Blumenau,,Itoupava%20Norte,,,neighborhood,BR%3ESanta%20Catarina%3ENULL%3EBlumenau%3EBarrios%3EItoupava%20Norte,-26.883239,-49.074135,&transacao=Venda&tipo=Im%C3%B3vel%20usado&tipoUnidade=Residencial,&precoMaximo=250000&ordem=Menor%20pre%C3%A7o";
    private static final String PAGE_ZAP= "&pagina=";

    private static final String HTTPS = "https:";

    private static final String BASE_URL_GOOGLE = "https://www.google.com.br/search?q=";
    private static final String COMPLEMENTO_URL_GOOGLE = "&hl=pt-BR";

    public float convertStringToFloat(String givenString) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

        try {
            return decimalFormat.parse(givenString).floatValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<ImovelDTO> obterInfoImoveis(String url) {
        Document document = null;
        List<ImovelDTO> listImoveis = new ArrayList<>();

        Elements imoveis = null;
        try {
            LOGGER.info(url);
            //Paginas no Imóveis SC
            for (int i = 1; i < 6; i++) {
                // conecta no site
                document = Jsoup.connect(IMOVEIS_SC_URL+PAGE_IMOVEIS_SC+i).get();

                // recupera o titulo da pagina
                String title = document.title();
                LOGGER.info(title);

                document.select(LISTA_IMOVEIS_SC);

                imoveis = document.select(LISTA_IMOVEIS_SC).select(LISTA_IMOVEIS_DETAIL_SC);
                for (Element e : imoveis) {
                    listImoveis.add( new ImovelDTO( e.select(IMOVEIS_SC_TITULO).text(),
                                                    e.select(IMOVEIS_SC_EXTRA).text(),
                                                    convertStringToFloat(e.select(IMOVEIS_SC_PRICE).text()),
                                                    "IMOVEIS-SC",
                                                    e.select(IMOVEIS_SC_PRICE).text()));

                }
            }

            //Paginas no ACRC imoveis
            document = Jsoup.connect(ACRC_URL+PAGE_ACRC).get();

            imoveis = document.select(LISTA_ACRC);

            for (Element e : imoveis) {
                listImoveis.add( new ImovelDTO( e.select(ACRC_TITULO).text(),
                        e.select(ACRC_EXTRA).html().substring(0,250),
                        convertStringToFloat(e.select(ACRC_PRICE).last().text()),
                        "ACRC",
                        e.select(ACRC_PRICE).last().text()));

            }

            //Pagina ZAP imoveis
            for (int i = 1; i < 6; i++) {
                // conecta no site
                document = Jsoup.connect(ZAP_URL+PAGE_ZAP+i).get();

                // recupera o titulo da pagina
                String title = document.title();
                LOGGER.info(title);

                document.select(LISTA_ZAP);

                imoveis = document.select(LISTA_ZAP).select(LISTA_ZAP_DETAIL_SC);
                for (Element e : imoveis) {
                    listImoveis.add( new ImovelDTO( e.select(ZAP_TITULO).text().concat(e.select(ZAP_EXTRA2).text()),
                            e.select(ZAP_EXTRA).text().concat(" - " + e.select(ZAP_COND).text()),
                            convertStringToFloat(e.select(ZAP_PRICE).text()),
                            "ZAP-IMOVEIS",
                            e.select(ZAP_PRICE).text()));

                }
            }

            return listImoveis;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    public String montaUrlGoogle(String nomeEquipeCasa, String nomeEquipeVisitante) {
        try {
            String equipeCasa = nomeEquipeCasa.replace(" ", "+").replace("-", "+");
            String equipeVisitante = nomeEquipeVisitante.replace(" ", "+").replace("-", "+");

            return BASE_URL_GOOGLE + equipeCasa + "+x+" + equipeVisitante + COMPLEMENTO_URL_GOOGLE;
        } catch (Exception e) {
            LOGGER.error("ERRO: {}", e.getMessage());
        }
        return null;
    }

}
