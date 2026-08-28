package br.com.deivisutp.imofindapi.dto;

import java.math.BigDecimal;

/** Reducao de preco recente de um anuncio (evento PRICE_CHANGED com queda). */
public interface ReducaoPreco {
    String getTitulo();

    String getNeighborhood();

    String getCity();

    String getLink();

    String getOrigem();

    BigDecimal getOldPrice();

    BigDecimal getNewPrice();
}
