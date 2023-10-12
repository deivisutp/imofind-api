package br.com.deivisutp.imofindapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImovelFilterDTO {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String titulo;
    private String extra;
    private Float initialPrice;
    private Float endPrice;
    private String origem;
    private String city;
    private String neighborhood;
    private String type;
    private Integer page;
    private Integer size;
}
