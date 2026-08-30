package br.com.deivisutp.imofindapi.util;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Normaliza os rotulos de tipo vindos das diversas fontes (Apartamento/Apartamentos,
 * Casa/Casas, Lote Terreno, Galpao, Sitio...) para um conjunto canonico usado pela
 * projecao `imovel` (filtros e indicadores). Ajuste os grupos aqui para mais/menos granularidade.
 */
public final class PropertyTypeNormalizer {

    /** Categoria usada quando o tipo bruto nao se encaixa em nenhum grupo conhecido. */
    public static final String OUTRO = "Outro";

    private static final Map<String, String> CANONICAL = buildMap();

    private PropertyTypeNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String key = simplify(raw);
        if (key.isEmpty()) {
            return null;
        }
        return CANONICAL.getOrDefault(key, OUTRO);
    }

    /** minusculo, sem acentos e com espacos colapsados, para casar variantes de escrita. */
    private static String simplify(String value) {
        String noAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return noAccents.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static Map<String, String> buildMap() {
        Map<String, String> m = new HashMap<>();
        putAll(m, "Apartamento", "apartamento", "apartamentos", "apto", "kitnet", "kitchenette",
                "studio", "cobertura", "flat", "loft");
        putAll(m, "Casa", "casa", "casas", "casa geminada", "geminada");
        putAll(m, "Sobrado", "sobrado", "sobrados");
        putAll(m, "Terreno", "terreno", "terrenos", "lote", "lotes", "lote terreno");
        putAll(m, "Comercial", "comercial", "sala", "salas", "sala comercial", "conjunto comercial",
                "loja", "lojas", "ponto", "ponto comercial", "galpao", "galpoes", "barracao",
                "pavilhao", "deposito", "box", "garagem", "hotel", "edificio comercial",
                "predio comercial", "predio", "predios", "predio residencial");
        putAll(m, "Rural", "chacara", "chacaras", "fazenda", "fazendas", "sitio", "sitios",
                "rural", "area rural", "terreno rural");
        return m;
    }

    private static void putAll(Map<String, String> m, String canonical, String... keys) {
        for (String k : keys) {
            m.put(k, canonical);
        }
    }
}
