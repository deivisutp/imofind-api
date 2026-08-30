package br.com.deivisutp.imofindapi.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyTypeNormalizerTests {

    @Test
    void colapsaVariantesDeApartamentoECasa() {
        assertThat(PropertyTypeNormalizer.normalize("Apartamento")).isEqualTo("Apartamento");
        assertThat(PropertyTypeNormalizer.normalize("Apartamentos")).isEqualTo("Apartamento");
        assertThat(PropertyTypeNormalizer.normalize("Casa")).isEqualTo("Casa");
        assertThat(PropertyTypeNormalizer.normalize("Casas")).isEqualTo("Casa");
    }

    @Test
    void agrupaTerrenoComercialERural() {
        assertThat(PropertyTypeNormalizer.normalize("Lote Terreno")).isEqualTo("Terreno");
        assertThat(PropertyTypeNormalizer.normalize("Sala Comercial")).isEqualTo("Comercial");
        assertThat(PropertyTypeNormalizer.normalize("Galpao")).isEqualTo("Comercial");
        assertThat(PropertyTypeNormalizer.normalize("Predio Comercial")).isEqualTo("Comercial");
        assertThat(PropertyTypeNormalizer.normalize("Sitio")).isEqualTo("Rural");
        assertThat(PropertyTypeNormalizer.normalize("Chácara")).isEqualTo("Rural");
    }

    @Test
    void ignoraAcentosEspacosECaixa() {
        assertThat(PropertyTypeNormalizer.normalize("  edifício   COMERCIAL ")).isEqualTo("Comercial");
        assertThat(PropertyTypeNormalizer.normalize("SÍTIO")).isEqualTo("Rural");
    }

    @Test
    void tiposDesconhecidosViramOutroEnulosPermanecemNulos() {
        assertThat(PropertyTypeNormalizer.normalize("Marina")).isEqualTo("Outro");
        assertThat(PropertyTypeNormalizer.normalize("")).isNull();
        assertThat(PropertyTypeNormalizer.normalize(null)).isNull();
    }
}
