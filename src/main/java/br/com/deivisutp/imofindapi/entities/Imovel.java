package br.com.deivisutp.imofindapi.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "imovel")
public class Imovel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imovel_id")
    private Long id;

    @Column(name = "titulo", length = 4000)
    private String titulo;

    @Column(name = "extra", length = 4000)
    private String extra;

    @Column(name = "price")
    private Float price;

    @Column(name = "origem")
    private String origem;

    @Column(name = "price_varchar", length = 4000)
    private String price_varchar;

    @Column(name = "link", length = 4000)
    private String link;

    @Column(name = "image", length = 4000)
    private String image;
}
