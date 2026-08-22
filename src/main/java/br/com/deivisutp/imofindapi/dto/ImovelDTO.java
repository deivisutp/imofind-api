package br.com.deivisutp.imofindapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ImovelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Long id;   

    private String titulo;

    private String extra;

    private Float price;

    private String price_varchar;

    private String origem;

    private String link;

    private String image;

    private String city;

    private String neighborhood;

    private String type;

    public ImovelDTO(Long id, String titulo, String extra, Float price, String origem, String price_varchar, String link, String image, String city, String neighborhood, String type) {
        this.id = id;
        this.titulo = titulo;
        this.extra = extra;
        this.price = price;
        this.origem = origem;
        this.price_varchar = price_varchar;
        this.link = link;
        this.image = image;
        this.city = city;
        this.neighborhood = neighborhood;
        this.type = type;
    }

    public ImovelDTO(String titulo, String extra, Float price, String origem, String price_varchar, String link, String image, String city, String neighborhood, String type) {
        this.titulo = titulo;
        this.extra = extra;
        this.price = price;
        this.origem = origem;
        this.price_varchar = price_varchar;
        this.link = link;
        this.image = image;
        this.city = city;
        this.neighborhood = neighborhood;
        this.type = type;
    }

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

