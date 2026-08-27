package br.com.deivisutp.imofindapi.dto;

import java.math.BigDecimal;

/** Indicadores agregados de ofertas ativas por cidade/bairro. */
public interface NeighborhoodAggregate {
    String getCity();

    String getNeighborhood();

    Long getCount();

    BigDecimal getMedianPrice();

    BigDecimal getMinPrice();

    BigDecimal getMaxPrice();

    BigDecimal getAvgPrice();

    BigDecimal getMedianPricePerSqm();

    Long getSampleWithArea();
}
