package br.com.deivisutp.imofindapi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public final class ScrappingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrappingUtil.class);

    private ScrappingUtil() {
    }

    /**
     * Converte preços em formato brasileiro (ex.: "R$ 1.234.567,89") em BigDecimal,
     * de forma independente do locale da JVM. Trata "." como separador de milhar e "," como decimal.
     */
    public static BigDecimal convertStringToBigDecimal(String givenString) {
        if (givenString == null) {
            return null;
        }
        String cleaned = givenString.replaceAll("[^0-9.,]", "");
        if (cleaned.isEmpty()) {
            return null;
        }
        cleaned = cleaned.replace(".", "").replace(",", ".");
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            LOGGER.warn("Falha ao converter preco: {}", givenString);
            return null;
        }
    }
}
