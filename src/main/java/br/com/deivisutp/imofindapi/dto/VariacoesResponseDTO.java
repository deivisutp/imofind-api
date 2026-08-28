package br.com.deivisutp.imofindapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class VariacoesResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private VariacaoResumo resumo;
    private List<ReducaoPreco> reducoes;
}
