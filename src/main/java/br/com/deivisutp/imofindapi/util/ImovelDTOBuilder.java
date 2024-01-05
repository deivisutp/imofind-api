package br.com.deivisutp.imofindapi.util;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

@Getter
@Setter
public class ImovelDTOBuilder {
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

    public ImovelDTOBuilder with(
            Consumer<ImovelDTOBuilder> builderFunction) {
        builderFunction.accept(this);
        return this;
    }

    public ImovelDTO createImovelDTO() {
        return new ImovelDTO(id, titulo, extra, price, origem, price_varchar, link, image, city, neighborhood, type);
    }
}
