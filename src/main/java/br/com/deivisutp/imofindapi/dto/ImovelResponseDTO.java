package br.com.deivisutp.imofindapi.dto;

import br.com.deivisutp.imofindapi.entities.Imovel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImovelResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<Imovel> imoveis;
    private Long totalElements;

}