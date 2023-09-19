package br.com.deivisutp.imofindapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ImovelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String titulo;

    private String extra;

    private Float price;

    private String price_varchar;

    private String origem;

    private String link;

    private String image;

    public ImovelDTO(String titulo, String extra, Float price, String origem, String price_varchar, String link, String image) {
        this.titulo = titulo;
        this.extra = extra;
        this.price = price;
        this.origem = origem;
        this.price_varchar = price_varchar;
        this.link = link;
        this.image = image;
    }

    public ImovelDTO(String titulo, String extra, Float price, String origem, String price_varchar, String link) {
        this.titulo = titulo;
        this.extra = extra;
        this.price = price;
        this.origem = origem;
        this.price_varchar = price_varchar;
        this.link = link;
    }

    public ImovelDTO(String titulo, String extra, Float price, String origem, String price_varchar) {
        this.titulo = titulo;
        this.extra = extra;
        this.price = price;
        this.origem = origem;
        this.price_varchar = price_varchar;
    }

    public ImovelDTO() {
    }
}

