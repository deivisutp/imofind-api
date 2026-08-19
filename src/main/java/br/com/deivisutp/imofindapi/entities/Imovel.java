package br.com.deivisutp.imofindapi.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

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

    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "origem")
    private String origem;

    @Column(name = "price_varchar", length = 4000)
    private String price_varchar;

    @Column(name = "link", length = 4000)
    private String link;

    @Column(name = "image", length = 4000)
    private String image;

    @Column(name = "city")
    private String city;

    @Column(name = "neighborhood")
    private String neighborhood;

    @Column(name = "type")
    private String type;

    @Column(name = "source_listing_id")
    private Long sourceListingId;

    @Column(name = "external_id", length = 64)
    private String externalId;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "first_seen_at")
    private Instant firstSeenAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;
}
