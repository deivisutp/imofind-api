package br.com.deivisutp.imofindapi.repository.filter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;
import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class ImovelFilter implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer codigoImovel;
    private String descricaoImovel;
    private String origemImovel;
    private BigDecimal valorImovel;
}